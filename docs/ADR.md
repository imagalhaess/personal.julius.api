# Architecture Decision Records (ADR)

**Projeto:** Personal Julius API  
**Data:** Janeiro 2026  
**Status:** Aprovado

---

## ADR-001: Versões do Stack Tecnológico

### Contexto

O projeto Personal Julius API é um desafio BECA 2026 que exige o uso de tecnologias modernas e estáveis. As escolhas de versão devem equilibrar:

- **Estabilidade:** Versões maduras e testadas em produção
- **Compatibilidade:** Interoperabilidade entre dependências
- **Modernidade:** Suporte a recursos atuais (Spring Boot 4, Java 21)
- **Suporte de longo prazo:** Documentação atualizada e comunidade ativa

### Decisão

Adotamos as seguintes versões para o stack tecnológico:

| Tecnologia | Versão | Justificativa |
|------------|--------|---------------|
| **Java** | 21 | Versão LTS (Long-Term Support) mais recente. Suporte até 2029. Recursos modernos: pattern matching, virtual threads, record patterns. |
| **Spring Boot** | 4.0.1 | Versão mais recente (lançada em dezembro 2024). Migração completa para Jakarta EE. Suporte nativo a Java 21. |
| **Spring Data JPA** | 4.0.x | Incluída no Spring Boot 4.0.1. Compatibilidade total com Jakarta Persistence. |
| **PostgreSQL** | 18.1 | Versão mais recente (lançada em 2024). Suporte a UUID nativo, JSONB, performance otimizada. |
| **Spring Security** | 7.0.x | Incluída no Spring Boot 4.0.1. Arquitetura moderna de segurança. |
| **JWT (jjwt)** | 0.12.3 | Biblioteca mais popular para JWT em Java. Suporte a algoritmos modernos (HS256, RS256). |
| **Apache Kafka** | 4.0.0 | Versão com KRaft nativo (sem Zookeeper). Simplifica arquitetura. Performance superior. |
| **Apache POI** | 5.5.1 | Versão estável para manipulação Excel (.xlsx). Compatível com Java 21. |
| **Springdoc OpenAPI** | 2.3.0 | Biblioteca padrão para OpenAPI 3 no Spring Boot 4. Substitui Springfox (descontinuado). |
| **JUnit** | 6.0.2 | JUnit Jupiter - versão mais recente. Suporte completo a Java 21 e anotações modernas. |
| **Mockito** | 5.21.0 | Framework de mocks mais popular. Compatibilidade com JUnit 5. |
| **Docker** | 29.1.5 | Versão mais recente do Docker Engine. Suporte completo a docker-compose v3. |

### Consequências

#### Positivas

- **Compatibilidade garantida:** Todas as versões foram testadas juntas e são oficialmente compatíveis
- **Suporte oficial:** Spring Boot 4.0.1 garante compatibilidade transitiva entre dependências
- **Recursos modernos:** Java 21 + Spring Boot 4 permitem uso de records, virtual threads, pattern matching
- **Jakarta EE:** Alinhamento com o padrão Jakarta (migração completa de `javax.*` para `jakarta.*`)
- **Kafka KRaft:** Elimina dependência do Zookeeper, simplificando deploy e manutenção
- **Documentação atualizada:** Todas as tecnologias têm documentação oficial atualizada para 2026

#### Negativas

- **Migração Jakarta:** Dependências antigas que usam `javax.*` não funcionarão (requer versões atualizadas)
- **Breaking changes:** Spring Boot 4 tem breaking changes em relação ao 3.x (requer atenção na configuração)
- **Kafka 4.0.0:** Como é versão recente, pode ter menos exemplos práticos disponíveis (documentação oficial compensa)

#### Mitigações

- **Documentação preparada:** Guias completos do projeto incluem referências das documentações oficiais
- **Validação de dependências:** pom.xml será validado antes do início do desenvolvimento
- **Uso de starters oficiais:** Priorizamos Spring Boot Starters para evitar conflitos de versão

---

## ADR-002: Apache Kafka 4.0.0 com KRaft (sem Zookeeper)

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

## ADR-003: JUnit 6 + Mockito 5 para Testes

### Contexto

Testes automatizados são obrigatórios com cobertura mínima de 80%.

### Decisão

**JUnit 6.0.2 (JUnit Jupiter):**
- Anotações modernas: `@Test`, `@BeforeEach`, `@ParameterizedTest`
- Melhor integração com Spring Boot Test
- Suporte a testes parametrizados nativamente

**Mockito 5.21.0:**
- Framework de mocks mais popular no ecossistema Java
- Sintaxe clara: `when(...).thenReturn(...)`
- Integração perfeita com JUnit 5 via `@ExtendWith(MockitoExtension.class)`

**JaCoCo (Code Coverage):**
- Plugin Maven para relatório de cobertura
- Meta: >80% de cobertura de linhas

### Consequências

- Testes unitários para domain layer (entidades, value objects, use cases)
- Testes de integração para repositories (com `@DataJpaTest`)
- Testes de API com `@SpringBootTest` e `MockMvc`

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
