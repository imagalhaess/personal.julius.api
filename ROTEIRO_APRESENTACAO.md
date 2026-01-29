# Roteiro de Apresentação: Julius API (Refatoração)

## 1. Introdução (5 min)
*   **Contexto**: O que é a Julius API (Gestão Financeira Pessoal).
*   **Problema**: Código com duplicação, IDs expostos, tratamento de erro fraco e falta de documentação.
*   **Objetivo da Refatoração**: Arquitetura limpa, escalável e segura (Clean Code, SOLID, KISS).

## 2. Melhorias Arquiteturais (10 min)
*   **Centralização (`ms-common`)**:
    *   **Kafka/Security**: Eliminação de duplicação.
    *   **BrasilAPI**: Cliente centralizado para cotações.
    *   **Tratamento de Erros**: Sistema global de exceções (Issue #15) com `errorCode` e logs estruturados.
*   **Estratégia de IDs**:
    *   `ms-user`: Transição para **Public ID (UUID)** para comunicação externa e **Internal ID (Long)** para persistência.

## 3. Novas Funcionalidades e Evolução (10 min)
*   **Conversão Visual (`ms-transaction`)**:
    *   Demonstração do JSON de resposta com `source_currency`, `exchange_rate` e `converted_amount`.
    *   Uso da BrasilAPI para cotações em tempo real.
*   **Relatórios**:
    *   Nova rota para download de relatórios financeiros em **PDF** e **Excel**.
*   **Processamento Assíncrono (`ms-processor`)**:
    *   Lógica de validação de saldo e uso da BrasilAPI com **lógica de retry** (busca em dias anteriores).

## 4. Qualidade e Documentação (5 min)
*   **OpenAPI**: Documentação completa com `@Operation` e schemas.
*   **Cobertura de Testes**: Cobertura unitária > 80%.
*   **Documentação Técnica**: Apresentação dos novos `README.md`, `README_TESTS.md` e ADRs.

## 5. Conclusão e Próximos Passos (5 min)
*   **Resumo**: Projeto pronto para produção, seguindo as melhores práticas.
*   **Q&A**.
