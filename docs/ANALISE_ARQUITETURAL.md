# Relatório de Análise Arquitetural

**Projeto:** Julius API - Microservices
**Data:** 27/01/2026
**Versão:** 1.0

---

## Sumário Executivo

O projeto apresenta uma boa estrutura baseada em **Clean Architecture** e **Arquitetura Hexagonal**, com separação clara de camadas. No entanto, foram identificados pontos de melhoria relacionados principalmente a **duplicação de código**, **validações de domínio** e **princípios SOLID**.

**Nota Geral:** 7.5/10

---

## 1. Estrutura Atual do Projeto

```
NTTData/
├── ms-user/                          # Microsserviço de Usuários
│   └── src/main/java/.../api/
│       ├── adapter/                  # Camada de Interface (Controllers, DTOs)
│       ├── application/              # Camada de Aplicação (Services, DTOs)
│       ├── domain/                   # Camada de Domínio (Models, Repositories)
│       └── infrastructure/           # Camada de Infraestrutura (JPA, Security)
│
└── ms-transaction/                   # Microsserviço de Transações
    └── src/main/java/.../api/
        ├── adapter/
        ├── application/
        ├── domain/
        └── infrastructure/
```

---

## 2. Pontos Positivos Identificados

### 2.1 Separação de Camadas (Clean Architecture)
- Camadas bem definidas: Domain, Application, Adapter, Infrastructure
- Dependências apontam para o centro (Domain)

### 2.2 Inversão de Dependência (SOLID - D)
- Interfaces de repositório definidas no domínio
- Implementações na camada de infraestrutura
- Injeção de dependência via construtor em todas as classes

### 2.3 Padrão Ports & Adapters (Hexagonal)
- Controllers funcionam como adapters de entrada
- PersistenceAdapter implementa ports de saída

### 2.4 Boas Práticas Implementadas
- Soft Delete para usuários (método `deactivate()`)
- Uso de records para DTOs imutáveis
- Event-driven com Kafka para transações
- Dead Letter Queue para tratamento de falhas

---

## 3. Problemas Identificados e Correções

### 3.1 ALTA PRIORIDADE

#### 3.1.1 Duplicação de Código entre Microsserviços

**Problema:** Classes idênticas ou muito similares duplicadas em ambos os módulos.

**Arquivos Afetados:**
- `JwtService.java` - Duplicado em ms-user e ms-transaction
- `JwtAuthenticationFilter.java` - Duplicado em ambos
- `GlobalExceptionHandler.java` - Idêntico em ambos
- `BusinessException.java` - Idêntico em ambos
- `SecurityConfig.java` - Muito similar em ambos

