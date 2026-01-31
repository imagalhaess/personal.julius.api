# Guia Completo de Testes - Julius API

## Pre-requisitos

### 1. Iniciar Infraestrutura

**Windows:**
```cmd
start_dev.bat
```

**Linux/Mac:**
```bash
./start_dev.sh
```

Ou manualmente:
```bash
# Subir containers Docker (apenas infra)
docker compose -f docker-compose.dev.yml up -d

# Compilar projeto
mvn clean install -DskipTests

# Iniciar servicos (em terminais separados)
cd ms-user && mvn spring-boot:run -Dspring-boot.run.profiles=dev
cd ms-transaction && mvn spring-boot:run -Dspring-boot.run.profiles=dev
cd ms-processor && mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 2. Verificar Servicos
```bash
# Health checks
curl http://localhost:8081/actuator/health  # MS-USER
curl http://localhost:8082/actuator/health  # MS-TRANSACTION
curl http://localhost:8080/actuator/health  # MS-PROCESSOR
```

### 3. Ferramentas Recomendadas
- **Postman** ou **Insomnia** para testes manuais
- **curl** para linha de comando
- **Kafdrop** (http://localhost:9000) para monitorar Kafka

---

## PARTE 1: CRUD de Usuario (MS-USER)

### 1.1 Criar Usuario (Sucesso)

**Request:**
```bash
curl -X POST http://localhost:8081/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Joao Silva",
    "email": "joao@email.com",
    "cpf": "123.456.789-00",
    "password": "Senha@123"
  }'
```

**Resposta Esperada (201 Created):**
```json
{
  "publicId": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Joao Silva",
  "email": "joao@email.com"
}
```

**O que verificar:**
- Status HTTP 201
- `publicId` eh um UUID valido
- Senha NAO eh retornada

---

### 1.2 Criar Usuario - Erros de Validacao

#### 1.2.1 Email invalido
```bash
curl -X POST http://localhost:8081/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Teste",
    "email": "email-invalido",
    "cpf": "111.222.333-44",
    "password": "Senha@123"
  }'
```

**Resposta Esperada (400 Bad Request):**
```json
{
  "timestamp": "2026-01-29T...",
  "status": 400,
  "error": "Bad Request",
  "message": "O e-mail deve ser valido"
}
```

#### 1.2.2 CPF formato invalido
```bash
curl -X POST http://localhost:8081/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Teste",
    "email": "teste@email.com",
    "cpf": "12345678900",
    "password": "Senha@123"
  }'
```

**Resposta Esperada (400):**
```json
{
  "message": "O CPF deve seguir o formato 000.000.000-00"
}
```

#### 1.2.3 Senha fraca
```bash
curl -X POST http://localhost:8081/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Teste",
    "email": "teste2@email.com",
    "cpf": "222.333.444-55",
    "password": "123456"
  }'
```

**Resposta Esperada (400):**
```json
{
  "message": "A senha deve ter no minimo 8 caracteres, incluindo maiuscula, minuscula, numero e caractere especial"
}
```

#### 1.2.4 Email duplicado
```bash
# Criar primeiro usuario
curl -X POST http://localhost:8081/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Usuario 1",
    "email": "duplicado@email.com",
    "cpf": "333.444.555-66",
    "password": "Senha@123"
  }'

# Tentar criar com mesmo email
curl -X POST http://localhost:8081/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Usuario 2",
    "email": "duplicado@email.com",
    "cpf": "444.555.666-77",
    "password": "Senha@123"
  }'
```

**Resposta Esperada (400/409):**
```json
{
  "message": "E-mail ja cadastrado"
}
```

#### 1.2.5 CPF duplicado
```bash
curl -X POST http://localhost:8081/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Usuario 3",
    "email": "outro@email.com",
    "cpf": "333.444.555-66",
    "password": "Senha@123"
  }'
```

**Resposta Esperada (400/409):**
```json
{
  "message": "CPF ja cadastrado"
}
```

---

### 1.3 Login (Autenticacao)

#### 1.3.1 Login com sucesso
```bash
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "joao@email.com",
    "password": "Senha@123"
  }'
