# Decisoes de Arquitetura (ADR)

## ADR 001: Identificadores Publicos (UUID)

**Status:** Aceito

**Decisao:** IDs internos (Long) para performance + UUIDs publicos para APIs.

**Motivo:** Previne ataques de enumeracao e desacopla estrutura interna.

---

## ADR 002: Campos de Conversao de Moeda

**Status:** Aceito

**Decisao:** Transacoes armazenam `amount` (original), `convertedAmount` (em BRL) e `exchangeRate`.

**Motivo:** Auditoria e transparencia nas operacoes com cambio.

---

## ADR 003: DLQ para Erros Tecnicos

**Status:** Aceito

**Decisao:** Erros tecnicos (falha de API, timeout) vao para `transaction-dlq` e sao persistidos.

| Vai para DLQ | Nao vai para DLQ |
|--------------|------------------|
| `CURRENCY_CONVERSION_FAILED` | `INSUFFICIENT_FUNDS` |
| `EXTERNAL_BANK_ERROR` | (regra de negocio) |
| `INTERNAL_ERROR` | |

---

## ADR 004: Integracao MockAPI (Somente Leitura)

**Status:** Aceito

**Decisao:** MockAPI (simulando Open Finance) eh somente leitura.

**Logica de aprovacao:**

| Tipo | Origem | Validacao |
|------|--------|-----------|
| INCOME | qualquer | Aprovado automaticamente (entrada) |
| EXPENSE | ACCOUNT | Valida saldo externo (se existir) |
| EXPENSE | CASH | Aprovado (sem conta vinculada) |

**Nota:** Se usuario nao tem carteira externa vinculada, transacao EXPENSE+ACCOUNT eh aprovada (controle interno via transacoes).

---

## ADR 005: Cache de Cotacoes (Caffeine)

**Status:** Aceito

**Decisao:** Cache local (Caffeine) para cotacoes da BrasilAPI.

**Configuracao:**
- TTL: 24 horas
- Max entries: 50
- Key: `{moeda}-{data}`

**Motivo:** Cotacoes nao mudam no mesmo dia. Evita chamadas repetidas a API externa.

---

## TODO

- [ ] Migrar persistencia de transacoes para ms-processor (ADR proposto)
- [ ] Adicionar metricas (Prometheus/Micrometer)
- [ ] Integracao real com Open Finance (longo prazo)
