# Fluxo da Aplicação Julius API

## Visao Geral da Arquitetura

A aplicacao Julius API eh composta por **3 microsservicos** que se comunicam de forma **assincrona via Apache Kafka** e **sincrona via REST**:

```
┌─────────────────┐     ┌─────────────────────┐     ┌─────────────────┐
│    MS-USER      │     │   MS-TRANSACTION    │     │   MS-PROCESSOR  │
│   (porta 8081)  │     │    (porta 8082)     │     │   (porta 8080)  │
└────────┬────────┘     └──────────┬──────────┘     └────────┬────────┘
         │                         │                          │
         │                         │    Kafka                 │
         │                         ├──────────────────────────┤
         │                         │  transaction-events      │
         │                         │  transaction-processed   │
         │                         │  transaction-dlq         │
         │                         │                          │
         ▼                         ▼                          ▼
   ┌──────────┐             ┌──────────┐              ┌──────────────┐
   │PostgreSQL│             │PostgreSQL│              │  APIs Externas│
   │ user_db  │             │ trans_db │              │  (MockAPI,   │
   │ (5432)   │             │ (5433)   │              │  BrasilAPI)  │
   └──────────┘             └──────────┘              └──────────────┘
```

---

## 1. Fluxo de Autenticacao (MS-USER)

### 1.1 Registro de Usuario

```
Cliente HTTP                    MS-USER                         PostgreSQL
     │                             │                                 │
     │  POST /users                │                                 │
     │  {name, email, cpf, pass}   │                                 │
     │────────────────────────────>│                                 │
     │                             │                                 │
     │                             │  Validacoes:                    │
     │                             │  - @Valid (Bean Validation)     │
     │                             │  - Email unico?                 │
     │                             │  - CPF unico?                   │
     │                             │                                 │
     │                             │  INSERT INTO users              │
     │                             │────────────────────────────────>│
     │                             │                                 │
     │                             │  Gera UUID publico              │
     │                             │  Criptografa senha (BCrypt)     │
     │                             │                                 │
     │  201 Created                │                                 │
     │  {publicId, name, email}    │                                 │
     │<────────────────────────────│                                 │
```

**Endpoint:** `POST /users`

**Validacoes aplicadas:**
- Nome: 3-100 caracteres
- Email: formato valido, unico no sistema
- CPF: formato `000.000.000-00`, unico no sistema
- Senha: minimo 8 caracteres, maiuscula, minuscula, numero, caractere especial

**Codigo relevante:** `UserService.create()` em `ms-user/src/main/java/.../application/service/UserService.java:22-41`

---

### 1.2 Login (Geracao de Token JWT)

```
Cliente HTTP                    MS-USER                         PostgreSQL
     │                             │                                 │
     │  POST /auth/login           │                                 │
     │  {email, password}          │                                 │
     │────────────────────────────>│                                 │
     │                             │                                 │
     │                             │  SELECT * FROM users            │
     │                             │  WHERE email = ?                │
     │                             │────────────────────────────────>│
     │                             │                                 │
     │                             │  BCrypt.matches(password)?      │
     │                             │                                 │
     │                             │  TokenService.generateToken()   │
     │                             │  - Claims: userId, roles        │
     │                             │  - Expiracao: 24h               │
     │                             │                                 │
     │  200 OK                     │                                 │
     │  {token: "eyJhbG..."}       │                                 │
     │<────────────────────────────│                                 │
```

**Endpoint:** `POST /auth/login`

**Estrutura do Token JWT:**
```json
{
  "sub": "usuario@email.com",
  "userId": 1,
  "roles": ["ROLE_USER"],
  "iat": 1706550000,
  "exp": 1706636400
}
```

**Codigo relevante:** `AuthService.authenticate()` em `ms-user/src/main/java/.../application/service/AuthService.java`

---

### 1.3 Validacao de Token (Em todas as requisicoes autenticadas)

```
Cliente HTTP                    Qualquer MS
     │                             │
     │  GET /recurso               │
     │  Authorization: Bearer xxx  │
     │────────────────────────────>│
     │                             │
     │                             │  BaseSecurityConfig
     │                             │  .authenticateRequest()
     │                             │
     │                             │  1. Extrai token do header
     │                             │  2. JwtService.isTokenValid()
     │                             │  3. JwtService.extractUserId()
     │                             │  4. SecurityUtils.setAuthentication()
     │                             │
     │                             │  Se valido: prossegue
     │                             │  Se invalido: 401
```