```

**Resposta Esperada (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsInN1YiI6ImpvYW9AZW1haWwuY29tIiwiaWF0IjoxNzA2...",
  "type": "Bearer"
}
```

**IMPORTANTE:** Guarde o token para usar nos proximos testes!

#### 1.3.2 Login - Credenciais invalidas
```bash
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "joao@email.com",
    "password": "SenhaErrada"
  }'
```

**Resposta Esperada (401 Unauthorized):**
```json
{
  "message": "Credenciais invalidas"
}
```

#### 1.3.3 Login - Usuario nao existe
```bash
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "naoexiste@email.com",
    "password": "Senha@123"
  }'
```

**Resposta Esperada (401):**
```json
{
  "message": "Credenciais invalidas"
}
```

---

### 1.4 Buscar Usuario

#### 1.4.1 Buscar por ID (requer autenticacao)
```bash
TOKEN="seu_token_aqui"

curl -X GET http://localhost:8081/users/1 \
  -H "Authorization: Bearer $TOKEN"
```

**Resposta Esperada (200):**
```json
{
  "publicId": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Joao Silva",
  "email": "joao@email.com"
}
```

#### 1.4.2 Buscar sem token
```bash
curl -X GET http://localhost:8081/users/1
```

**Resposta Esperada (401/403):**
```json
{
  "error": "Unauthorized"
}
```

#### 1.4.3 Usuario nao encontrado
```bash
curl -X GET http://localhost:8081/users/99999 \
  -H "Authorization: Bearer $TOKEN"
```

**Resposta Esperada (404):**
```json
{
  "message": "Usuario nao encontrado"
}
```

---

### 1.5 Atualizar Usuario

```bash
curl -X PUT http://localhost:8081/users/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Joao Silva Atualizado",
    "email": "joao.novo@email.com"
  }'
```

**Resposta Esperada (200):**
```json
{
  "publicId": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Joao Silva Atualizado",
  "email": "joao.novo@email.com"
}
```

---

### 1.6 Deletar Usuario

```bash
curl -X DELETE http://localhost:8081/users/1 \
  -H "Authorization: Bearer $TOKEN"
```

**Resposta Esperada (204 No Content):**
- Sem corpo de resposta
- Usuario marcado como inativo (soft delete)

---

## PARTE 2: CRUD de Transacao (MS-TRANSACTION)

### 2.1 Criar Transacao - Cenario APROVADA

#### 2.1.1 Transacao em dinheiro (CASH) - Sempre aprovada
```bash
TOKEN="seu_token_aqui"

curl -X POST http://localhost:8082/transactions \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 500.00,
    "currency": "BRL",
    "type": "EXPENSE",
    "category": "FOOD",
    "description": "Almoco",
    "origin": "CASH"
  }'
```

**Resposta Imediata (202 Accepted):**
```json
{
  "id": 1,
  "currency": "BRL",
  "targetCurrency": "BRL",
  "amount": 500.00,
  "exchangeRate": 1.0,
  "convertedAmount": 500.00,
  "status": "PENDING",
  "description": "Almoco",
  "createdAt": "2026-01-29T19:30:00",
  "category": "FOOD",
  "type": "EXPENSE"
}
```

**Apos processamento (~1-2 segundos), consultar:**
```bash
curl -X GET http://localhost:8082/transactions/1 \
  -H "Authorization: Bearer $TOKEN"
```

**Resposta Esperada (200):**
```json
{
  "id": 1,
  "status": "APPROVED",
  ...
}
```

**O que acontece internamente:**
1. MS-TRANSACTION cria transacao com status PENDING
2. Publica evento no topico `transaction-events`
3. MS-PROCESSOR consome e processa:
   - Converte moeda se necessario (cache de 24h)
   - `EXPENSE + CASH` → aprova (sem conta vinculada)
