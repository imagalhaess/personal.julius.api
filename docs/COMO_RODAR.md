# Como Rodar a Aplicação

## 1. Pré-requisitos

Você precisa ter rodando:
- **PostgreSQL** (2 bancos de dados)
- **Kafka** (para ms-transaction)
- **Variável de ambiente JWT_SECRET**

### Bancos de Dados

| Serviço | Host | Porta | Database | Usuário | Senha |
|---------|------|-------|----------|---------|-------|
| ms-user | localhost | 5432 | julius_user_db | julius_user | julius1234 |
| ms-transaction | localhost | 5433 | julius_transaction_db | julius_user | julius1234 |

### Kafka

- Bootstrap servers: `localhost:9092`

---

## 2. Configurar Variável de Ambiente

```bash
# Windows (PowerShell)
$env:JWT_SECRET="dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tZ2VuZXJhdGlvbi1taW5pbXVtLTI1Ni1iaXRz"

# Windows (CMD)
set JWT_SECRET=dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tZ2VuZXJhdGlvbi1taW5pbXVtLTI1Ni1iaXRz

# Linux/Mac
export JWT_SECRET="dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tZ2VuZXJhdGlvbi1taW5pbXVtLTI1Ni1iaXRz"
```

---

## 3. Rodar os Microsserviços

```bash
# Terminal 1 - ms-user (porta 8081)
cd C:\Users\isa\NTTData\ms-user
mvn spring-boot:run

# Terminal 2 - ms-transaction (porta 8082)
cd C:\Users\isa\NTTData\ms-transaction
mvn spring-boot:run
```

---

## 4. Endpoints Disponíveis

### ms-user (http://localhost:8081)

| Método | Endpoint | Descrição | Auth |
|--------|----------|-----------|------|
| POST | `/auth/register` | Registrar usuário | Não |
| POST | `/auth/login` | Login (retorna JWT) | Não |
| GET | `/users` | Listar usuários | Sim |
| GET | `/users/{id}` | Buscar usuário | Sim |
| PUT | `/users/{id}` | Atualizar usuário | Sim |
| DELETE | `/users/{id}` | Desativar usuário (soft delete) | Sim |

### ms-transaction (http://localhost:8082)

| Método | Endpoint | Descrição | Auth |
|--------|----------|-----------|------|
| POST | `/transactions` | Criar transação | Sim |
| GET | `/transactions` | Listar transações | Sim |
| GET | `/transactions/balance` | Ver saldo | Sim |
| DELETE | `/transactions/{id}` | Deletar transação | Sim |

---

## 5. Exemplos de Requisições (Insomnia/Postman)

### 5.1 Registrar Usuário

```http
POST http://localhost:8081/auth/register
Content-Type: application/json

{
  "name": "João Silva",
  "email": "joao@email.com",
  "cpf": "123.456.789-00",
  "password": "Senha@123"
}
```

**Resposta (201 Created):**
```json
{
  "id": 1,
  "name": "João Silva",
  "email": "joao@email.com"
}
```

### 5.2 Login

```http
POST http://localhost:8081/auth/login
Content-Type: application/json

{
  "email": "joao@email.com",
  "password": "Senha@123"
}
```

**Resposta (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### 5.3 Listar Usuários (com token)

```http
GET http://localhost:8081/users?page=0&size=20
Authorization: Bearer <seu_token_jwt>
```

### 5.4 Buscar Usuário por ID

```http
GET http://localhost:8081/users/1
Authorization: Bearer <seu_token_jwt>
```

### 5.5 Atualizar Usuário

```http
PUT http://localhost:8081/users/1
Content-Type: application/json
Authorization: Bearer <seu_token_jwt>

{
  "name": "João Silva Atualizado",
  "email": "joao.novo@email.com"
}
```

### 5.6 Deletar Usuário (Soft Delete)

```http
DELETE http://localhost:8081/users/1
Authorization: Bearer <seu_token_jwt>
```

---

### 5.7 Criar Transação

```http
POST http://localhost:8082/transactions
Content-Type: application/json
Authorization: Bearer <seu_token_jwt>

{
  "amount": 150.00,
  "currency": "BRL",
  "category": "FOOD",
  "type": "EXPENSE",
  "description": "Almoço no restaurante",
  "date": "2026-01-27"
}
```

**Categorias disponíveis:** `FOOD`, `TRANSPORT`, `LEISURE`, `HEALTH`, `EDUCATION`, `SALARY`, `INVESTMENT`, `OTHER`

**Tipos disponíveis:** `INCOME`, `EXPENSE`

### 5.8 Listar Transações

```http
GET http://localhost:8082/transactions?page=0&size=10
Authorization: Bearer <seu_token_jwt>
```

### 5.9 Ver Saldo

```http
GET http://localhost:8082/transactions/balance
Authorization: Bearer <seu_token_jwt>
```

**Resposta:**
```json
{
  "totalIncome": 5000.00,
  "totalExpense": 1500.00,
  "balance": 3500.00
}
```

### 5.10 Deletar Transação

```http
DELETE http://localhost:8082/transactions/1
Authorization: Bearer <seu_token_jwt>
```

---

## 6. Swagger UI (Documentação Interativa)

Após rodar os microsserviços, acesse:

- **ms-user:** http://localhost:8081/swagger-ui.html
- **ms-transaction:** http://localhost:8082/swagger-ui.html

---

## 7. Troubleshooting

### Erro: "JWT_SECRET não configurado"
Certifique-se de que a variável de ambiente está configurada no mesmo terminal onde você executa o `mvn spring-boot:run`.

### Erro: "Connection refused" no banco de dados
Verifique se o PostgreSQL está rodando e se os bancos de dados foram criados:
```sql
CREATE DATABASE julius_user_db;
CREATE DATABASE julius_transaction_db;
CREATE USER julius_user WITH PASSWORD 'julius1234';
GRANT ALL PRIVILEGES ON DATABASE julius_user_db TO julius_user;
GRANT ALL PRIVILEGES ON DATABASE julius_transaction_db TO julius_user;
```

### Erro: "Kafka not available"
Certifique-se de que o Kafka está rodando na porta 9092 antes de iniciar o ms-transaction.
