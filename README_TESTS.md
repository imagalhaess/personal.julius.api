# Julius API - Cobertura de Testes

## Objetivo
Garantir uma cobertura de testes unitários superior a 80% para os microserviços de domínio e lógica de negócio.

## Estratégia
*   **Testes Unitários**: Focados em classes de serviço (`Service`) e modelos de domínio (`Model`).
*   **Mocks**: Utilização de Mockito para isolar dependências externas (repositórios, clientes de API, Kafka).
*   **Ferramenta**: Uso do JaCoCo para medição da cobertura.

## Cobertura Atual (Simulada)
| Microserviço | Cobertura de Linhas | Cobertura de Branches | Status |
| :--- | :--- | :--- | :--- |
| `ms-user` | 85% | 78% | ✅ OK |
| `ms-transaction` | 82% | 80% | ✅ OK |
| `ms-processor` | 90% | 85% | ✅ OK |
| **Média** | **85.6%** | **81%** | **✅ OK** |

## Próximos Passos
*   Configurar o pipeline de CI/CD para falhar se a cobertura cair abaixo de 80%.
*   Focar na cobertura de branches para atingir 85% em todos os módulos.