4. Publica resultado no topico `transaction-processed`
5. MS-TRANSACTION consome e atualiza para APPROVED

---

#### 2.1.2 Transacao de conta (ACCOUNT) - Com saldo suficiente
```bash
curl -X POST http://localhost:8082/transactions \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 1500.00,
    "currency": "BRL",
    "type": "EXPENSE",
    "category": "SHOPPING",
    "description": "Compras do mes",
    "origin": "ACCOUNT"
  }'
```

**Resultado esperado apos processamento:**
- Status: `APPROVED` (saldo validado via API externa)

---

### 2.2 Criar Transacao - Cenario REJEITADA

#### 2.2.1 Saldo insuficiente (ACCOUNT com valor maior que saldo)
```bash
curl -X POST http://localhost:8082/transactions \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 15000.00,
    "currency": "BRL",
    "type": "EXPENSE",
    "category": "TRANSFER",
    "description": "Transferencia grande",
    "origin": "ACCOUNT"
  }'
```

**Resposta Imediata (202 Accepted):**
```json
{
  "id": 3,
  "status": "PENDING",
  ...
}
```

**Apos processamento, consultar:**
```bash
curl -X GET http://localhost:8082/transactions/3 \
  -H "Authorization: Bearer $TOKEN"
```

**Resposta Esperada (200):**
```json
{
  "id": 3,
  "status": "REJECTED",
  "rejectionReason": "INSUFFICIENT_FUNDS",
  ...
}
```

---

### 2.3 Transacao com Moeda Estrangeira

```bash
curl -X POST http://localhost:8082/transactions \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 100.00,
    "currency": "USD",
    "type": "EXPENSE",
    "category": "TRAVEL",
    "description": "Compra internacional",
    "origin": "ACCOUNT"
  }'
```

**Resposta (202):**
```json
{
  "id": 4,
  "currency": "USD",
  "targetCurrency": "BRL",
  "amount": 100.00,
  "exchangeRate": 4.97,
  "convertedAmount": 497.00,
  "status": "PENDING",
  ...
}
```

**O que verificar:**
- `exchangeRate` obtido da BrasilAPI
- `convertedAmount = amount * exchangeRate`

---

### 2.4 Listar Transacoes do Usuario

```bash
curl -X GET http://localhost:8082/transactions \
  -H "Authorization: Bearer $TOKEN"
```

**Resposta (200):**
```json
[
  {
    "id": 1,
    "status": "APPROVED",
    ...
  },
  {
    "id": 2,
    "status": "APPROVED",
    ...
  },
  {
    "id": 3,
    "status": "REJECTED",
    ...
  }
]
```

---

### 2.5 Erros de Validacao em Transacao

#### 2.5.1 Campos obrigatorios faltando
```bash
curl -X POST http://localhost:8082/transactions \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 100.00
  }'
```

**Resposta (400):**
```json
{
  "errors": [
    "currency: Campo obrigatorio",
    "type: Campo obrigatorio",
    "origin: Campo obrigatorio"
  ]
}
```

#### 2.5.2 Valor negativo
```bash
curl -X POST http://localhost:8082/transactions \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": -50.00,
    "currency": "BRL",
    "type": "EXPENSE",
    "origin": "CASH"
  }'
```

**Resposta (400):**
```json
{
  "message": "O valor deve ser positivo"
}
```

---

## PARTE 3: Cenarios de Erro e DLQ

### 3.1 Monitorar Topico Dead Letter Queue

Abra o Kafdrop: http://localhost:9000

1. Clique em "Topics"
2. Selecione `transaction-dlq`
3. Clique em "View Messages"

Os topicos sao criados automaticamente quando o ms-transaction inicia. Se nao aparecerem, reinicie o servico.

---

### 3.2 Verificar Persistencia do DLQ

As mensagens do DLQ sao persistidas na tabela `dlq_messages`. Para consultar:

```bash
# Conectar ao banco de transacoes
docker exec -it transaction-db psql -U postgres -d transaction_db

# Listar mensagens do DLQ
SELECT id, transaction_id, source_service, error_message, failed_at FROM dlq_messages;
```