**Codigo relevante:** `BaseSecurityConfig.authenticateRequest()` em `ms-common/src/main/java/.../config/BaseSecurityConfig.java:48-58`

---

## 2. Fluxo de Transacoes (MS-TRANSACTION + MS-PROCESSOR)

### 2.1 Criacao de Transacao (Fluxo Completo)

Este eh o fluxo mais complexo, envolvendo comunicacao assincrona:

```
┌──────────┐     ┌───────────────────┐     ┌─────────┐     ┌──────────────┐     ┌──────────┐
│  Cliente │     │  MS-TRANSACTION   │     │  KAFKA  │     │ MS-PROCESSOR │     │ APIs Ext │
└────┬─────┘     └─────────┬─────────┘     └────┬────┘     └──────┬───────┘     └────┬─────┘
     │                     │                    │                 │                  │
     │ POST /transactions  │                    │                 │                  │
     │ {amount, currency,  │                    │                 │                  │
     │  type, category,    │                    │                 │                  │
     │  description, origin}                    │                 │                  │
     │────────────────────>│                    │                 │                  │
     │                     │                    │                 │                  │
     │                     │ Validacoes         │                 │                  │
     │                     │ Persiste PENDING   │                 │                  │
     │                     │                    │                 │                  │
     │                     │ Publica evento     │                 │                  │
     │                     │───────────────────>│                 │                  │
     │                     │ transaction-events │                 │                  │
     │                     │                    │                 │                  │
     │ 202 Accepted        │                    │ Consome         │                  │
     │ {id, status:PENDING}│                    │<────────────────│                  │
     │<────────────────────│                    │                 │                  │
     │                     │                    │                 │                  │
     │                     │                    │                 │ Se ACCOUNT:      │
     │                     │                    │                 │ Consulta saldo   │
     │                     │                    │                 │────────────────>│
     │                     │                    │                 │                  │
     │                     │                    │                 │ Verifica limite  │
     │                     │                    │                 │ (< R$ 10.000)    │
     │                     │                    │                 │                  │
     │                     │                    │ Publica resultado                  │
     │                     │                    │<────────────────│                  │
     │                     │                    │ transaction-processed              │
     │                     │                    │                 │                  │
     │                     │ Consome resultado  │                 │                  │
     │                     │<───────────────────│                 │                  │
     │                     │                    │                 │                  │
     │                     │ Atualiza status    │                 │                  │
     │                     │ APPROVED/REJECTED  │                 │                  │
```

#### Passo a Passo Detalhado:

**PASSO 1: Cliente envia requisicao**
```http
POST /transactions HTTP/1.1
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "amount": 150.00,
  "currency": "USD",
  "type": "EXPENSE",
  "category": "FOOD",
  "description": "Almoco",
  "origin": "ACCOUNT"
}
```

**PASSO 2: MS-TRANSACTION valida e persiste**
- Extrai `userId` do token JWT via `SecurityUtils.getAuthenticatedUserId()`
- Valida campos obrigatorios
- Busca taxa de cambio via `CurrencyApiClient` (BrasilAPI)
- Calcula `convertedAmount = amount * exchangeRate`
- Persiste com status `PENDING`
- Codigo: `TransactionService.create()` linha 37-70

**PASSO 3: Evento publicado no Kafka**
```java
TransactionCreatedEvent event = new TransactionCreatedEvent(
    transactionId,
    userId,
    amount,
    currency,
    type,
    category,
    origin
);
// Topico: transaction-events
```

**PASSO 4: MS-PROCESSOR consome e processa**
- Consumer Group: `processor-group-v7`
- Logica de aprovacao:
  - Se `origin == CASH`: aprovado automaticamente
  - Se `origin == ACCOUNT`: verifica se `amount < 10000.00`
- Codigo: `TransactionProcessorService.process()` linha 17-37

**PASSO 5: Resultado publicado**
```java
TransactionProcessedEvent result = new TransactionProcessedEvent(
    transactionId,
    approved,      // true ou false
    reason         // null ou "LIMIT_EXCEEDED"
);
// Topico: transaction-processed
```

