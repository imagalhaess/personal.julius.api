# Julius API - Gestão Financeira Pessoal

Sistema de microserviços para controle financeiro, com suporte a múltiplas moedas e integração com APIs externas.

## Tecnologias
*   Java 21 / Spring Boot 4.0.2
*   Kafka (Mensageria)
*   PostgreSQL / TiDB
*   Redis (Caffeine para Cache)
*   BrasilAPI (Cotações em tempo real)

## Arquitetura
O projeto segue os princípios de **Clean Architecture** e **SOLID**, dividido em:
*   `ms-common`: Lógica compartilhada, segurança e tratamento de erros.
*   `ms-user`: Gestão de usuários e autenticação.
*   `ms-transaction`: Registro de transações e relatórios.
*   `ms-processor`: Processamento assíncrono e validações externas.

## Como rodar
1.  Suba a infraestrutura: `docker-compose up -d`
2.  Inicie cada microserviço via Maven ou sua IDE favorita.
3.  Acesse o Swagger: `http://localhost:8080/swagger-ui.html`

## Relatórios
Disponíveis em `/transactions/report/pdf` e `/transactions/report/excel`.