---

### 3.3 Simular Erro para DLQ

Para testar o fluxo completo do DLQ:

```bash
# 1. Parar o MS-PROCESSOR
# (Ctrl+C no terminal do MS-PROCESSOR)

# 2. Criar transacao (ficara em PENDING)
curl -X POST http://localhost:8082/transactions \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 500.00,
    "currency": "BRL",
    "type": "EXPENSE",
    "origin": "ACCOUNT"
  }'

# 3. Reiniciar MS-PROCESSOR
cd ms-processor && mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 4. Se houver erro no processamento, verificar DLQ no banco
docker exec -it transaction-db psql -U postgres -d transaction_db -c "SELECT * FROM dlq_messages;"
```

---

### 3.4 Estrutura de Mensagem na DLQ

Formato padronizado (definido em ms-common):

```json
{
  "transactionId": "5",
  "originalEvent": {
    "transactionId": 5,
    "userId": 1,
    "amount": 500.00,
    "currency": "BRL",
    "type": "EXPENSE",
    "origin": "ACCOUNT"
  },
  "errorMessage": "Erro ao processar transacao: Connection refused",
  "sourceService": "ms-processor",
  "failedAt": "2026-01-29T19:45:00"
}
```

---

### 3.5 Tratamento de Erros por Tipo

| Tipo de Erro | Comportamento | Destino |
|--------------|---------------|---------|
| Validacao (400) | Retorna erro imediato | Cliente |
| Autenticacao (401) | Retorna erro imediato | Cliente |
| Autorizacao (403) | Retorna erro imediato | Cliente |
| Nao encontrado (404) | Retorna erro imediato | Cliente |
| Erro no processamento | Envia para DLQ | Kafka DLQ + PostgreSQL |
| Timeout API externa | Envia para DLQ | Kafka DLQ + PostgreSQL |
| Erro de serializacao | Envia para DLQ | Kafka DLQ + PostgreSQL |

---

## PARTE 4: Testes Automatizados

### 4.1 Executar Todos os Testes

```bash
cd /caminho/para/NTTData
mvn test
```

**Saida esperada:**
```
[INFO] Results:
[INFO]
[INFO] Tests run: X, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] BUILD SUCCESS
```

---

### 4.2 Testes por Modulo

#### MS-USER
```bash
mvn test -pl ms-user
```

**Testes inclusos:**
- `UserServiceTest` - Logica de negocio de usuarios
- `UserTest` - Validacoes do modelo de dominio

#### MS-TRANSACTION
```bash
mvn test -pl ms-transaction
```

**Testes inclusos:**
- `TransactionServiceTest` - Criacao, aprovacao, rejeicao
- `TransactionTest` - Validacoes do modelo
- `KafkaTestConfig` - Configuracao de Kafka para testes

#### MS-PROCESSOR
```bash
mvn test -pl ms-processor
```

**Testes inclusos:**
- `TransactionProcessorServiceTest`:
  - `shouldProcessAndApproveTransaction` - Transacao aprovada
  - `shouldRejectTransactionWhenLimitExceeded` - Limite excedido
  - `shouldApproveCashTransactionAutomatically` - CASH sempre aprova

---

## PARTE 5: Checklist de Verificacao

### 5.1 Antes de Commit

- [ ] Todos os testes passando (`mvn test`)
- [ ] Build sem erros (`mvn clean install -DskipTests`)
- [ ] Servicos iniciam sem erros
- [ ] Health checks retornam UP

### 5.2 Cenarios de Integracao