**Violação:** Princípio DRY (Don't Repeat Yourself)

**Solução Recomendada:**
Criar um módulo comum (`ms-common` ou `julius-common`) para código compartilhado:

```xml
<!-- pom.xml do projeto pai -->
<modules>
    <module>ms-common</module>
    <module>ms-user</module>
    <module>ms-transaction</module>
</modules>
```

**Estrutura sugerida para ms-common:**
```
ms-common/
└── src/main/java/.../common/
    ├── exception/
    │   ├── BusinessException.java
    │   └── GlobalExceptionHandler.java
    └── security/
        ├── JwtService.java
        └── JwtAuthenticationFilter.java
```

---

#### 3.1.2 IDs UUID Excessivamente Grandes

**Problema:** O projeto utiliza `GenerationType.UUID` para geração de identificadores, resultando em IDs de 36 caracteres como:
```
550e8400-e29b-41d4-a716-446655440000
```

**Arquivos Afetados:**
- `ms-user/.../infrastructure/persistence/entity/UserEntity.java:18-20`
- `ms-transaction/.../infrastructure/persistence/entity/TransactionEntity.java:21-23`

```java
@Id
@GeneratedValue(strategy = GenerationType.UUID)
private UUID id;
```

**Problemas:**
- IDs difíceis de memorizar/comunicar
- Ocupam mais espaço em banco de dados e índices
- URLs longas e pouco amigáveis (`/users/550e8400-e29b-41d4-a716-446655440000`)
- Pior performance em JOINs comparado a inteiros

**Violação:** Princípio KISS - simplicidade

**Soluções Disponíveis:**

**Opção 1: ID Sequencial (IDENTITY) - Recomendada para simplicidade**
```java
// UserEntity.java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

| Vantagem | Desvantagem |
|----------|-------------|
| IDs simples (1, 2, 3...) | Previsível (segurança) |
| Melhor performance | Problema em sharding |
| Fácil de comunicar | Dependente do banco |

**Opção 2: ID Sequencial com SEQUENCE (Melhor para produção)**
```java
// UserEntity.java
@Id
@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
@SequenceGenerator(name = "user_seq", sequenceName = "user_sequence", allocationSize = 50)
private Long id;
```

| Vantagem | Desvantagem |
|----------|-------------|
| IDs simples e sequenciais | Requer suporte do banco |
| Melhor performance em batch | Configuração adicional |
| Pré-alocação de IDs | - |

**Opção 3: ID Curto Customizado (Híbrido)**
```java
// Gera IDs como: "USR-00001", "TXN-00042"
@Id
@Column(length = 15)
private String id;

@PrePersist
public void generateId() {
    if (this.id == null) {
        // Usar serviço de geração de ID sequencial com prefixo
        this.id = IdGenerator.nextUserId();
    }
}
```

**Opção 4: NanoID ou ULID (Alternativa moderna ao UUID)**
```java
// NanoID: "V1StGXR8_Z5jdHi6B-myT" (21 chars, URL-safe)
// ULID: "01ARZ3NDEKTSV4RRFFQ69G5FAV" (26 chars, ordenável)

// Dependência Maven para NanoID
// <dependency>
//     <groupId>com.aventrix.jnanoid</groupId>
//     <artifactId>jnanoid</artifactId>
//     <version>2.0.0</version>
// </dependency>

@Id
@Column(length = 21)
private String id;

@PrePersist
public void generateId() {
    if (this.id == null) {
        this.id = NanoIdUtils.randomNanoId();
    }
}
```

**Recomendação Final:**

Para este projeto, recomendo **Opção 2 (SEQUENCE)** pelos seguintes motivos:
- IDs numéricos simples e fáceis de usar
- Boa performance
- Suportado pelo PostgreSQL (banco comum em produção)
- Mantém ordenação temporal

**Implementação Sugerida:**

```java
// domain/model/User.java - Alterar tipo do ID
public class User {
    private Long id;  // Mudança de UUID para Long
    // ... resto do código
}

// infrastructure/persistence/entity/UserEntity.java
@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
    @SequenceGenerator(name = "user_seq", sequenceName = "user_sequence", allocationSize = 50)
    private Long id;

    // ... resto do código
}

// Atualizar UserRepository, UserService, UserController para usar Long
```

**Migração de Dados (se já houver dados em produção):**
```sql
-- Script de migração
ALTER TABLE users ADD COLUMN new_id BIGSERIAL;
UPDATE users SET new_id = nextval('user_sequence');
ALTER TABLE users DROP COLUMN id;
ALTER TABLE users RENAME COLUMN new_id TO id;
ALTER TABLE users ADD PRIMARY KEY (id);
```

---

#### 3.1.3 Violação do Princípio de Responsabilidade Única (SRP)

**Problema:** `TransactionService` importa classe de infraestrutura diretamente.

**Arquivo:** `ms-transaction/.../application/service/TransactionService.java:9-10`

```java
// PROBLEMA: Camada de aplicação dependendo de infraestrutura
import messaging.infrastructure.api.nttdata.personal.julius.api.TransactionCreatedEvent;
import messaging.infrastructure.api.nttdata.personal.julius.api.TransactionEventProducer;
```

**Violação:** Clean Architecture - camada de aplicação não deve conhecer infraestrutura

**Solução:**
1. Criar interface (port) no domínio ou aplicação:

```java
// application/port/TransactionEventPort.java
public interface TransactionEventPort {
    void publishTransactionCreated(TransactionCreatedEventDto event);
}
```

2. Mover o DTO de evento para camada de aplicação:

```java
// application/dto/TransactionCreatedEventDto.java
public record TransactionCreatedEventDto(
    UUID transactionId,
    UUID userId,
    BigDecimal amount,
    String currency,
    String type,
    String category
) {}
```

3. Implementar na infraestrutura:

```java
// infrastructure/messaging/TransactionEventAdapter.java
@Component
public class TransactionEventAdapter implements TransactionEventPort {
    private final TransactionEventProducer producer;

    @Override
    public void publishTransactionCreated(TransactionCreatedEventDto dto) {
        producer.send(new TransactionCreatedEvent(...));
    }
}
```

---

#### 3.1.4 Falta de Validação no Domínio

**Problema:** Validações de negócio estão apenas na camada de adapter (DTOs).

**Arquivo:** `ms-user/.../adapter/dto/UserRequest.java`

```java
// Validações apenas no DTO de entrada
@Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])...")
String password
```

**Violação:** O domínio deveria ser auto-validante (Domain-Driven Design)

**Solução:** Adicionar validações no modelo de domínio:

```java
// domain/model/User.java
public class User {
    // ... campos existentes

