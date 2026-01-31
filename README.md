# Julius API - Gestao Financeira Pessoal

Sistema de microsservicos para controle financeiro pessoal, com suporte a multiplas moedas e integracao com APIs externas.

> *Se nao comprar, o desconto é maior!*

## Tecnologias

| Categoria | Tecnologia |
|-----------|------------|
| Linguagem | Java 21 |
| Framework | Spring Boot 4.0.1 |
| Mensageria | Apache Kafka |
| Banco de Dados | PostgreSQL 16 |
| Cache | Caffeine (in-memory) |
| Containerizacao | Docker / Docker Compose |
| APIs Externas | BrasilAPI (cambio), MockAPI (saldo) |

## Arquitetura

O projeto segue os principios de **Clean Architecture** e **SOLID**, dividido em:

| Modulo | Porta | Descricao |
|--------|-------|-----------|
| `ms-common` | - | Logica compartilhada, seguranca JWT, tratamento de erros |
| `ms-user` | 8081 | Gestao de usuarios, autenticacao e autorizacao |
| `ms-transaction` | 8082 | Registro de transacoes, relatorios PDF/Excel, DLQ |
| `ms-processor` | 8080 | Processamento assincrono, conversao de moeda, validacao de saldo |

> Veja o diagrama completo em [docs/FLUXO_APLICACAO.md](docs/FLUXO_APLICACAO.md)

## Como Rodar

### Opcao 1: Docker Compose (Recomendado)

```bash
# Subir tudo (infra + aplicacoes)
docker compose up -d --build

# Ver status
docker compose ps

# Ver logs
docker compose logs -f
```

### Opcao 2: Desenvolvimento Local 
*(Melhor opção para debugar, microsserviços são abertos em terminais distintos)*

**Pre-requisitos:**
- Java 21
- Maven 3.9+
- Docker (para infra)

**Windows:**
```cmd
start_dev.bat
```

**Linux/Mac:**
```bash
chmod +x start_dev.sh
./start_dev.sh
```

**Ou manualmente:**
```bash
# 1. Subir infraestrutura (apenas bancos e kafka)
docker compose -f docker-compose.dev.yml up -d

# 2. Aguardar (15 segundos)

# 3. Compilar
mvn clean install -DskipTests

# 4. Rodar cada microsservico (em terminais separados)
cd ms-user && mvn spring-boot:run -Dspring-boot.run.profiles=dev
cd ms-transaction && mvn spring-boot:run -Dspring-boot.run.profiles=dev
cd ms-processor && mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## URLs dos Servicos

| Servico | URL |
|---------|-----|
| MS-USER | http://localhost:8081 |
| MS-TRANSACTION | http://localhost:8082 |
| MS-PROCESSOR | http://localhost:8080 |
| Kafdrop (Kafka UI) | http://localhost:9000 |

## Documentacao

| Recurso | URL |
|---------|-----|
| Swagger MS-USER | http://localhost:8081/swagger-ui/index.html |
| Swagger MS-TRANSACTION | http://localhost:8082/swagger-ui/index.html |

## Endpoints Principais

### Autenticacao
```
POST /users          - Registrar usuario
POST /auth/login     - Login (retorna JWT)
```

### Transacoes
```
POST   /transactions              - Criar transacao
GET    /transactions              - Listar transacoes
GET    /transactions/balance      - Saldo consolidado
GET    /transactions/report/pdf   - Relatorio PDF
GET    /transactions/report/excel - Relatorio Excel
DELETE /transactions/{id}         - Excluir transacao
```

## Estrutura do Projeto

```
NTTData/
├── docker-compose.yml        # Orquestracao dos containers
├── docker-compose.dev.yml    # Backup (apenas infra)
├── start_dev.bat             # Script Windows
├── start_dev.sh              # Script Linux/Mac
├── ms-common/                # Modulo compartilhado
├── ms-user/                  # Microsservico de usuarios
│   └── Dockerfile
├── ms-transaction/           # Microsservico de transacoes
│   └── Dockerfile
├── ms-processor/             # Microsservico de processamento
│   └── Dockerfile
└── docs/
    ├── ADR.md                # Decisoes de arquitetura
    ├── FLUXO_APLICACAO.md    # Fluxo detalhado
    ├── GUIA_TESTES.md        # Guia de testes
    ├── collections/          # Collection Insomnia
    └── mock-data/            # Dados para importar no MockAPI
```

## Documentacao Adicional

- [Fluxo da Aplicacao](docs/FLUXO_APLICACAO.md) - Diagramas e explicacao detalhada
- [Decisoes de Arquitetura (ADR)](docs/ADR.md) - Decisoes tecnicas documentadas
- [Guia de Testes](docs/GUIA_TESTES.md) - Como testar a aplicacao

## Variaveis de Ambiente

| Variavel | Descricao | Padrao |
|----------|-----------|--------|
| `JWT_SECRET` | Chave para assinar tokens | (definido no compose) |
| `SPRING_DATASOURCE_URL` | URL do banco | (definido no compose) |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | Servidor Kafka | localhost:9092 |

## Licenca

Projeto desenvolvido para fins educacionais - NTT Data.
