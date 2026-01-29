# ADR 003: Estratégia de Identificadores Públicos (UUID)

## Status
Aceito

## Contexto
O sistema utilizava IDs sequenciais (`Long`) expostos diretamente nos endpoints da API. Isso facilitava ataques de enumeração e expunha detalhes da estrutura do banco de dados.

## Decisão
Adotamos uma estratégia de IDs duplos:
1.  **Internal ID (BigInt/Long)**: Usado para chaves primárias, relacionamentos entre tabelas e performance de indexação. Nunca é exposto via API.
2.  **Public ID (UUID)**: Gerado no momento da criação do registro. Usado para toda a comunicação externa (endpoints, eventos de integração).

## Consequências
*   **Segurança**: Impede a descoberta do volume de dados por meio de IDs sequenciais.
*   **Desacoplamento**: Permite migrações de banco de dados sem quebrar links externos.
*   **Complexidade**: Requer uma busca adicional por UUID no banco (indexada) antes de realizar operações.