    public User(String name, String email, String cpf, String password) {
        validateName(name);
        validateEmail(email);
        validateCpf(cpf);
        // Nota: senha já vem encriptada do service, validação de força
        // deve ser feita antes da encriptação

        this.name = name;
        this.email = email;
        this.cpf = cpf;
        this.password = password;
        this.active = true;
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new DomainValidationException("Nome é obrigatório");
        }
    }

    private void validateEmail(String email) {
        if (email == null || !email.matches("^[\\w-.]+@[\\w-]+\\.[a-z]{2,}$")) {
            throw new DomainValidationException("E-mail inválido");
        }
    }

    private void validateCpf(String cpf) {
        if (cpf == null || !cpf.matches("\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}")) {
            throw new DomainValidationException("CPF inválido");
        }
    }
}
```

---

### 3.2 MÉDIA PRIORIDADE

#### 3.2.1 findAll() Retorna Usuários Inativos

**Problema:** O método `findAll()` não filtra usuários desativados.

**Arquivo:** `ms-user/.../application/service/UserService.java:88-93`

```java
public List<UserResponseDto> findAll() {
    return repository.findAll()  // Retorna TODOS, incluindo inativos
            .stream()
            .map(user -> new UserResponseDto(...))
            .toList();
}
```

**Solução:**

```java
public List<UserResponseDto> findAll() {
    return repository.findAll()
            .stream()
            .filter(User::isActive)  // Filtrar apenas ativos
            .map(user -> new UserResponseDto(user.getId(), user.getName(), user.getEmail()))
            .toList();
}
```

**Alternativa (melhor performance):** Adicionar método no repositório:

```java
// domain/repository/UserRepository.java
List<User> findAllActive();

// infrastructure/persistence/repository/UserJpaRepository.java
@Query("SELECT u FROM UserEntity u WHERE u.active = true")
List<UserEntity> findAllActive();
```

---

#### 3.2.2 Falta de Paginação em UserService

**Problema:** `findAll()` retorna todos os registros sem paginação.

**Arquivo:** `ms-user/.../application/service/UserService.java:88`

**Violação:** Pode causar problemas de performance com muitos registros

**Solução:**

```java
// domain/repository/UserRepository.java
List<User> findAllActive(int page, int size);

// application/service/UserService.java
public List<UserResponseDto> findAll(int page, int size) {
    return repository.findAllActive(page, size)
            .stream()
            .map(this::toResponse)
            .toList();
}
```

---

#### 3.2.3 Entidade de Domínio com Setters Públicos

**Problema:** `Transaction` possui setters públicos, violando encapsulamento.

**Arquivo:** `ms-transaction/.../domain/model/Transaction.java:44-117`

```java
public void setId(UUID id) { this.id = id; }
public void setStatus(TransactionStatus status) { this.status = status; }
// ... todos os setters
```

**Violação:** Entidades de domínio devem ser imutáveis ou ter comportamentos explícitos

**Solução:** Usar métodos de comportamento ao invés de setters:

```java
public class Transaction {
    // Remover setters públicos
    // Adicionar métodos de comportamento:

    public void approve() {
        if (this.status != TransactionStatus.PENDING) {
            throw new BusinessException("Transação não pode ser aprovada");
        }
        this.status = TransactionStatus.APPROVED;
    }