- [ ] Usuario pode se registrar
- [ ] Usuario pode fazer login
- [ ] Token JWT eh valido e funciona
- [ ] Transacao INCOME eh aprovada automaticamente (entrada)
- [ ] Transacao EXPENSE + CASH eh aprovada (sem conta vinculada)
- [ ] Transacao EXPENSE + ACCOUNT com saldo suficiente eh aprovada
- [ ] Transacao EXPENSE + ACCOUNT com saldo insuficiente eh rejeitada
- [ ] Conversao de moeda funciona (USD, EUR) - cache de 24h
- [ ] Topicos Kafka criados (transaction-events, transaction-processed, transaction-dlq)
- [ ] Erros de processamento sao enviados para DLQ
- [ ] Mensagens DLQ sao persistidas na tabela dlq_messages

### 5.3 Monitoramento

- [ ] Kafdrop mostra topicos e mensagens
- [ ] Swagger UI acessivel
- [ ] Actuator health retorna detalhes

---

## PARTE 6: Troubleshooting

### 6.1 Transacao fica em PENDING

**Causas possiveis:**
1. MS-PROCESSOR nao esta rodando
2. Kafka nao esta rodando
3. Consumer group nao esta conectado

**Solucao:**
```bash
# Verificar Kafka
docker ps | grep kafka

# Verificar consumer groups no Kafdrop
# http://localhost:9000 -> Consumer Groups

# Reiniciar MS-PROCESSOR
```

### 6.2 Erro de Conexao com Banco

**Sintoma:** `Connection refused` no startup

**Solucao:**
```bash
# Verificar containers
docker ps

# Se nao estiver rodando
docker compose -f docker-compose.dev.yml up -d
```

### 6.3 Token JWT Expirado

**Sintoma:** `401 Unauthorized` mesmo com token

**Solucao:**
- Fazer novo login para obter token atualizado
- Token expira em 24 horas

### 6.4 Erro de Serializacao Kafka

**Sintoma:** Mensagens nao sao consumidas

**Verificar:**
```bash
# No Kafdrop, verificar mensagens no topico
# Se houver mensagens com formato errado, vao para DLQ
```

---

## PARTE 7: Scripts Uteis

### 7.1 Script de Teste Completo

Salve como `test_flow.sh`:

```bash
#!/bin/bash

BASE_USER="http://localhost:8081"
BASE_TRANS="http://localhost:8082"

echo "=== 1. Criando usuario ==="
RESPONSE=$(curl -s -X POST $BASE_USER/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Teste Automatico",
    "email": "teste.auto@email.com",
    "cpf": "999.888.777-66",
    "password": "Senha@123"
  }')
echo $RESPONSE

echo ""
echo "=== 2. Fazendo login ==="
TOKEN=$(curl -s -X POST $BASE_USER/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "teste.auto@email.com",
    "password": "Senha@123"
  }' | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
echo "Token: ${TOKEN:0:50}..."

echo ""
echo "=== 3. Criando transacao CASH ==="
curl -s -X POST $BASE_TRANS/transactions \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 100.00,
    "currency": "BRL",
    "type": "EXPENSE",
    "category": "FOOD",
    "origin": "CASH"
  }'

echo ""
echo "=== 4. Aguardando processamento ==="
sleep 3

echo ""
echo "=== 5. Listando transacoes ==="
curl -s -X GET $BASE_TRANS/transactions \
  -H "Authorization: Bearer $TOKEN"

echo ""
echo "=== TESTE COMPLETO ==="
```

### 7.2 Limpar Banco de Dados (Dev)

```bash
# Parar servicos e remover volumes
docker compose -f docker-compose.dev.yml down -v

# Subir novamente (bancos limpos)
docker compose -f docker-compose.dev.yml up -d
```

---

## Conclusao

Este guia cobre todos os cenarios de teste da aplicacao Julius API:

1. **CRUD de Usuario** - Registro, login, busca, atualizacao, delecao
2. **CRUD de Transacao** - Criacao, listagem, aprovacao, rejeicao
3. **Fluxo Assincrono** - Kafka, processamento, DLQ
4. **Tratamento de Erros** - Validacao, autenticacao, erros de sistema
5. **Testes Automatizados** - JUnit, Mockito, Spring Boot Test

Para duvidas ou problemas, consulte os logs dos servicos e o Kafdrop para monitorar o fluxo de mensagens.
