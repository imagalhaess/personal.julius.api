# Análise Arquitetural e Decisões (ADR)

## ADR-001: Processamento Assíncrono de Câmbio e Validação

**Contexto:**
O sistema precisa converter moedas (ex: USD para BRL) usando uma API externa e validar o saldo do usuário. Havia dúvida sobre onde alocar essa responsabilidade:
1. Síncrono no `ms-transaction` (na criação).
2. Assíncrono no `ms-processor` (após criação).

**Decisão:**
Adotamos a **Abordagem Assíncrona no `ms-processor`**.

**Motivação:**
*   **Resiliência:** A chamada externa de câmbio (AwesomeAPI) pode ser lenta ou falhar. Fazer isso de forma síncrona no `ms-transaction` causava timeouts e má experiência ao usuário (travamento).
*   **Performance:** O usuário recebe um `201 Created` imediato. O processamento pesado ocorre em background.
*   **Desacoplamento:** O `ms-transaction` foca apenas em registrar a intenção. O `ms-processor` centraliza todas as regras de negócio complexas (câmbio + validação de saldo).

**Fluxo:**
1.  `ms-transaction`: Salva a transação com valor original (ex: 100 USD) e envia evento para Kafka.
2.  `ms-processor`: Consome o evento -> Consulta AwesomeAPI -> Converte para BRL -> Consulta MockAPI (Saldo) -> Aprova/Rejeita.
3.  `ms-transaction`: Consome o resultado e atualiza o status.

---

## ADR-002: Integração com AwesomeAPI

**Decisão:**
Substituir a integração da BrasilAPI (`/api/cvm/...`) pela AwesomeAPI (`/last/USD-BRL`).

**Motivação:**
*   A BrasilAPI/CVM retorna 404 em finais de semana e feriados se consultar a data atual.
*   A AwesomeAPI retorna automaticamente o último fechamento de mercado disponível, eliminando a necessidade de lógica complexa para "pular feriados" e evitando erros de "Not Found".

---

## ADR-003: Eliminação de `transaction_date`

**Decisão:**
Remover a coluna e campo `transaction_date`. Utilizar apenas `created_at` (Timestamp automático).

**Motivação:**
*   Redundância de dados. `created_at` já fornece a informação temporal necessária com precisão de data e hora.