    public void reject(String reason) {
        if (this.status != TransactionStatus.PENDING) {
            throw new BusinessException("Transação não pode ser rejeitada");
        }
        this.status = TransactionStatus.REJECTED;
        // Opcionalmente guardar o motivo
    }
}
```

**Atualizar o service:**

```java
// TransactionService.java
@Transactional
public void approve(UUID transactionId) {
    Transaction t = repository.findById(transactionId)
            .orElseThrow(() -> new BusinessException("Transação não encontrada"));

    t.approve();  // Método de comportamento
    repository.save(t);
}
```

---

#### 3.2.4 Tratamento Silencioso de Exceções no JWT Filter

**Problema:** Exceções são capturadas silenciosamente.

**Arquivo:** `ms-transaction/.../infrastructure/security/JwtAuthenticationFilter.java`

```java
catch (Exception e) {
    // Token inválido - silencioso
}
```

**Solução:**

```java
catch (Exception e) {
    logger.debug("Token JWT inválido: {}", e.getMessage());
    // Continua sem autenticação - comportamento correto para token inválido
}
```

---

### 3.3 BAIXA PRIORIDADE

#### 3.3.1 Duplicação de DTOs entre Camadas

**Problema:** DTOs muito similares em adapter e application.

**Arquivos:**
- `adapter/dto/UserRequest.java` vs `application/dto/UserDto.java`
- `adapter/dto/UserResponse.java` vs `application/dto/UserResponseDto.java`

**Análise:** Esta é uma decisão arquitetural válida que mantém independência entre camadas. Porém, causa duplicação.

**Sugestão:** Manter como está se a independência entre camadas for prioritária. Caso contrário, considerar usar apenas um conjunto de DTOs na camada de aplicação.

---

#### 3.3.2 Constantes Hardcoded

**Problema:** Valores hardcoded no código.

**Arquivos:**
```java
// TransactionService.java:35
t.setCurrency(request.currency() != null ? request.currency() : "BRL");
```

**Solução:** Usar constantes ou configuração:

```java
@Value("${app.default-currency:BRL}")
private String defaultCurrency;
```

---

#### 3.3.3 Falta de Documentação JavaDoc

**Problema:** Classes e métodos sem documentação.

**Sugestão:** Adicionar JavaDoc nos métodos públicos dos services e interfaces de repositório.

---

## 4. Checklist de Conformidade

| Princípio | Status | Observação |
|-----------|--------|------------|
| **Single Responsibility (S)** | ⚠️ Parcial | TransactionService depende de infraestrutura |
| **Open/Closed (O)** | ✅ OK | Uso de interfaces permite extensão |
| **Liskov Substitution (L)** | ✅ OK | Implementações respeitam contratos |
| **Interface Segregation (I)** | ✅ OK | Interfaces focadas e coesas |
| **Dependency Inversion (D)** | ✅ OK | Depende de abstrações |
| **DRY** | ❌ Violado | Código duplicado entre módulos |
| **KISS** | ❌ Violado | UUIDs complexos, estruturas poderiam ser simplificadas |
| **Clean Architecture** | ⚠️ Parcial | Violação de dependência no TransactionService |
| **Hexagonal** | ✅ OK | Ports e Adapters bem definidos |

---

## 5. Plano de Ação Sugerido

### Fase 1 - Correções Críticas
1. [ ] Criar módulo `ms-common` para código compartilhado
2. [ ] Mover JwtService, JwtAuthenticationFilter, GlobalExceptionHandler para comum
3. [ ] **Substituir UUID por ID sequencial (Long)** em User e Transaction
4. [ ] Criar interface TransactionEventPort para desacoplar service de infraestrutura

### Fase 2 - Melhorias de Domínio
5. [ ] Adicionar validações no modelo User
6. [ ] Substituir setters por métodos de comportamento em Transaction
7. [ ] Filtrar usuários inativos no findAll()

### Fase 3 - Melhorias de Qualidade
8. [ ] Adicionar paginação no UserService
9. [ ] Melhorar logging no JWT filter
10. [ ] Adicionar JavaDoc nas interfaces públicas

---

## 6. Exemplos de Refatoração

### 6.1 Estrutura Proposta com ms-common

```
NTTData/
├── pom.xml (parent)
├── ms-common/
│   ├── pom.xml
│   └── src/main/java/nttdata/personal/julius/common/
│       ├── exception/
│       │   ├── BusinessException.java
│       │   └── GlobalExceptionHandler.java
│       └── security/
│           ├── JwtService.java
│           ├── JwtAuthenticationFilter.java
│           └── JwtProperties.java
├── ms-user/
│   ├── pom.xml (depends on ms-common)
│   └── ...
└── ms-transaction/
    ├── pom.xml (depends on ms-common)
    └── ...
```

### 6.2 pom.xml do ms-common

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>nttdata.personal.julius</groupId>
        <artifactId>julius-parent</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </parent>

    <artifactId>ms-common</artifactId>
    <name>ms-common</name>
    <description>Shared components for Julius microservices</description>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>0.12.6</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>0.12.6</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>0.12.6</version>
            <scope>runtime</scope>
        </dependency>
    </dependencies>
</project>
```

---

## 7. Conclusão

O projeto demonstra boa compreensão de Clean Architecture e princípios SOLID, com estrutura bem organizada. Os principais pontos de atenção são:

1. **Crítico:** Duplicação de código entre microsserviços - resolver com módulo comum
2. **Crítico:** IDs UUID muito grandes - migrar para IDs sequenciais (Long)
3. **Importante:** Violação de camadas no TransactionService - criar port/adapter
4. **Recomendado:** Melhorar encapsulamento das entidades de domínio

A implementação das correções sugeridas elevará a qualidade arquitetural do projeto e facilitará a manutenção a longo prazo.

---

*Relatório gerado automaticamente por análise de código.*
