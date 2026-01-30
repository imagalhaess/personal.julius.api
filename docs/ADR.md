# ADR 003: Estratégia de Identificadores Públicos (UUID)

## Status
Aceito

## Contexto
O sistema utilizava IDs sequenciais (`Long`) expostos diretamente nos endpoints da API. Isso facilitava ataques de enumeração e expunha detalhes da estrutura do banco de dados.

## Decisão
Após revisão de gestor, adotei uma estratégia de IDs duplos:
1.  **Internal ID (BigInt/Long)**: Usado para chaves primárias, relacionamentos entre tabelas e performance de indexação. Nunca é exposto via API.
2.  **Public ID (UUID)**: Gerado no momento da criação do registro. Usado para toda a comunicação externa (endpoints, eventos de integração).

## Consequências
*   **Segurança**: Impede a descoberta do volume de dados por meio de IDs sequenciais.
*   **Desacoplamento**: Permite migrações de banco de dados sem quebrar links externos.
*   **Complexidade**: Requer uma busca adicional por UUID no banco (indexada) antes de realizar operações.

---

# ADR 004: Campos originalAmount e convertedAmount no Payload de Transações

## Status
Aceito (Implementado)

## Contexto
Ao realizar transações com conversão de moeda, o sistema armazenava apenas o valor convertido (`amount`). Isso dificultava a visualização e auditoria do valor original da transação antes da conversão.

## Decisão
Adicionei dois campos explícitos no payload de resposta das transações:
1. **originalAmount**: Valor original informado pelo usuário na moeda de origem.
2. **convertedAmount**: Valor após a conversão para a moeda de destino (quando aplicável).

Para transações sem conversão de moeda, ambos os campos terão o mesmo valor.

## Consequências
*   **Transparência**: O usuário visualiza claramente o valor original e o convertido.
*   **Auditoria**: Facilita rastreamento e conferência de operações com câmbio.
*   **Compatibilidade**: Campos adicionais não quebram integrações existentes.

---

# ADR 005: Migração da Persistência de Transações para ms-processor

## Status
Proposto (TODO)

## Contexto
Atualmente, o `ms-transaction` é responsável por persistir as transações no banco de dados. No entanto, o `ms-processor` já possui toda a lógica de processamento, validação de saldo e conversão de moeda. Essa separação cria uma dependência desnecessária e aumenta a complexidade do fluxo.

## Decisão Proposta
Migrar a responsabilidade de salvar as transações no banco de dados do `ms-transaction` para o `ms-processor`.

### Motivação
1. **Coesão**: O `ms-processor` já processa e valida a transação; faz sentido que ele também persista.
2. **Simplificação**: Reduz a comunicação entre microsserviços.
3. **Atomicidade**: Garante que validação e persistência ocorram na mesma unidade de trabalho.

### Impacto
*   O `ms-processor` precisará de acesso ao banco de transações.
*   O `ms-transaction` se tornará apenas uma camada de consulta/leitura.
*   Eventos Kafka podem ser simplificados ou removidos.

## Consequências Esperadas
*   **Positivas**: Menor latência, código mais coeso, menos pontos de falha.
*   **Negativas**: Requer refatoração significativa e testes de regressão.

---

# ADR 006: Erros de Processamento Persistidos no DLQ

## Status
Aceito (Implementado)

## Contexto
Anteriormente, erros como `CURRENCY_CONVERSION_FAILED`, `EXTERNAL_BANK_ERROR` e `INTERNAL_ERROR` apenas rejeitavam a transação sem manter um registro detalhado da falha. Isso dificultava a análise de problemas e impossibilitava retry automático.

## Decisão
Erros de processamento agora são enviados para o tópico `transaction-dlq` **antes** de rejeitar a transação. O fluxo ficou:

1. Erro ocorre no `ms-processor`
2. Mensagem enviada para `transaction-dlq` com detalhes do erro
3. Resultado `REJECTED` enviado para `transaction-processed`
4. `ms-transaction` persiste o DLQ e atualiza o status da transação

### Erros que vão para o DLQ
| Erro | Causa |
|------|-------|
| `CURRENCY_CONVERSION_FAILED` | Falha ao obter cotação da BrasilAPI |
| `EXTERNAL_BANK_ERROR` | Falha ao consultar saldo no banco externo (MockAPI) |
| `INTERNAL_ERROR` | Exceção não tratada durante processamento |
| `UNEXPECTED_ERROR` | Exceção no consumer antes de chamar o service |

### Erros que NÃO vão para o DLQ
| Erro | Motivo |
|------|--------|
| `INSUFFICIENT_FUNDS` | Regra de negócio válida, não é falha técnica |

## Consequências
*   **Rastreabilidade**: Todo erro técnico fica registrado na tabela `dlq`.
*   **Análise**: Possibilita identificar padrões de falha (ex: API externa instável).
*   **Retry**: Base para implementar reprocessamento automático no futuro.

---

# ADR 007: Integração com Carteira Externa (Somente Leitura)

## Status
Aceito (Implementado)

## Contexto
O sistema precisa validar saldo para transações de despesa (`EXPENSE`) com origem em conta (`ACCOUNT`). Inicialmente, o sistema atualizava o saldo na API externa (MockAPI) após cada transação aprovada, o que não reflete um cenário real de integração bancária.

## Decisão
A integração com carteira externa (MockAPI, simulando Open Finance) é **somente leitura**:

1. **Consulta**: Verificamos se o usuário possui saldo em carteira externa
2. **Validação**: Se tem carteira externa, validamos contra esse saldo
3. **Fallback**: Se não tem carteira externa vinculada, a transação é aprovada automaticamente
4. **Sem escrita**: Não criamos nem atualizamos saldo na API externa

### Fluxo de Validação
```
EXPENSE + ACCOUNT:
  └─ Consulta carteira externa (MockAPI)
       ├─ TEM saldo externo? → Valida se suficiente
       └─ NÃO TEM?           → Aprova (controle interno via transações)
```

### O que foi removido
- `POST /balances` - criar saldo externo
- `PUT /balances/{id}` - atualizar saldo externo
- Método `updateExternalBalance()` no processor

## Consequências
*   **Realismo**: Reflete integração real com Open Finance (somente leitura)
*   **Simplicidade**: Menos pontos de falha, menos código
*   **Controle interno**: Saldo calculado das próprias transações do usuário

---

# TODO - Melhorias Pendentes

## Alta Prioridade

- [ ] **Migrar persistência de transações para ms-processor** (ADR 005)
  - Mover lógica de salvamento do `ms-transaction` para `ms-processor`
  - Atualizar configuração de banco de dados no `ms-processor`
  - Refatorar eventos Kafka (se necessário)
  - Atualizar testes de integração

## Média Prioridade

- [x] ~~Revisar e consolidar tratamento de erros entre microsserviços~~ (ADR 006)
- [ ] Adicionar métricas de observabilidade (Prometheus/Micrometer)

## Baixa Prioridade

- [ ] Documentar endpoints com exemplos no Swagger
- [ ] Implementar cache para consultas frequentes

## Futuro (Longo Prazo)

- [ ] **Integração real com Open Finance**
  - Substituir MockAPI por integração real com APIs bancárias
  - Implementar fluxo de autorização OAuth2 para vinculação de contas
  - Vínculo automático: usuário autoriza → sistema consulta saldo real
  - Sem vínculo: sistema usa saldo interno (calculado das transações)
