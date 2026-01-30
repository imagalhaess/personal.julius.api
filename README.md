# Julius API - Gestao Financeira Pessoal

Sistema de microsservicos para controle financeiro, com suporte a multiplas moedas e integracao com APIs externas.

## Tecnologias
*   Java 21 / Spring Boot 4.0.1
*   Apache Kafka (Mensageria assincrona)
*   PostgreSQL (Persistencia)
*   BrasilAPI (Cotacoes de cambio)
*   MockAPI (Validacao de saldo externo)

## Arquitetura
O projeto segue os principios de **Clean Architecture** e **SOLID**, dividido em:
*   `ms-common`: Logica compartilhada, seguranca e tratamento de erros.
*   `ms-user`: Gestao de usuarios e autenticacao (porta 8081).
*   `ms-transaction`: Registro de transacoes e relatorios (porta 8082).
*   `ms-processor`: Processamento assincrono, conversao de moeda e validacoes externas (porta 8080).

## Como rodar

**Windows:**
```cmd
start_dev.bat
```

**Linux/Mac:**
```bash
./start_dev.sh
```

**Ou manualmente:**
1.  Suba a infraestrutura: `docker compose up -d user-db transaction-db kafka kafdrop`
2.  Compile: `mvn clean install -DskipTests`
3.  Inicie cada microsservico com profile dev.

## Swagger UI
*   MS-USER: http://localhost:8081/swagger-ui/index.html
*   MS-TRANSACTION: http://localhost:8082/swagger-ui/index.html

## Monitoramento
*   Kafdrop: http://localhost:9000

## Relatorios
Disponiveis em `/transactions/report/pdf` e `/transactions/report/excel`.
