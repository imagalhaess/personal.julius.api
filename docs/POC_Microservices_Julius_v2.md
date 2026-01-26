# PERSONAL JULIUS API

## Proof of Concept (POC) - Arquitetura de Microsserviços

**BECA Java Jr - NTT DATA 2025/2026**  
**Desenvolvido por:** Isabela Magalhães  
**Versão:** 2.0 - Completa

---

## Índice

1. [Informações do Projeto](#1-informações-do-projeto)
2. [Arquitetura de Microsserviços](#2-arquitetura-de-microsserviços)
3. [Estrutura dos Microsserviços](#3-estrutura-dos-microsserviços)
4. [Modelo de Dados](#4-modelo-de-dados)
5. [Validação de Inputs (Bean Validation)](#5-validação-de-inputs-bean-validation)
6. [Endpoints da API](#6-endpoints-da-api)
7. [Fluxo de Transações (Kafka)](#7-fluxo-de-transações-kafka)
8. [Integrações Externas](#8-integrações-externas)
9. [Relatórios (Download PDF/Excel)](#9-relatórios-download-pdfexcel)
10. [Spring Security + JWT](#10-spring-security--jwt)
11. [Stack Tecnológico](#11-stack-tecnológico)
12. [Docker e Produção](#12-docker-e-produção)
13. [Flyway (Migrations)](#13-flyway-migrations)
14. [Testes](#14-testes)
15. [O Que Falta Implementar](#15-o-que-falta-implementar)
16. [Entregáveis](#16-entregáveis)
17. [Métricas de Sucesso](#17-métricas-de-sucesso)

---

## 1. Informações do Projeto

| Campo | Valor |
|-------|-------|
| **Nome** | Personal Julius API |
| **Descrição** | API de gerenciamento de finanças pessoais com arquitetura de microsserviços, comunicação via Kafka, autenticação JWT, integração com APIs externas e geração de relatórios |
| **Repositório** | github.com/imagalhaess/personal.julius.api |
| **Branch** | dev/refactor |
| **Java** | 21 (LTS) |
| **Build Tool** | Maven (Multi-module) |
| **Arquitetura** | Microsserviços + Clean Architecture + DDD |

---

## 2. Arquitetura de Microsserviços

### 2.1 Visão Geral

O projeto é composto por **2 microsserviços independentes** que se comunicam via Apache Kafka:

| Microsserviço | Responsabilidade | Porta DEV | Porta PROD |
|---------------|------------------|-----------|------------|
| **ms-user** | Gestão de usuários, autenticação JWT, importação Excel | 8081 | 8081 |
| **ms-transaction** | CRUD de transações, análise de despesas, integração APIs, relatórios | 8082 | 8082 |

### 2.2 Diagrama de Arquitetura Completo

```
                                    ┌─────────────────────────────────────┐
                                    │          KAFKA CLUSTER              │
                                    │  ┌─────────────────────────────┐   │
                                    │  │  topic: transaction.requested│   │
                                    │  └─────────────────────────────┘   │
                                    │  ┌─────────────────────────────┐   │
                                    │  │  topic: transaction.dlq      │   │
                                    │  └─────────────────────────────┘   │
                                    │  ┌─────────────────────────────┐   │
                                    │  │  topic: user.events          │   │
                                    │  └─────────────────────────────┘   │
                                    └──────────────┬──────────────────────┘
                                                   │
                    ┌──────────────────────────────┼──────────────────────────────┐
                    │                              │                              │
                    ▼                              ▼                              ▼
┌───────────────────────────────┐  ┌───────────────────────────────┐  ┌─────────────────────┐
│       MS1 - USER SERVICE      │  │   MS2 - TRANSACTION SERVICE   │  │   EXTERNAL APIs     │
│           (8081)              │  │           (8082)              │  │                     │
│                               │  │                               │  │  ┌───────────────┐  │
│  ┌─────────────────────────┐  │  │  ┌─────────────────────────┐  │  │  │  BrasilAPI    │  │
│  │  REST API CRUD Usuários │  │  │  │  Transaction Command API│  │  │  │  (Câmbio)     │  │
│  │  POST /api/auth/login   │  │  │  │  POST /api/transactions │  │  │  └───────────────┘  │
│  │  POST /api/users/import │  │  │  │  GET /api/transactions  │  │  │                     │
│  └─────────────────────────┘  │  │  └─────────────────────────┘  │  │  ┌───────────────┐  │
│                               │  │                               │  │  │  MockAPI      │  │
│  ┌─────────────────────────┐  │  │  ┌─────────────────────────┐  │  │  │  (Saldo/Conta)│  │
│  │       User DB           │  │  │  │  Transaction Processor  │  │  │  └───────────────┘  │
│  │     (PostgreSQL)        │  │  │  │  Consumer Worker        │  │  │                     │
│  └─────────────────────────┘  │  │  └─────────────────────────┘  │  └─────────────────────┘
│                               │  │                               │
│                               │  │  ┌─────────────────────────┐  │
│                               │  │  │    Transaction DB       │  │
│                               │  │  │     (PostgreSQL)        │  │
│                               │  │  └─────────────────────────┘  │
└───────────────────────────────┘  └───────────────────────────────┘

                    ▲                              ▲
                    │                              │
                    │      JWT + Request           │
                    └──────────────┬───────────────┘
                                   │
                    ┌──────────────┴───────────────┐
                    │           CLIENTE            │
                    │    (Web/Mobile/Postman)      │
                    └──────────────────────────────┘
```

### 2.3 Tópicos Kafka

| Tópico | Producer | Consumer | Propósito |
|--------|----------|----------|-----------|
| `transaction.requested` | ms-transaction | ms-transaction (Processor) | Transações criadas aguardando processamento |
| `transaction.dlq` | ms-transaction | (análise manual) | Dead Letter Queue - erros de processamento |
| `user.events` | ms-user | ms-transaction | Notificar criação/atualização de usuários |

---

## 3. Estrutura dos Microsserviços

### 3.1 ms-user (Microsserviço de Usuários)

```
ms-user/
├── src/
│   ├── main/
│   │   ├── java/nttdata/personal/julius/api/
│   │   │   ├── adapter/
│   │   │   │   ├── controller/
│   │   │   │   │   ├── AuthController.java
│   │   │   │   │   └── UserController.java
│   │   │   │   ├── dto/
│   │   │   │   │   ├── LoginRequest.java
│   │   │   │   │   ├── LoginResponse.java
│   │   │   │   │   ├── UserRequest.java
│   │   │   │   │   ├── UserResponse.java
│   │   │   │   │   └── UserUpdateRequest.java
│   │   │   │   └── exception/
│   │   │   │       └── GlobalExceptionHandler.java
│   │   │   ├── application/
│   │   │   │   ├── dto/
│   │   │   │   │   └── (DTOs internos)
│   │   │   │   └── service/
│   │   │   │       ├── AuthService.java
│   │   │   │       ├── UserService.java
│   │   │   │       └── ExcelImportService.java
│   │   │   ├── domain/
│   │   │   │   ├── exception/
│   │   │   │   │   └── BusinessException.java
│   │   │   │   ├── model/
│   │   │   │   │   └── User.java
│   │   │   │   └── repository/
│   │   │   │       └── UserRepository.java
│   │   │   └── infrastructure/
│   │   │       ├── config/
│   │   │       │   └── OpenApiConfig.java
│   │   │       ├── messaging/
│   │   │       │   ├── UserCreatedEvent.java
│   │   │       │   └── UserEventProducer.java
│   │   │       ├── persistence/
│   │   │       │   ├── entity/
│   │   │       │   │   └── UserEntity.java
│   │   │       │   └── repository/
│   │   │       │       ├── UserJpaRepository.java
│   │   │       │       └── UserPersistenceAdapter.java
│   │   │       └── security/
│   │   │           ├── JwtService.java
│   │   │           ├── JwtAuthenticationFilter.java
│   │   │           ├── SecurityConfig.java
│   │   │           └── UserDetailsServiceImpl.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       └── db/migration/
│   │           ├── V1__create_users_table.sql
│   │           └── V2__add_indexes.sql
│   └── test/
│       └── java/
├── Dockerfile
└── pom.xml
```

### 3.2 ms-transaction (Microsserviço de Transações)

```
ms-transaction/
├── src/
│   ├── main/
│   │   ├── java/nttdata/personal/julius/api/
│   │   │   ├── adapter/
│   │   │   │   ├── controller/
│   │   │   │   │   ├── TransactionController.java
│   │   │   │   │   ├── ExchangeController.java
│   │   │   │   │   └── ReportController.java
│   │   │   │   ├── dto/
│   │   │   │   │   ├── TransactionRequest.java
│   │   │   │   │   ├── TransactionResponse.java
│   │   │   │   │   ├── BalanceResponse.java
│   │   │   │   │   └── ExchangeResponse.java
│   │   │   │   └── exception/
│   │   │   │       └── GlobalExceptionHandler.java
│   │   │   ├── application/
│   │   │   │   ├── dto/
│   │   │   │   │   └── (DTOs internos)
│   │   │   │   └── service/
│   │   │   │       ├── TransactionService.java
│   │   │   │       ├── BalanceService.java
│   │   │   │       ├── ExchangeService.java
│   │   │   │       └── ReportService.java
│   │   │   ├── domain/
│   │   │   │   ├── exception/
│   │   │   │   │   └── BusinessException.java
│   │   │   │   ├── model/
│   │   │   │   │   ├── Transaction.java
│   │   │   │   │   ├── Category.java
│   │   │   │   │   ├── TransactionType.java
│   │   │   │   │   └── TransactionStatus.java
│   │   │   │   └── repository/
│   │   │   │       └── TransactionRepository.java
│   │   │   └── infrastructure/
│   │   │       ├── config/
│   │   │       │   ├── KafkaConfig.java
│   │   │       │   ├── OpenApiConfig.java
│   │   │       │   └── CacheConfig.java
│   │   │       ├── external/
│   │   │       │   ├── brasilapi/
│   │   │       │   │   ├── BrasilApiClient.java
│   │   │       │   │   └── ExchangeRateResponse.java
│   │   │       │   └── mockapi/
│   │   │       │       ├── MockApiClient.java
│   │   │       │       ├── AccountBalanceResponse.java
│   │   │       │       └── AccountLimitsResponse.java
│   │   │       ├── messaging/
│   │   │       │   ├── TransactionRequestedEvent.java
│   │   │       │   ├── TransactionEventProducer.java
│   │   │       │   ├── TransactionEventConsumer.java
│   │   │       │   └── DlqProducer.java
│   │   │       ├── persistence/
│   │   │       │   ├── entity/
│   │   │       │   │   └── TransactionEntity.java
│   │   │       │   └── repository/
│   │   │       │       └── TransactionJpaRepository.java
│   │   │       ├── report/
│   │   │       │   ├── PdfReportGenerator.java
│   │   │       │   └── ExcelReportGenerator.java
│   │   │       └── security/
│   │   │           ├── JwtService.java
│   │   │           ├── JwtAuthenticationFilter.java
│   │   │           └── SecurityConfig.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       └── db/migration/
│   │           ├── V1__create_transactions_table.sql
│   │           └── V2__add_indexes.sql
│   └── test/
│       └── java/
├── Dockerfile
└── pom.xml
```

---

## 4. Modelo de Dados

### 4.1 Tabela: users (ms-user)

| Campo | Tipo | Constraints | Descrição |
|-------|------|-------------|-----------|
| id | UUID | PK | Identificador único |
| name | VARCHAR(255) | NOT NULL | Nome completo |
| email | VARCHAR(255) | UNIQUE, NOT NULL | Email do usuário |
| cpf | VARCHAR(11) | UNIQUE, NOT NULL | CPF (apenas números) |
| password_hash | VARCHAR(255) | NOT NULL | Hash BCrypt |
| created_at | TIMESTAMP | DEFAULT NOW() | Data de criação |
| updated_at | TIMESTAMP | DEFAULT NOW() | Data de atualização |
| active | BOOLEAN | DEFAULT true | Soft delete |

**Índices:**
- `idx_users_email` (email)
- `idx_users_cpf` (cpf)

### 4.2 Tabela: transactions (ms-transaction)

| Campo | Tipo | Constraints | Descrição |
|-------|------|-------------|-----------|
| id | UUID | PK | Identificador único |
| user_id | UUID | NOT NULL, INDEX | ID do usuário (ms-user) |
| amount | DECIMAL(15,2) | NOT NULL | Valor da transação |
| original_amount | DECIMAL(15,2) | NULLABLE | Valor original (se convertido) |
| currency | VARCHAR(3) | DEFAULT 'BRL' | Moeda (BRL, USD, EUR) |
| original_currency | VARCHAR(3) | NULLABLE | Moeda original (se convertido) |
| exchange_rate | DECIMAL(10,6) | NULLABLE | Taxa de câmbio usada |
| category | VARCHAR(50) | NOT NULL | Categoria da transação |
| type | VARCHAR(10) | NOT NULL | INCOME ou EXPENSE |
| status | VARCHAR(20) | DEFAULT 'PENDING' | Status do processamento |
| description | TEXT | NULLABLE | Descrição opcional |
| transaction_date | DATE | NOT NULL | Data da transação |
| created_at | TIMESTAMP | DEFAULT NOW() | Data de criação |
| updated_at | TIMESTAMP | DEFAULT NOW() | Data de atualização |
| processed_at | TIMESTAMP | NULLABLE | Data do processamento |
| rejection_reason | TEXT | NULLABLE | Motivo da rejeição (se rejeitada) |

**Índices:**
- `idx_transactions_user_id` (user_id)
- `idx_transactions_status` (status)
- `idx_transactions_date` (transaction_date)
- `idx_transactions_user_date` (user_id, transaction_date)

### 4.3 Enums

**Category:**
```java
public enum Category {
    SALARY,      // Salário
    FREELANCE,   // Trabalho freelance
    INVESTMENT,  // Investimentos
    FOOD,        // Alimentação
    TRANSPORT,   // Transporte
    HEALTH,      // Saúde
    ENTERTAINMENT, // Entretenimento
    SHOPPING,    // Compras
    BILLS,       // Contas (luz, água, internet)
    EDUCATION,   // Educação
    OTHER        // Outros
}
```

**TransactionType:**
```java
public enum TransactionType {
    INCOME,   // Entrada (receita)
    EXPENSE   // Saída (despesa)
}
```

**TransactionStatus:**
```java
public enum TransactionStatus {
    PENDING,   // Aguardando processamento
    APPROVED,  // Aprovada
    REJECTED   // Rejeitada
}
```

---

## 5. Validação de Inputs (Bean Validation)

### 5.1 Dependência

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

### 5.2 UserRequest.java

```java
public record UserRequest(
    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 3, max = 255, message = "Nome deve ter entre 3 e 255 caracteres")
    String name,

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ser válido")
    String email,

    @NotBlank(message = "CPF é obrigatório")
    @Pattern(regexp = "\\d{11}", message = "CPF deve conter exatamente 11 dígitos")
    @CPF(message = "CPF inválido")  // Validador customizado ou usar Caelum Stella
    String cpf,

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
        message = "Senha deve conter: maiúscula, minúscula, número e caractere especial"
    )
    String password
) {}
```

### 5.3 TransactionRequest.java

```java
public record TransactionRequest(
    @NotNull(message = "Valor é obrigatório")
    @Positive(message = "Valor deve ser positivo")
    @Digits(integer = 13, fraction = 2, message = "Valor inválido")
    BigDecimal amount,

    @NotBlank(message = "Moeda é obrigatória")
    @Pattern(regexp = "^(BRL|USD|EUR)$", message = "Moeda deve ser BRL, USD ou EUR")
    String currency,

    @NotNull(message = "Categoria é obrigatória")
    Category category,

    @NotNull(message = "Tipo é obrigatório")
    TransactionType type,

    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    String description,

    @NotNull(message = "Data da transação é obrigatória")
    @PastOrPresent(message = "Data não pode ser futura")
    LocalDate transactionDate
) {}
```

### 5.4 LoginRequest.java

```java
public record LoginRequest(
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ser válido")
    String email,

    @NotBlank(message = "Senha é obrigatória")
    String password
) {}
```

### 5.5 Tratamento de Erros de Validação

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );
        
        ErrorResponse response = new ErrorResponse(
            "VALIDATION_ERROR",
            "Erro de validação nos campos",
            errors
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        ErrorResponse response = new ErrorResponse(
            ex.getCode(),
            ex.getMessage(),
            null
        );
        return ResponseEntity.status(ex.getStatus()).body(response);
    }
}

public record ErrorResponse(
    String code,
    String message,
    Map<String, String> fieldErrors
) {}
```

---

## 6. Endpoints da API

### 6.1 ms-user (porta 8081)

#### Autenticação (Público)

| Método | Endpoint | Descrição | Request Body | Response |
|--------|----------|-----------|--------------|----------|
| POST | `/api/auth/register` | Registra novo usuário | UserRequest | UserResponse (201) |
| POST | `/api/auth/login` | Autenticar e obter token | LoginRequest | LoginResponse (200) |

#### Usuários (Autenticado)

| Método | Endpoint | Descrição | Request Body | Response |
|--------|----------|-----------|--------------|----------|
| POST | `/api/users` | Cria usuário | UserRequest | UserResponse (201) |
| GET | `/api/users/{id}` | Busca por ID | - | UserResponse (200) |
| GET | `/api/users/me` | Dados do usuário logado | - | UserResponse (200) |
| PUT | `/api/users/{id}` | Atualiza usuário | UserUpdateRequest | UserResponse (200) |
| DELETE | `/api/users/{id}` | Remove (soft delete) | - | 204 No Content |
| POST | `/api/users/import` | Importa via Excel | MultipartFile | ImportResponse (200) |

### 6.2 ms-transaction (porta 8082)

#### Transações (Autenticado)

| Método | Endpoint | Descrição | Request Body | Response |
|--------|----------|-----------|--------------|----------|
| POST | `/api/transactions` | Cria transação | TransactionRequest | TransactionResponse (202) |
| GET | `/api/transactions` | Lista paginada | Query params | Page<TransactionResponse> |
| GET | `/api/transactions/{id}` | Busca por ID | - | TransactionResponse (200) |
| PUT | `/api/transactions/{id}` | Atualiza | TransactionRequest | TransactionResponse (200) |
| DELETE | `/api/transactions/{id}` | Remove | - | 204 No Content |

#### Análise de Despesas (Autenticado)

| Método | Endpoint | Descrição | Response |
|--------|----------|-----------|----------|
| GET | `/api/transactions/balance` | Saldo geral | BalanceResponse |
| GET | `/api/transactions/balance/daily` | Saldo por dia | List<DailyBalanceResponse> |
| GET | `/api/transactions/balance/monthly` | Saldo por mês | List<MonthlyBalanceResponse> |
| GET | `/api/transactions/balance/category` | Saldo por categoria | Map<Category, BigDecimal> |

#### Câmbio (Autenticado)

| Método | Endpoint | Descrição | Response |
|--------|----------|-----------|----------|
| GET | `/api/exchange/{currency}` | Cotação atual | ExchangeResponse |
| GET | `/api/exchange/convert?from=USD&to=BRL&amount=100` | Conversão | ConversionResponse |

#### Relatórios (Autenticado)

| Método | Endpoint | Descrição | Response |
|--------|----------|-----------|----------|
| GET | `/api/reports/transactions/pdf` | Relatório PDF | application/pdf |
| GET | `/api/reports/transactions/excel` | Relatório Excel | application/vnd.openxmlformats... |

---

## 7. Fluxo de Transações (Kafka)

### 7.1 Diagrama de Sequência Completo

```
┌────────┐     ┌──────────────────┐     ┌───────────────┐     ┌────────────────────┐     ┌────────────────┐     ┌─────────────┐
│Cliente │     │MS2-Transaction API│    │Transaction DB │     │Kafka               │     │MS2-Processor   │     │MockAPI      │
└───┬────┘     └────────┬─────────┘     └───────┬───────┘     │(transaction.requested)   └───────┬────────┘     └──────┬──────┘
    │                   │                       │             └─────────┬──────────┘            │                     │
    │ POST /transactions│                       │                       │                       │                     │
    │ (JWT + payload)   │                       │                       │                       │                     │
    ├──────────────────►│                       │                       │                       │                     │
    │                   │                       │                       │                       │                     │
    │                   │ valida JWT            │                       │                       │                     │
    │                   │ valida payload        │                       │                       │                     │
    │                   │ valida regras simples │                       │                       │                     │
    │                   │                       │                       │                       │                     │
    │                   │ INSERT (PENDING)      │                       │                       │                     │
    │                   ├──────────────────────►│                       │                       │                     │
    │                   │                       │                       │                       │                     │
    │                   │ publica evento        │                       │                       │                     │
    │                   ├───────────────────────┼──────────────────────►│                       │                     │
    │                   │                       │                       │                       │                     │
    │  202 Accepted     │                       │                       │                       │                     │
    │  + transactionId  │                       │                       │                       │                     │
    │◄──────────────────┤                       │                       │                       │                     │
    │                   │                       │                       │                       │                     │
    │                   │                       │                       │ consome evento        │                     │
    │                   │                       │                       ├──────────────────────►│                     │
    │                   │                       │                       │                       │                     │
    │                   │                       │                       │                       │ GET /accounts/{id}  │
    │                   │                       │                       │                       │ (saldo/limites)     │
    │                   │                       │                       │                       ├────────────────────►│
    │                   │                       │                       │                       │                     │
    │                   │                       │                       │                       │ {balance, limits}   │
    │                   │                       │                       │                       │◄────────────────────┤
    │                   │                       │                       │                       │                     │
    │                   │                       │                       │                       │ aplica regras:      │
    │                   │                       │                       │                       │ - saldo suficiente? │
    │                   │                       │                       │                       │ - dentro do limite? │
    │                   │                       │                       │                       │                     │
    │                   │                       │ UPDATE status         │                       │                     │
    │                   │                       │ (APPROVED/REJECTED)   │                       │                     │
    │                   │                       │◄──────────────────────┼───────────────────────┤                     │
    │                   │                       │                       │                       │                     │
    │                   │                       │                       │                       │                     │
    │ GET /transactions/{id}                    │                       │                       │                     │
    ├──────────────────►│                       │                       │                       │                     │
    │                   │ SELECT                │                       │                       │                     │
    │                   ├──────────────────────►│                       │                       │                     │
    │                   │                       │                       │                       │                     │
    │  200 OK           │◄──────────────────────┤                       │                       │                     │
    │  (status final)   │                       │                       │                       │                     │
    │◄──────────────────┤                       │                       │                       │                     │
```

### 7.2 TransactionRequestedEvent

```java
public record TransactionRequestedEvent(
    UUID transactionId,
    UUID userId,
    BigDecimal amount,
    String currency,
    TransactionType type,
    Category category,
    LocalDateTime createdAt
) {}
```

### 7.3 TransactionEventConsumer

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventConsumer {

    private final TransactionService transactionService;
    private final MockApiClient mockApiClient;
    private final DlqProducer dlqProducer;

    @KafkaListener(topics = "transaction.requested", groupId = "transaction-processor")
    public void consume(TransactionRequestedEvent event) {
        log.info("Processando transação: {}", event.transactionId());
        
        try {
            // 1. Buscar saldo e limites no MockAPI
            AccountBalanceResponse account = mockApiClient.getAccountBalance(event.userId());
            
            // 2. Aplicar regras de negócio
            boolean approved = validateTransaction(event, account);
            
            // 3. Atualizar status
            if (approved) {
                transactionService.approve(event.transactionId());
                log.info("Transação APROVADA: {}", event.transactionId());
            } else {
                transactionService.reject(event.transactionId(), "Saldo insuficiente ou limite excedido");
                log.info("Transação REJEITADA: {}", event.transactionId());
            }
            
        } catch (Exception e) {
            log.error("Erro ao processar transação: {}", event.transactionId(), e);
            dlqProducer.send(event, e.getMessage());
        }
    }

    private boolean validateTransaction(TransactionRequestedEvent event, AccountBalanceResponse account) {
        // Se for INCOME, sempre aprova
        if (event.type() == TransactionType.INCOME) {
            return true;
        }
        
        // Se for EXPENSE, valida saldo e limite
        BigDecimal balance = account.balance();
        BigDecimal dailyLimit = account.dailyLimit();
        BigDecimal dailySpent = account.dailySpent();
        
        boolean hasSufficientBalance = balance.compareTo(event.amount()) >= 0;
        boolean withinDailyLimit = dailySpent.add(event.amount()).compareTo(dailyLimit) <= 0;
        
        return hasSufficientBalance && withinDailyLimit;
    }
}
```

### 7.4 DlqProducer (Dead Letter Queue)

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class DlqProducer {

    private final KafkaTemplate<String, DlqMessage> kafkaTemplate;

    public void send(TransactionRequestedEvent event, String errorMessage) {
        DlqMessage dlqMessage = new DlqMessage(
            event,
            errorMessage,
            LocalDateTime.now(),
            0 // retry count
        );
        
        kafkaTemplate.send("transaction.dlq", event.transactionId().toString(), dlqMessage);
        log.warn("Mensagem enviada para DLQ: {}", event.transactionId());
    }
}

public record DlqMessage(
    TransactionRequestedEvent originalEvent,
    String errorMessage,
    LocalDateTime failedAt,
    int retryCount
) {}
```

---

## 8. Integrações Externas

### 8.1 BrasilAPI (Câmbio)

**URL Base:** `https://brasilapi.com.br/api/`

**Endpoint:** `GET /cotacao/v1/{moeda}`

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class BrasilApiClient {

    private final RestTemplate restTemplate;

    @Value("${integration.brasilapi.base-url}")
    private String baseUrl;

    @Cacheable(value = "exchange-rates", key = "#currency", unless = "#result == null")
    public ExchangeRateResponse getExchangeRate(String currency) {
        String url = baseUrl + "/cotacao/v1/" + currency;
        
        try {
            ResponseEntity<BrasilApiResponse> response = restTemplate.getForEntity(url, BrasilApiResponse.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                BrasilApiResponse body = response.getBody();
                return new ExchangeRateResponse(
                    body.name(),
                    body.bid(),    // Valor de compra
                    body.ask(),    // Valor de venda
                    body.timestamp()
                );
            }
            throw new IntegrationException("Erro ao buscar cotação");
            
        } catch (RestClientException e) {
            log.error("Erro na integração com BrasilAPI: {}", e.getMessage());
            throw new IntegrationException("Serviço de câmbio indisponível", e);
        }
    }
}

public record ExchangeRateResponse(
    String currency,
    BigDecimal buyRate,
    BigDecimal sellRate,
    LocalDateTime timestamp
) {}
```

**Configuração de Cache (1 hora):**

```java
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("exchange-rates");
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .maximumSize(100));
        return cacheManager;
    }
}
```

### 8.2 MockAPI (Saldo de Conta Bancária)

**Criar conta em:** `https://mockapi.io/`

**Endpoint:** `GET /api/v1/accounts/{userId}`

**Estrutura do Mock:**

```json
{
  "id": "uuid-do-usuario",
  "balance": 5000.00,
  "dailyLimit": 1000.00,
  "dailySpent": 250.00,
  "monthlyLimit": 10000.00,
  "monthlySpent": 3500.00,
  "accountStatus": "ACTIVE"
}
```

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class MockApiClient {

    private final RestTemplate restTemplate;

    @Value("${integration.mockapi.base-url}")
    private String baseUrl;

    public AccountBalanceResponse getAccountBalance(UUID userId) {
        String url = baseUrl + "/accounts/" + userId;
        
        try {
            ResponseEntity<AccountBalanceResponse> response = 
                restTemplate.getForEntity(url, AccountBalanceResponse.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            
            // Se não encontrar, retorna conta padrão (para não bloquear)
            return getDefaultAccount(userId);
            
        } catch (RestClientException e) {
            log.warn("MockAPI indisponível, usando valores padrão: {}", e.getMessage());
            return getDefaultAccount(userId);
        }
    }

    private AccountBalanceResponse getDefaultAccount(UUID userId) {
        return new AccountBalanceResponse(
            userId,
            new BigDecimal("10000.00"),  // saldo padrão
            new BigDecimal("5000.00"),   // limite diário
            BigDecimal.ZERO,              // gasto diário
            new BigDecimal("50000.00"),  // limite mensal
            BigDecimal.ZERO,              // gasto mensal
            "ACTIVE"
        );
    }
}

public record AccountBalanceResponse(
    UUID userId,
    BigDecimal balance,
    BigDecimal dailyLimit,
    BigDecimal dailySpent,
    BigDecimal monthlyLimit,
    BigDecimal monthlySpent,
    String accountStatus
) {}
```

### 8.3 Configuração (application.yml)

```yaml
integration:
  brasilapi:
    base-url: https://brasilapi.com.br/api
    timeout: 5000
  mockapi:
    base-url: https://your-project.mockapi.io/api/v1
    timeout: 5000
```

---

## 9. Relatórios (Download PDF/Excel)

### 9.1 ReportController

```java
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Geração de relatórios")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/transactions/pdf")
    @Operation(summary = "Download relatório PDF das transações")
    public ResponseEntity<byte[]> downloadPdf(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal UUID userId) {
        
        byte[] pdfBytes = reportService.generatePdfReport(userId, startDate, endDate);
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=transacoes.pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdfBytes);
    }

    @GetMapping("/transactions/excel")
    @Operation(summary = "Download relatório Excel das transações")
    public ResponseEntity<byte[]> downloadExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal UUID userId) {
        
        byte[] excelBytes = reportService.generateExcelReport(userId, startDate, endDate);
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=transacoes.xlsx")
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .body(excelBytes);
    }
}
```

### 9.2 PdfReportGenerator (usando iText ou OpenPDF)

```java
@Component
@RequiredArgsConstructor
public class PdfReportGenerator {

    public byte[] generate(List<Transaction> transactions, BalanceResponse balance) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();
            
            // Título
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph("Relatório de Transações", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));
            
            // Resumo
            document.add(new Paragraph("Resumo Financeiro"));
            document.add(new Paragraph("Total de Receitas: R$ " + balance.totalIncome()));
            document.add(new Paragraph("Total de Despesas: R$ " + balance.totalExpense()));
            document.add(new Paragraph("Saldo: R$ " + balance.balance()));
            document.add(new Paragraph(" "));
            
            // Tabela de transações
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.addCell("Data");
            table.addCell("Categoria");
            table.addCell("Tipo");
            table.addCell("Descrição");
            table.addCell("Valor");
            
            for (Transaction t : transactions) {
                table.addCell(t.getTransactionDate().toString());
                table.addCell(t.getCategory().name());
                table.addCell(t.getType().name());
                table.addCell(t.getDescription() != null ? t.getDescription() : "-");
                table.addCell("R$ " + t.getAmount());
            }
            
            document.add(table);
            document.close();
            
            return baos.toByteArray();
            
        } catch (Exception e) {
            throw new ReportGenerationException("Erro ao gerar PDF", e);
        }
    }
}
```

### 9.3 ExcelReportGenerator (usando Apache POI)

```java
@Component
@RequiredArgsConstructor
public class ExcelReportGenerator {

    public byte[] generate(List<Transaction> transactions, BalanceResponse balance) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("Transações");
            
            // Estilo para cabeçalho
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            // Cabeçalho
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Data", "Categoria", "Tipo", "Descrição", "Valor", "Status"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Dados
            int rowNum = 1;
            for (Transaction t : transactions) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(t.getTransactionDate().toString());
                row.createCell(1).setCellValue(t.getCategory().name());
                row.createCell(2).setCellValue(t.getType().name());
                row.createCell(3).setCellValue(t.getDescription() != null ? t.getDescription() : "");
                row.createCell(4).setCellValue(t.getAmount().doubleValue());
                row.createCell(5).setCellValue(t.getStatus().name());
            }
            
            // Auto-size colunas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Resumo em nova aba
            Sheet summarySheet = workbook.createSheet("Resumo");
            Row r1 = summarySheet.createRow(0);
            r1.createCell(0).setCellValue("Total Receitas");
            r1.createCell(1).setCellValue(balance.totalIncome().doubleValue());
            
            Row r2 = summarySheet.createRow(1);
            r2.createCell(0).setCellValue("Total Despesas");
            r2.createCell(1).setCellValue(balance.totalExpense().doubleValue());
            
            Row r3 = summarySheet.createRow(2);
            r3.createCell(0).setCellValue("Saldo");
            r3.createCell(1).setCellValue(balance.balance().doubleValue());
            
            workbook.write(baos);
            return baos.toByteArray();
            
        } catch (Exception e) {
            throw new ReportGenerationException("Erro ao gerar Excel", e);
        }
    }
}
```

---

## 10. Spring Security + JWT

### 10.1 SecurityConfig (ms-user)

```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authorize -> authorize
                // Endpoints públicos
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
                // Swagger
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // Actuator health
                .requestMatchers("/actuator/health").permitAll()
                // Todo o resto exige autenticação
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### 10.2 JwtService

```java
@Component
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(UUID userId, String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
            .subject(userId.toString())
            .claim("email", email)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secretKey)
            .compact();
    }

    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public UUID getUserIdFromToken(String token) {
        return UUID.fromString(extractAllClaims(token).getSubject());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
```

### 10.3 JwtAuthenticationFilter

```java
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            if (jwtService.validateToken(token)) {
                UUID userId = jwtService.getUserIdFromToken(token);
                
                var authentication = new UsernamePasswordAuthenticationToken(
                    userId,
                    null,
                    List.of()
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}
```

---

## 11. Stack Tecnológico

| Tecnologia | Versão | Finalidade |
|------------|--------|------------|
| Java | 21 | Linguagem (LTS) |
| Spring Boot | 4.0.1 | Framework backend |
| Spring Data JPA | 4.0.x | ORM e acesso a dados |
| Spring Security | 7.0.x | Autenticação/Autorização |
| Spring Validation | 4.0.x | Bean Validation |
| Spring Cache | 4.0.x | Cache (Caffeine) |
| PostgreSQL | 18.x | Banco de dados |
| Flyway | 10.x | Migrations de banco |
| Apache Kafka | 4.0 | Mensageria assíncrona |
| JWT (jjwt) | 0.12.6 | Tokens de autenticação |
| Apache POI | 5.5.x | Leitura/escrita Excel |
| OpenPDF | 2.0.x | Geração de PDF |
| Caffeine | 3.x | Cache local |
| Springdoc OpenAPI | 3.0.0 | Documentação Swagger |
| Docker | - | Containerização |
| JUnit 6 | 6.0.x | Testes unitários |
| Mockito | 5.x | Mocks em testes |
| Testcontainers | 1.19.x | Testes de integração |

---

## 12. Docker e Produção

### 12.1 Dockerfile (ms-user)

```dockerfile
# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Criar usuário não-root
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 12.2 Dockerfile (ms-transaction)

```dockerfile
# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 12.3 docker-compose.yml (Completo para Produção)

```yaml
version: '3.8'

services:
  # ============ BANCO DE DADOS ============
  postgres:
    image: postgres:16-alpine
    container_name: julius-postgres
    environment:
      POSTGRES_USER: ${DB_USER:-julius}
      POSTGRES_PASSWORD: ${DB_PASSWORD:-julius123}
      POSTGRES_DB: ${DB_NAME:-julius_db}
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U julius"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - julius-network

  # ============ KAFKA ============
  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0
    container_name: julius-zookeeper
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    networks:
      - julius-network

  kafka:
    image: confluentinc/cp-kafka:7.5.0
    container_name: julius-kafka
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:29092,PLAINTEXT_HOST://localhost:9092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_AUTO_CREATE_TOPICS_ENABLE: "true"
    healthcheck:
      test: ["CMD", "kafka-broker-api-versions", "--bootstrap-server", "localhost:9092"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - julius-network

  # ============ MICROSSERVIÇOS ============
  ms-user:
    build:
      context: ./ms-user
      dockerfile: Dockerfile
    container_name: julius-ms-user
    depends_on:
      postgres:
        condition: service_healthy
    ports:
      - "8081:8081"
    environment:
      SPRING_PROFILES_ACTIVE: prod
      DB_HOST: postgres
      DB_PORT: 5432
      DB_NAME: julius_db
      DB_USER: julius
      DB_PASSWORD: julius123
      JWT_SECRET: ${JWT_SECRET:-sua-chave-secreta-muito-grande-pelo-menos-256-bits-para-producao}
    networks:
      - julius-network

  ms-transaction:
    build:
      context: ./ms-transaction
      dockerfile: Dockerfile
    container_name: julius-ms-transaction
    depends_on:
      postgres:
        condition: service_healthy
      kafka:
        condition: service_healthy
    ports:
      - "8082:8082"
    environment:
      SPRING_PROFILES_ACTIVE: prod
      DB_HOST: postgres
      DB_PORT: 5432
      DB_NAME: julius_db
      DB_USER: julius
      DB_PASSWORD: julius123
      KAFKA_BOOTSTRAP_SERVERS: kafka:29092
      JWT_SECRET: ${JWT_SECRET:-sua-chave-secreta-muito-grande-pelo-menos-256-bits-para-producao}
      MOCKAPI_URL: ${MOCKAPI_URL:-https://your-project.mockapi.io/api/v1}
    networks:
      - julius-network

  # ============ FERRAMENTAS DE DESENVOLVIMENTO ============
  pgadmin:
    image: dpage/pgadmin4:latest
    container_name: julius-pgadmin
    environment:
      PGADMIN_DEFAULT_EMAIL: admin@julius.com
      PGADMIN_DEFAULT_PASSWORD: admin123
    ports:
      - "5050:80"
    depends_on:
      - postgres
    networks:
      - julius-network
    profiles:
      - dev

  kafdrop:
    image: obsidiandynamics/kafdrop:latest
    container_name: julius-kafdrop
    depends_on:
      - kafka
    ports:
      - "19000:9000"
    environment:
      KAFKA_BROKERCONNECT: kafka:29092
    networks:
      - julius-network
    profiles:
      - dev

volumes:
  postgres_data:

networks:
  julius-network:
    driver: bridge
```

### 12.4 Comandos Docker

```bash
# Desenvolvimento (com pgAdmin e Kafdrop)
docker-compose --profile dev up -d

# Produção (apenas serviços essenciais)
docker-compose up -d

# Ver logs de todos os serviços
docker-compose logs -f

# Ver logs de um serviço específico
docker-compose logs -f ms-transaction

# Rebuild após alterações
docker-compose up -d --build

# Parar tudo
docker-compose down

# Parar e remover volumes (CUIDADO: apaga dados)
docker-compose down -v
```

### 12.5 application-prod.yml (ms-user)

```yaml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
    username: ${DB_USER}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
  jpa:
    hibernate:
      ddl-auto: validate  # Em prod, usa Flyway
    show-sql: false
  flyway:
    enabled: true
    locations: classpath:db/migration

jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000

logging:
  level:
    root: WARN
    nttdata.personal.julius: INFO
```

### 12.6 application-prod.yml (ms-transaction)

```yaml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
    username: ${DB_USER}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
  flyway:
    enabled: true
    locations: classpath:db/migration
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS}
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
    consumer:
      group-id: transaction-processor
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: nttdata.personal.julius.api.infrastructure.messaging

jwt:
  secret: ${JWT_SECRET}

integration:
  brasilapi:
    base-url: https://brasilapi.com.br/api
    timeout: 5000
  mockapi:
    base-url: ${MOCKAPI_URL}
    timeout: 5000

logging:
  level:
    root: WARN
    nttdata.personal.julius: INFO
```

---

## 13. Flyway (Migrations)

### 13.1 Dependência

```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
</dependency>
```

### 13.2 V1__create_users_table.sql (ms-user)

```sql
CREATE TABLE users (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    cpf VARCHAR(11) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    active BOOLEAN DEFAULT TRUE
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_cpf ON users(cpf);
CREATE INDEX idx_users_active ON users(active);
```

### 13.3 V1__create_transactions_table.sql (ms-transaction)

```sql
CREATE TABLE transactions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    original_amount DECIMAL(15,2),
    currency VARCHAR(3) DEFAULT 'BRL',
    original_currency VARCHAR(3),
    exchange_rate DECIMAL(10,6),
    category VARCHAR(50) NOT NULL,
    type VARCHAR(10) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    description TEXT,
    transaction_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    rejection_reason TEXT
);

CREATE INDEX idx_transactions_user_id ON transactions(user_id);
CREATE INDEX idx_transactions_status ON transactions(status);
CREATE INDEX idx_transactions_date ON transactions(transaction_date);
CREATE INDEX idx_transactions_user_date ON transactions(user_id, transaction_date);
CREATE INDEX idx_transactions_type ON transactions(type);
CREATE INDEX idx_transactions_category ON transactions(category);
```

### 13.4 V2__add_audit_columns.sql

```sql
-- Adicionar colunas de auditoria se necessário
ALTER TABLE users ADD COLUMN IF NOT EXISTS created_by UUID;
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS created_by UUID;
```

---

## 14. Testes

### 14.1 Estrutura de Testes

```
src/test/java/
├── unit/
│   ├── domain/
│   │   ├── UserTest.java
│   │   └── TransactionTest.java
│   ├── application/
│   │   ├── UserServiceTest.java
│   │   └── TransactionServiceTest.java
│   └── infrastructure/
│       └── JwtServiceTest.java
├── integration/
│   ├── UserControllerIntegrationTest.java
│   ├── TransactionControllerIntegrationTest.java
│   └── KafkaIntegrationTest.java
└── e2e/
    └── FullFlowE2ETest.java
```

### 14.2 Exemplo de Teste Unitário

```java
@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    
    @Mock
    private TransactionEventProducer eventProducer;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    @DisplayName("Deve criar transação com status PENDING")
    void shouldCreateTransactionWithPendingStatus() {
        // Arrange
        UUID userId = UUID.randomUUID();
        TransactionRequestDto request = new TransactionRequestDto(
            new BigDecimal("100.00"),
            "BRL",
            Category.FOOD,
            TransactionType.EXPENSE,
            "Almoço",
            LocalDate.now()
        );
        
        Transaction savedTransaction = Transaction.builder()
            .id(UUID.randomUUID())
            .userId(userId)
            .amount(request.amount())
            .status(TransactionStatus.PENDING)
            .build();
            
        when(transactionRepository.save(any())).thenReturn(savedTransaction);
        
        // Act
        TransactionResponseDto response = transactionService.create(userId, request);
        
        // Assert
        assertThat(response.status()).isEqualTo(TransactionStatus.PENDING);
        verify(eventProducer).publish(any(TransactionRequestedEvent.class));
    }
}
```

### 14.3 Teste de Integração com Kafka

```java
@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"transaction.requested"})
class KafkaIntegrationTest {

    @Autowired
    private TransactionEventProducer producer;

    @Autowired
    private KafkaTemplate<String, TransactionRequestedEvent> kafkaTemplate;

    @Test
    @DisplayName("Deve publicar evento no Kafka")
    void shouldPublishEventToKafka() {
        // Arrange
        TransactionRequestedEvent event = new TransactionRequestedEvent(
            UUID.randomUUID(),
            UUID.randomUUID(),
            new BigDecimal("100.00"),
            "BRL",
            TransactionType.EXPENSE,
            Category.FOOD,
            LocalDateTime.now()
        );

        // Act & Assert
        assertDoesNotThrow(() -> producer.publish(event));
    }
}
```

### 14.4 Cobertura de Código (JaCoCo)

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
        <execution>
            <id>check</id>
            <goals>
                <goal>check</goal>
            </goals>
            <configuration>
                <rules>
                    <rule>
                        <element>BUNDLE</element>
                        <limits>
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.80</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

---

## 15. O Que Falta Implementar

### Prioridade Alta (Essencial para Demo)

| # | Tarefa | MS | Arquivo |
|---|--------|-----|---------|
| 1 | Completar JwtAuthenticationFilter | ms-user | JwtAuthenticationFilter.java |
| 2 | Registrar filtro no SecurityConfig | ms-user | SecurityConfig.java |
| 3 | Copiar JwtService para ms-transaction | ms-transaction | JwtService.java |
| 4 | Criar SecurityConfig | ms-transaction | SecurityConfig.java |
| 5 | Implementar TransactionEventConsumer | ms-transaction | TransactionEventConsumer.java |
| 6 | Implementar DlqProducer | ms-transaction | DlqProducer.java |

### Prioridade Média (Diferenciais)

| # | Tarefa | MS | Arquivo |
|---|--------|-----|---------|
| 7 | Criar MockApiClient | ms-transaction | MockApiClient.java |
| 8 | Criar BrasilApiClient | ms-transaction | BrasilApiClient.java |
| 9 | Implementar ReportController | ms-transaction | ReportController.java |
| 10 | Implementar PdfReportGenerator | ms-transaction | PdfReportGenerator.java |
| 11 | Implementar ExcelReportGenerator | ms-transaction | ExcelReportGenerator.java |
| 12 | Implementar ExcelImportService | ms-user | ExcelImportService.java |
| 13 | Criar BalanceService | ms-transaction | BalanceService.java |

### Prioridade Baixa (Documentação e Qualidade)

| # | Tarefa |
|---|--------|
| 14 | Configurar OpenAPI em ambos MS |
| 15 | Criar migrations Flyway |
| 16 | Escrever testes unitários (>80%) |
| 17 | Escrever README.md |
| 18 | Configurar Dockerfiles |
| 19 | Testar docker-compose completo |

---

## 16. Entregáveis

- [ ] Código-fonte completo no GitHub (branch: dev/refactor)
- [ ] docker-compose.yml configurado (dev + prod)
- [ ] Dockerfiles para cada microsserviço
- [ ] Migrations Flyway
- [ ] example-users-import.xlsx (planilha modelo)
- [ ] README.md profissional
- [ ] README_TESTS.md (documentação de testes)
- [ ] OpenAPI/Swagger acessível em cada microsserviço
- [ ] Testes unitários com cobertura >80%
- [ ] Demonstração funcional com APIs mock e públicas
- [ ] Roteiro de apresentação

---

## 17. Métricas de Sucesso

| Critério | Meta | Como Validar |
|----------|------|--------------|
| Cobertura de testes | >80% | `mvn jacoco:report` |
| Endpoints funcionais | 100% | Testar via Insomnia |
| Kafka funcionando | ✓ | Ver mensagens no Kafdrop |
| JWT funcionando | ✓ | Login retorna token válido |
| BrasilAPI integrada | ✓ | GET /api/exchange/USD retorna cotação |
| MockAPI integrada | ✓ | Consumer consulta saldo |
| Relatórios funcionando | ✓ | Download PDF/Excel |
| Import Excel funcionando | ✓ | Upload planilha cria usuários |
| Microsserviços independentes | ✓ | Cada um roda separadamente |
| Docker Compose | ✓ | `docker-compose up -d` sobe tudo |
| Validações funcionando | ✓ | Request inválido retorna 400 |

---

## 18. Comandos Úteis

```bash
# ============ DESENVOLVIMENTO ============

# Clonar e acessar
git clone https://github.com/imagalhaess/personal.julius.api.git
cd personal.julius.api
git checkout dev/refactor

# Subir infraestrutura (com ferramentas dev)
docker-compose --profile dev up -d

# Rodar ms-user
cd ms-user && ./mvnw spring-boot:run

# Rodar ms-transaction (outro terminal)
cd ms-transaction && ./mvnw spring-boot:run

# Rodar testes
./mvnw test

# Ver cobertura
./mvnw jacoco:report
# Abrir: target/site/jacoco/index.html

# ============ PRODUÇÃO ============

# Build das imagens
docker-compose build

# Subir em produção
docker-compose up -d

# Ver logs
docker-compose logs -f ms-user ms-transaction

# ============ URLS ============

# ms-user
http://localhost:8081/swagger-ui.html

# ms-transaction  
http://localhost:8082/swagger-ui.html

# pgAdmin (dev)
http://localhost:5050

# Kafdrop (dev)
http://localhost:19000
```

---

*Personal Julius API - BECA Challenge 2026*  
*Versão 2.0 - Documentação Completa*