**PASSO 6: MS-TRANSACTION atualiza status**
- Consumer Group: `transaction-group`
- Se `approved == true`: `TransactionService.approve(id)`
- Se `approved == false`: `TransactionService.reject(id, reason)`
- Codigo: `TransactionEventConsumer.consume()` linha 26-37

---

### 2.2 Fluxo de Erro (Dead Letter Queue)

Quando ocorre erro no processamento, a mensagem eh enviada para o topico DLQ e persistida no banco:

```
┌───────────────┐     ┌─────────┐     ┌───────────────┐
│ MS-PROCESSOR  │     │  KAFKA  │     │ MS-TRANSACTION│
└───────┬───────┘     └────┬────┘     └───────┬───────┘
        │                  │                  │
        │  Erro no         │                  │
        │  processamento   │                  │
        │                  │                  │
        │  DlqMessage      │                  │
        │─────────────────>│                  │
        │  transaction-dlq │                  │
        │                  │                  │
        │                  │  DlqConsumer     │
        │                  │─────────────────>│
        │                  │                  │
        │                  │                  │  Persiste em
        │                  │                  │  dlq_messages
        │                  │                  │  (PostgreSQL)
```

**Estrutura da mensagem DLQ (padronizada em ms-common):**
```java
public record DlqMessage(
    String transactionId,
    Object originalEvent,
    String errorMessage,
    String sourceService,
    LocalDateTime failedAt
) {}
```

**Tabela de persistencia (dlq_messages):**
```sql
CREATE TABLE dlq_messages (
    id BIGSERIAL PRIMARY KEY,
    transaction_id VARCHAR(100),
    original_event TEXT,
    error_message TEXT,
    source_service VARCHAR(50),
    failed_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Componentes envolvidos:**
- `DlqProducer` (ms-transaction): Envia erros para o topico
- `TransactionEventConsumer` (ms-processor): Captura erros e envia para DLQ
- `DlqConsumer` (ms-transaction): Consome do topico e persiste no banco
- `DlqEntity` / `DlqJpaRepository`: Persistencia JPA

---

## 3. Fluxo de Consultas

### 3.1 Listar Transacoes do Usuario

```
Cliente HTTP                    MS-TRANSACTION                  PostgreSQL
     │                             │                                 │
     │  GET /transactions          │                                 │
     │  Authorization: Bearer xxx  │                                 │
     │────────────────────────────>│                                 │
     │                             │                                 │
     │                             │  userId = SecurityUtils         │
     │                             │    .getAuthenticatedUserId()    │
     │                             │                                 │
     │                             │  SELECT * FROM transactions     │
     │                             │  WHERE user_id = ?              │
     │                             │────────────────────────────────>│
     │                             │                                 │
     │  200 OK                     │                                 │
     │  [{id, amount, status...}]  │                                 │
     │<────────────────────────────│                                 │
```

**Endpoint:** `GET /transactions`

**Retorno:** Lista de transacoes do usuario autenticado

---

### 3.2 Buscar Transacao por ID

```
Cliente HTTP                    MS-TRANSACTION                  PostgreSQL
     │                             │                                 │
     │  GET /transactions/{id}     │                                 │
     │  Authorization: Bearer xxx  │                                 │
     │────────────────────────────>│                                 │
     │                             │                                 │
     │                             │  SELECT * FROM transactions     │
     │                             │  WHERE id = ?                   │
     │                             │────────────────────────────────>│
     │                             │                                 │
     │                             │  Verifica se transaction.userId │
     │                             │  == authenticatedUserId         │
     │                             │                                 │
     │  200 OK / 404 Not Found     │                                 │
     │<────────────────────────────│                                 │
```

---

## 4. Integracao com APIs Externas

### 4.1 BrasilAPI (Taxa de Cambio)

**Uso:** Ao criar transacao com moeda diferente de BRL

```
MS-TRANSACTION                   BrasilAPI
     │                              │
     │  GET /api/cambio/v1          │
     │────────────────────────────>│
     │                              │
     │  {                           │
     │    "USD": 4.97,              │
     │    "EUR": 5.42               │
     │  }                           │
     │<────────────────────────────│
