# Architecture Decision Records (ADR)

**Projeto:** Personal Julius API  
**Data:** Janeiro 2026  
**Status:** Aprovado

---

## ADR-001: Apache Kafka 4.0.0 com KRaft (sem Zookeeper)

### Contexto

O projeto exige mensageria assíncrona para comunicação entre componentes e auditoria de eventos.

### Decisão

Apache Kafka 4.0.0 em modo **KRaft** (Kafka Raft):

**Por que Kafka 4.0.0?**
- KRaft como modo padrão (Zookeeper foi removido oficialmente)
- Simplificação de arquitetura: apenas containers Kafka, sem Zookeeper
- Performance superior em eleição de líder e recuperação de falhas
- Futuro-proof: Zookeeper foi descontinuado no Kafka 3.x

**Configuração no docker-compose.yml:**
```yaml
environment:
  KAFKA_PROCESS_ROLES: broker,controller
  KAFKA_CONTROLLER_QUORUM_VOTERS: 1@localhost:9093
```

### Alternativa Considerada

- **Kafka 3.x com Zookeeper:** Modo legado, descontinuado

### Consequências

- Tópicos serão criados automaticamente no primeiro publish (ou via script de inicialização)
- Retenção de mensagens configurável (padrão: 7 dias)
- Suporte a múltiplos consumers por tópico (consumer groups)

---

## ADR-002: Utilizar somente um arquivo application.properties

### Contexto

O Spring Boot permite separar configurações por ambiente utilizando profiles
(application-dev.properties, application-prod.properties, etc.). Essa abordagem
é recomendada para projetos que terão deploy em múltiplos ambientes.

No entanto, este projeto é acadêmico e:

- Não terá deploy em produção
- O banco de dados será sempre o mesmo (local via Docker)
- O Kafka será sempre o mesmo (local via Docker)

### Decisão

Utilizar **apenas um arquivo** `application.properties` com todas as configurações,
sem separação por profiles.

**Princípio aplicado:** KISS (Keep It Simple, Stupid) - evitar complexidade
desnecessária para o contexto do projeto.

### Consequências

**Positivas:**

- Configuração simples e centralizada
- Fácil manutenção e visualização
- Menos arquivos para gerenciar

**Negativas:**

- Não demonstra na prática o uso de profiles
- Credenciais ficam expostas no arquivo (aceitável para projeto acadêmico)

**Nota:** O conceito de profiles foi estudado e compreendido, mas optou-se
conscientemente por não aplicá-lo neste contexto.

---

## ADR-003: Delegação de Agregações Financeiras para a Camada de Persistência

### Contexto

Originalmente, o cálculo de saldo e totais (entradas/saídas) era realizado na camada de aplicação (GetBalanceUseCase),
recuperando a lista completa de transações do usuário e iterando sobre ela em memória. Com a introdução da paginação na
interface TransactionRepository para suportar grandes volumes de dados, a recuperação da lista completa tornou-se
inconsistente com a estratégia de performance do sistema.

### Decisão

Mover a lógica de agregação (soma de valores) para o banco de dados através de queries customizadas (SUM com filtros de
TransactionType) no TransactionJpaRepository. O GetBalanceUseCase agora consome apenas os resultados finais calculados
pela infraestrutura.
*Princípio aplicado*: Performance e Escalabilidade - evitar o tráfego desnecessário de grandes volumes de dados 
(evitando OutOfMemoryError) e aproveitar a otimização do motor de banco de dados para cálculos matemáticos.

### Consequências

**Positivas:**

- Performance: Redução drástica no tráfego de rede e uso de memória da aplicação.
- O código do Use Case tornou-se extremamente limpo e focado na regra de negócio final.

**Negativas:**

- Parte da lógica de "como calcular o saldo" agora reside em uma query SQL na camada de infraestrutura.
- Os testes do Use Case agora dependem de valores pré-calculados vindos do repositório (mockados), perdendo a validação
  da lógica de soma no código Java.

---

## Referências das Documentações Oficiais

Todas as decisões foram baseadas nas documentações oficiais atualizadas:

| Tecnologia | Link da Documentação |
|------------|---------------------|
| Spring Boot 4.0.1 | https://docs.spring.io/spring-boot/docs/current/reference/html/ |
| Spring Data JPA | https://docs.spring.io/spring-data/jpa/reference/ |
| Spring Security 7 | https://docs.spring.io/spring-security/reference/ |
| PostgreSQL 18.1 | https://www.postgresql.org/docs/18/ |
| Apache Kafka 4.0 | https://kafka.apache.org/documentation/#gettingStarted |
| Springdoc OpenAPI | https://springdoc.org/ |
| JUnit 5 | https://junit.org/junit5/docs/current/user-guide/ |
| Mockito | https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html |
| Apache POI | https://poi.apache.org/components/spreadsheet/quick-guide.html |
| Docker | https://docs.docker.com/engine/ |

---

**Status:** Aprovado  
**Data de Revisão:** Não necessária (projeto individual)  
**Responsável:** Isabela M