```

**Cliente Feign:** `CurrencyApiClient.java`

---

### 4.2 MockAPI (Saldo do Usuario)

**Uso:** Validacao de saldo antes de aprovar transacao (futuro)

```
MS-PROCESSOR                     MockAPI
     │                              │
     │  GET /api/users/{id}/balance │
     │────────────────────────────>│
     │                              │
     │  {                           │
     │    "userId": 1,              │
     │    "balance": 5000.00        │
     │  }                           │
     │<────────────────────────────│
```

**Cliente Feign:** `MockApiClient.java`

---

## 5. Topicos Kafka

| Topico | Producer | Consumer | Descricao |
|--------|----------|----------|-----------|
| `transaction-events` | ms-transaction | ms-processor | Transacoes criadas aguardando processamento |
| `transaction-processed` | ms-processor | ms-transaction | Resultado do processamento (aprovado/rejeitado) |
| `transaction-dlq` | ms-transaction, ms-processor | ms-transaction (DlqConsumer) | Mensagens com erro, persistidas no banco |

Os topicos sao criados automaticamente na inicializacao do ms-transaction via beans `NewTopic` configurados em `KafkaConfig`.

---

## 6. Estados da Transacao

```
     ┌──────────┐
     │ PENDING  │  ← Estado inicial ao criar
     └────┬─────┘
          │
    ┌─────┴─────┐
    │           │
    ▼           ▼
┌──────────┐ ┌──────────┐
│ APPROVED │ │ REJECTED │
└──────────┘ └──────────┘
```

**Transicoes:**
- `PENDING → APPROVED`: Transacao processada com sucesso
- `PENDING → REJECTED`: Transacao rejeitada (limite excedido, saldo insuficiente, etc.)

---

## 7. Seguranca

### 7.1 Endpoints Publicos (sem autenticacao)
- `POST /auth/login` - Login
- `POST /users` - Registro
- `/swagger-ui/**` - Documentacao
- `/v3/api-docs/**` - OpenAPI spec
- `/actuator/health` - Health check

### 7.2 Endpoints Protegidos (requer JWT)
- Todos os demais endpoints

### 7.3 Fluxo de Autenticacao
1. Token JWT enviado no header `Authorization: Bearer <token>`
2. `BaseSecurityConfig.authenticateRequest()` valida o token
3. `SecurityUtils.setAuthentication()` popula o contexto de seguranca
4. Controllers acessam `SecurityUtils.getAuthenticatedUserId()`

---

## 8. Banco de Dados

### 8.1 MS-USER (PostgreSQL porta 5432)

**Tabela: users**
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    public_id UUID NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    cpf VARCHAR(14) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 8.2 MS-TRANSACTION (PostgreSQL porta 5433)

**Tabela: transactions**
```sql
CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    public_id UUID NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    exchange_rate DECIMAL(19,6),
    converted_amount DECIMAL(19,2),
    type VARCHAR(20) NOT NULL,
    category VARCHAR(50),
    description VARCHAR(255),
    origin VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    rejection_reason VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);
```

**Tabela: dlq_messages**
```sql
CREATE TABLE dlq_messages (
    id BIGSERIAL PRIMARY KEY,
    transaction_id VARCHAR(100),
    original_event TEXT,
    error_message TEXT,
    source_service VARCHAR(50),
    failed_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## 9. Configuracoes por Ambiente

### 9.1 Desenvolvimento (profile: dev)
- Hibernate DDL: `update` (cria/altera tabelas automaticamente)
- Flyway: desabilitado
- Logs: DEBUG

### 9.2 Producao (profile: prod)
- Hibernate DDL: `none`
- Flyway: habilitado (migrations controladas)
- Logs: INFO

---

## 10. Monitoramento

### Health Checks
- MS-USER: `http://localhost:8081/actuator/health`
- MS-TRANSACTION: `http://localhost:8082/actuator/health`
- MS-PROCESSOR: `http://localhost:8080/actuator/health`

### Kafka UI (Kafdrop)
- URL: `http://localhost:9000`
- Visualiza topicos, mensagens, consumer groups

### Swagger UI
- MS-USER: `http://localhost:8081/swagger-ui.html`
- MS-TRANSACTION: `http://localhost:8082/swagger-ui.html`
