# Auditoria Técnica — Digital Banking API

> **Tipo:** Revisão técnica de código (somente leitura — nenhum código foi alterado).
> **Foco:** portfólio Back-End Java para vagas de estágio/desenvolvimento em instituições financeiras.
> **Stack:** Java 25 · Spring Boot 4.1.1 · Spring Data JPA/Hibernate · MySQL 8.4.7 · Maven Wrapper · springdoc-openapi 3.1.1 · JUnit 5 + Mockito.
> **Data:** 2026-10-11
> **Regra de honestidade técnica:** *Problema confirmado* = verificável no código/execução. *Risco potencial* = plausível, mas **não** comprovado por teste. Nada foi inventado; autoria e histórico de desenvolvimento não foram presumidos.

---

## 1. Sumário executivo

O projeto é um exemplar **acima da média** para portfólio: arquitetura em camadas limpa, DTOs como `record`, tratamento global de exceções, uso correto de `BigDecimal`, locks pessimistas em ordem crescente de id e suíte de 37 testes organizada (unitário / slice / integração). O build é reprodutível (`clean package` → 37 testes + JAR executável).

As lacunas mais relevantes **não são de código "quebrado"**, e sim de **segurança e de comprovação**:

1. **Não há autenticação/autorização** (esperado num estudo, mas crítico no contexto bancário).
2. **JDBC sem TLS** (`useSSL=false`) — credenciais e CPF trafegam sem criptografia.
3. **Alegações de segurança transacional/anti-deadlock não são comprovadas por testes** — o design é bom, mas hoje é *afirmação*, não *evidência*.
4. **Sem idempotência** em transferências — risco de operações duplicadas.
5. **Condição de corrida (TOCTOU)** nas checagens de unicidade, com `DataIntegrityViolationException` sem tratamento → 500 em vez de 409.
6. **PII (CPF/e-mail) sem mascaramento**.

Nada disso impede a publicação como **portfólio**, desde que o README explicite as limitações (o que em parte já faz) e que as alegações sejam ajustadas para o que é comprovável.

---

## 2. Inventário de arquivos analisados

### 2.1 Build e configuração
| Arquivo | Observação |
|---|---|
| `pom.xml` | Parent Spring Boot 4.1.1, 7 dependências, 1 plugin. |
| `.mvn/wrapper/maven-wrapper.properties` | Maven 3.9.16. |
| `mvnw` / `mvnw.cmd` | Wrapper (tracked). |
| `.gitignore` | Cobre Java, Maven, IntelliJ, Eclipse, VS Code, segredos. |
| `.env.example` | Placeholders fictícios. |
| `README.md` | Documentação em inglês. |

### 2.2 Recursos
| Arquivo | Observação |
|---|---|
| `src/main/resources/application.properties` | 13 linhas de config. |
| `src/main/resources/db/schema.sql` | DDL manual, idempotente. |

### 2.3 Código principal (32 fontes)
- **Entry point:** `DigitalBankingApiApplication.java`
- **config:** `OpenApiConfig.java`
- **controller (3):** `ClienteController`, `ContaController`, `TransferenciaController`
- **dto/cliente (2):** `ClienteRequest`, `ClienteResponse`
- **dto/comum (2):** `ApiErrorResponse`, `PageResponse`
- **dto/conta (2):** `ContaRequest`, `ContaResponse`
- **dto/operacao (3):** `TransacaoResponse`, `TransferenciaRequest`, `ValorRequest`
- **entity (3):** `Cliente`, `Conta`, `Transacao` · **enums (1):** `TipoTransacao`
- **exception (8):** `BusinessException` (abstrata) + 7 específicas
- **handler (1):** `GlobalExceptionHandler`
- **repository (3):** `ClienteRepository`, `ContaRepository`, `TransacaoRepository`
- **service (2):** `ClienteService`, `ContaService`

### 2.4 Testes (6 classes / 37 métodos)
- **service:** `ClienteServiceTest` (5), `ContaServiceTest` (12)
- **controller (MockMvc):** `ClienteControllerTest` (5), `ContaControllerTest` (9), `TransferenciaControllerTest` (4)
- **integração (H2):** `OpenApiDocumentationTest` (2)

### 2.5 Estado do Git
- Repositório **inicializado localmente** (`git init`), **sem commits e sem push**.
- Verificação `git add -A -n`: **48 arquivos** seriam versionados — **`target/` ausente (0 entradas)**, sem `.env`, sem JARs, sem segredos.
- **Nenhuma senha/credencial encontrada** no código: `application.properties:7` usa `${DB_PASSWORD}`; `OpenApiDocumentationTest.java:20` tem senha **vazia** (H2, escopo de teste); `mvnw` apenas cita o nome de variável `MVNW_PASSWORD`.

---

## 3. Pontos positivos comprovados

| # | Ponto | Evidência |
|---|---|---|
| P-01 | Arquitetura **Controller → Service → Repository** sem vazamento de entidades na API. | Controllers retornam DTOs; `ClienteController.java`, `ContaController.java`. |
| P-02 | DTOs **imutáveis** via `record`. | Pacotes `dto/*`. |
| P-03 | **Tratamento global de exceções** com corpo padronizado e status coerente. | `GlobalExceptionHandler.java:25-76`. |
| P-04 | **Bean Validation** nas entradas + validação de regra no serviço (defesa em profundidade). | `ValorRequest`, `TransferenciaRequest`; `ContaService.validarValor` (141-149). |
| P-05 | **Money com `BigDecimal`**, escala validada (≤ 2 casas) e `setScale(2, HALF_EVEN)`. | `ContaService.java:141-149`; `Conta.java:38`; `schema.sql:37,58` (`DECIMAL(19,2)`). |
| P-06 | **Locks pessimistas em ordem crescente de id** para transferências. | `ContaRepository.java:17-19`; `ContaService.java:113-120`. |
| P-07 | **Transactionalidade** nas operações de escrita. | `@Transactional` em `ContaService.depositar/sacar/transferir`. |
| P-08 | **Unicidade garantida no banco** (cpf, email, numero) além da checagem na aplicação. | `schema.sql:25-26,40`; entidades `Cliente.java:35,41`, `Conta.java:34`. |
| P-09 | **Extrato paginado** com limite máximo de página. | `ContaController` (`TAMANHO_MAXIMO_PAGINA=100`). |
| P-10 | **OpenAPI/Swagger** com metadados e exemplos. | `OpenApiConfig.java`; anotações nos controllers. |
| P-11 | **37 testes** em camadas (unitário, slice, integração) — todos verdes. | `mvnw clean package`. |
| P-12 | **Segredos fora do versionamento** e credenciais por variável de ambiente. | `.gitignore`; `application.properties:6-7`. |
| P-13 | **Schema manual**, idempotente, sem DROP/TRUNCATE, `ddl-auto=none`. | `schema.sql`; `application.properties:10-12`. |

---

## 4. Problemas encontrados

> Formato: **ID — Severidade — Título** · Arquivo:linha · Evidência · Risco · Solução · Testes.

### 4.1 CRÍTICA

#### C-01 — Ausência total de autenticação e autorização
- **Arquivo/linha:** projeto inteiro; ausência em `pom.xml:26-71` (não há `spring-boot-starter-security`).
- **Evidência (confirmada):** nenhum `SecurityFilterChain`, nenhum filtro, nenhuma anotação de autorização. Todos os endpoints são públicos.
- **Risco:** qualquer cliente pode cadastrar/consultar clientes e movimentar contas. Para uma API bancária é a falha mais grave; em avaliação para instituição financeira, é o primeiro item observado.
- **Solução recomendada:** mínimo — Spring Security com HTTP Basic + papéis (`CLIENTE`/`ADMIN`), endpoints de leitura/escrita protegidos; documentar como projeto de estudo. Alternativa para portfólio: manter público, mas **deixar explícito** no README (já há seção de limitações) **e** adicionar "Roadmap: autenticação" para não parecer descuido.
- **Testes necessários:** 401 sem credenciais; 403 com papel insuficiente; 200 com credenciais válidas.

#### C-02 — Conexão JDBC sem TLS
- **Arquivo/linha:** `application.properties:5` (`useSSL=false&allowPublicKeyRetrieval=true`).
- **Evidência (confirmada):** `useSSL=false` desabilita criptografia; `allowPublicKeyRetrieval=true` permite intercâmbio de chave pública (necessário para `caching_sha2_password` sem TLS), o que habilita ataques MITM na rede local.
- **Risco:** credenciais do banco e dados (CPF/e-mail/valores) em texto claro no tráfego de rede.
- **Solução recomendada:** usar `sslMode=REQUIRED` (ou `VERIFY_CA`) com certificado; remover `allowPublicKeyRetrieval` em ambientes com TLS. Para desenvolvimento local, manter e **documentar** explicitamente como decisão de ambiente. Alternativa: externalizar via variável `DB_URL`.
- **Testes necessários:** não aplicável a teste automatizado; verificar configuração por revisão/checklist de deploy.

### 4.2 ALTA

#### A-01 — Transferências sem idempotência
- **Arquivo/linha:** `ContaService.java:105-129`; reconhecido em `ContaService.java:101-103` e `README.md:586-587`.
- **Evidência (confirmada):** não há chave de idempotência; um retry de cliente (timeout/rede) cria uma segunda transferência idêntica.
- **Risco:** duplicação de débito/crédito — exatamente o tipo de incidente que um banco monitora.
- **Solução recomendada:** receber `Idempotency-Key` no header, persistir (tabela única) e retornar o resultado anterior para chave repetida. Envolve mudança de schema (decisão sua — ver Perguntas).
- **Testes necessários:** mesma chave, duas chamadas → um único débito; chave diferente → dois débitos.

#### A-02 — Alegações de concorrência/anti-deadlock não comprovadas
- **Arquivo/linha:** `README.md:43`; `ContaService.java:96-104`; `TransferenciaController.java:37`.
- **Evidência (confirmada de que a afirmação existe / confirmada de que a prova não existe):** o Javadoc/README afirmam "preventing negative balances and deadlocks", mas não há teste de concorrência. Os testes de `ContaServiceTest` usam **mocks** (`findByIdWithLock` stubbado) e **não** exercem transação, banco, nem threads.
- **Risco:** a afirmação pode ser contestada em entrevista/ação de RH como "não demonstrada". Design correto ≠ comportamento comprovado.
- **Solução recomendada:** (a) adicionar testes de concorrência (Item 6 / Etapa E2) que provem ausência de saldo negativo; **ou** (b) enquanto não houver teste, **suavizar** a redação do README/Javadoc para "designed to prevent… (pending concurrency tests)". Recomendo fazer as duas.
- **Testes necessários:** N threads transferindo entre as mesmas 2 contas; assertar soma dos saldos constante e nenhum saldo negativo; transferências cruzadas A→B e B→A concorrentes sem deadlock.

#### A-03 — Condição de corrida (TOCTOU) nas checagens de unicidade + exceção não tratada
- **Arquivo/linha:** `ClienteService.java:29-34` (`existsByCpf`/`existsByEmail`), `ContaService.java:151-160` (`existsByNumero`); `GlobalExceptionHandler.java:22-83` (sem handler para integridade).
- **Evidência (confirmada):** o padrão "checar depois inserir" não é atômico. Duas requisições simultâneas passam na checagem; a segunda viola a constraint de banco (`uk_cliente_cpf`/`uk_cliente_email`/`uk_conta_numero`, `schema.sql:25-26,40`) e o Hibernate lança `DataIntegrityViolationException`, **não mapeada** → cai no `@ExceptionHandler(Exception.class)` (`GlobalExceptionHandler.java:73-76`) → **HTTP 500**.
- **Risco:** resposta incorreta (500 em vez de 409) sob concorrência; experiência de API inconsistente.
- **Solução recomendada:** manter a checagem amigável **e** capturar `DataIntegrityViolationException` mapeando para **409** com mensagem genérica; opcionalmente inspecionar qual constraint falhou. O índice único é a fonte de verdade.
- **Testes necessários:** simular violação de constraint no serviço (`save` lança `DataIntegrityViolationException`) → esperar 409.

#### A-04 — Exposição de dados pessoais (PII) sem mascaramento
- **Arquivo/linha:** `ClienteResponse.java` (cpf/email), `ClienteController` (`GET /clientes/{id}`), `ContaResponse.java` (`clienteNome`).
- **Evidência (confirmada):** CPF e e-mail são retornados integralmente; não há mascaramento, minimização, nem controle de acesso (ver C-01).
- **Risco:** vazamento de PII; inadequação a LGPD; em banco real, dado sensível trafegando em claro (somado a C-02).
- **Solução recomendada:** mascarar CPF (`***.***.789-01`) na resposta pública; criar endpoint/DTO reduzido; quando houver segurança, proteger os campos por papel.
- **Testes necessários:** assertar formato mascarado; garantir que o CPF completo nunca apareça no JSON.

#### A-05 — Swagger/OpenAPI habilitado sem restrição
- **Arquivo/linha:** `pom.xml:32-36`; `application.properties` (sem flags de desativação); aviso em runtime do `SpringDocAppInitializer`.
- **Evidência (confirmada):** `/v3/api-docs` e `/swagger-ui/index.html` acessíveis por padrão em qualquer ambiente.
- **Risco:** expõe o mapa completo da API e modelos de dados; em produção, vetor de reconhecimento.
- **Solução recomendada:** perfis Spring (`application-prod.properties`) com `springdoc.api-docs.enabled=false` e `springdoc.swagger-ui.enabled=false`. Manter habilitado em `dev`.
- **Testes necessários:** teste por perfil verificado no perfil prod.

#### A-06 — Handler global captura `Exception` sem logging
- **Arquivo/linha:** `GlobalExceptionHandler.java:73-76`.
- **Evidência (confirmada):** não há `Logger`/`log.error` na classe; exceções inesperadas devolvem 500 **sem rastro** nos logs.
- **Risco:** baixa observabilidade; dificulta diagnóstico.
- **Solução recomendada:** adicionar `Logger` (SLF4J) e registrar o erro com stack trace no handler genérico (sem vazar detalhes ao cliente).
- **Testes necessários:** opcional — verificar via spy de logger (ou aceitar cobertura por revisão).

#### A-07 — Inconsistência de fuso horário
- **Arquivo/linha:** `application.properties:5` (`serverTimezone=UTC`); `Transacao.java:67-72` (`LocalDateTime.now()` usa o fuso padrão da JVM); `Transacao.java:40-41` (`DATETIME`).
- **Evidência (confirmada no código / risco potencial no comportamento):** horário gravado a partir do relógio local da JVM, mas o driver é instruído a tratar o servidor como UTC. Dependendo de `connectionTimeZone`/`preserveInstants`, pode haver deslocamento no extrato.
- **Risco:** horas incorretas em extratos/auditoria — inaceitável em contexto financeiro.
- **Solução recomendada:** padronizar em UTC — usar `Instant` (ou `LocalDateTime.now(Clock.systemUTC())`), e alinhar `hibernate.jdbc.time_zone=UTC`. Definir a decisão (ver Perguntas).
- **Testes necessários:** gravar transação e verificar hora esperada com `Clock` fixo.

### 4.3 MÉDIA

#### M-01 — Métodos de repositório mortos (código não utilizado) — RESOLVIDO
- **Arquivo/linha:** `ClienteRepository` (`findByCpf`, `findByEmail`); `ContaRepository` (`findByNumero`).
- **Evidência (confirmada):** busca no projeto mostra que essas assinaturas só aparecem na própria declaração — nenhuma chamada.
- **Correção desta auditoria:** `TransacaoRepository.findByContaOrigemIdOrderBy...` e `findByContaDestinoIdOrderBy...` (listados erroneamente aqui na versão anterior) **não** são mortos — são exercitados por `RepositorioMySqlIntegrationTest` (linhas 77 e 83).
- **Risco:** ruído/manutenção; sugere funcionalidade que não existe.
- **Solução (aplicada):** removidos os 3 métodos confirmados.
- **Testes:** remoção não quebra nada (cobertura atual cobre apenas os usados).

#### M-02 — Sem lock otimista (`@Version`)
- **Arquivo/linha:** `Conta.java` (sem campo `@Version`).
- **Evidência (confirmada):** existe apenas lock pessimista em transferência; updates fora do fluxo `findByIdWithLock` não têm controle otimista.
- **Risco:** perda de atualização / last-write-wins em cenários futuros.
- **Solução:** avaliar adicionar `@Version` à `Conta` (coluna nova → mudança de schema).
- **Testes:** update concorrente detecta conflito.

#### M-03 — Tratamento incompleto de erros HTTP padrão
- **Arquivo/linha:** `GlobalExceptionHandler.java:22-83`.
- **Evidência (confirmada):** não há handler para `HttpRequestMethodNotSupportedException` (405), `MissingServletRequestParameterException` (400) nem `ConstraintViolationException`. Caem no genérico/`Exception` → formato possivelmente diferente do `ApiErrorResponse`.
- **Risco:** respostas de erro inconsistentes.
- **Solução:** adicionar handlers específicos.

#### M-04 — CPF validado apenas por tamanho (sem dígito verificador)
- **Arquivo/linha:** `ClienteService.java:51-55` (`matches("\\d{11}")`).
- **Evidência (confirmada):** CPF como `11111111111` é aceito.
- **Risco:** dado inválido persistido; num banco, gera retrabalho.
- **Solução:** implementar validação dos dígitos verificadores.
- **Testes:** CPFs válidos/inválidos (incl. repetidos).

#### M-05 — Limite de magnitude monetária só no DTO
- **Arquivo/linha:** `ValorRequest.java` / `TransferenciaRequest.java` (`@Digits(integer=17, fraction=2)`); `ContaService.validarValor` (`141-149`) valida apenas escala e positividade.
- **Evidência (confirmada):** chamadas diretas ao serviço (fora do MVC) não checam o limite de 17 dígitos inteiros; valor > `DECIMAL(19,2)` causaria erro de banco.
- **Risco:** `DataIntegrityViolationException` em produção (→ 500).
- **Solução:** validar magnitude também no serviço.
- **Testes:** valor com 18 dígitos inteiros → 400.

#### M-06 — Validação redundante e inconsistente DTO × Service — RESOLVIDO
- **Arquivo/linha:** `ClienteRequest.java` (cpf) × `ClienteService.validarCpf` (exige 11 dígitos).
- **Evidência (confirmada):** o DTO aceitava até 14 caracteres (`@Size(max=14)`) enquanto o `@Schema` (`maxLength=11`) e o serviço exigem 11 dígitos.
- **Solução (aplicada):** DTO alinhado para `@Size(min=11, max=11)`; o `ClienteService` permanece como defesa em profundidade (normaliza e valida os dígitos verificadores).

#### M-07 — Configuração morta
- **Arquivo/linha:** `application.properties:13` (`spring.jpa.properties.hibernate.format_sql=true`) com `show-sql=false`.
- **Evidência (confirmada):** `format_sql` não tem efeito sem SQL sendo exibido.
- **Solução:** remover ou habilitar coerência (usar logger de SQL em dev).

#### M-08 — Índices redundantes no schema
- **Arquivo/linha:** `schema.sql:63-66` — `idx_transacao_origem` é prefixo de `idx_transacao_origem_data_hora`; o mesmo para `idx_transacao_destino`.
- **Evidência (confirmada):** índice de coluna única é coberto pelo composto à esquerda.
- **Risco:** custo de escrita/espaço desnecessário.
- **Solução:** manter apenas os compostos.

#### M-09 — README cita licença MIT sem arquivo `LICENSE`
- **Arquivo/linha:** `README.md:603`; `OpenApiConfig.java:33-34`.
- **Evidência (confirmada):** não existe arquivo `LICENSE` no repositório.
- **Solução:** adicionar `LICENSE` (MIT) coerente, ou remover a menção.

#### M-10 — Sem teste de integração com MySQL real
- **Arquivo/linha:** único teste de integração é `OpenApiDocumentationTest.java` (H2), e ele **não executa consultas** de negócio.
- **Evidência (confirmada):** nenhuma execução contra MySQL 8.4.7; dialect, JPQL de lock (`ContaRepository:17-19`), queries derivadas do extrato e tipos `DATETIME(6)`/`DECIMAL(19,2)` não são validados no banco real.
- **Risco:** diferenças H2×MySQL passam despercebidas.
- **Solução:** `@DataJpaTest` com Testcontainers MySQL (perfil de teste), validando as queries e o lock.

#### M-11 — Testes de serviço com repositórios mockados não validam queries/locks em runtime
- **Arquivo/linha:** `ContaServiceTest.java` (todo o arquivo), `ClienteServiceTest.java`.
- **Evidência (confirmada):** `findByIdWithLock` e `save` são stubbados; não há prova de que a JPQL com `@Lock` funciona.
- **Risco:** a suíte passa mesmo se a query ficar inválida.
- **Solução:** complementar com teste de repositório (ver M-10).

### 4.4 BAIXA

#### B-01 — Paginação coage valores inválidos silenciosamente
- **Arquivo/linha:** `ContaController` (`Math.max(page,0)`, `normalizarTamanho`).
- **Evidência (confirmada):** `page=-5` vira `0`; `size=0` vira `1` — sem 400.
- **Solução:** aceitar a coerção **ou** validar e retornar 400 (decisão de UX).

#### B-02 — Mistura de idiomas em nomes de métodos/varáveis
- **Evidência (confirmada):** métodos e handlers em português (consistente no projeto), mas convivendo com termos em inglês (Spring). Não é defeito; apenas estilo — manter consistência.

#### B-03 — Logging/observabilidade ausentes
- **Arquivo/linha:** projeto (sem `logback-spring.xml`, sem handlers de log).
- **Solução:** adicionar logs estruturados (request id, latência) quando fizer sentido.

#### B-04 — Warnings de build futuros
- **Evidência (confirmada no output do Maven):** aviso do Mockito "self-attaching to enable the inline-mock-maker" e de dynamic agent loading.
- **Solução:** configurar Mockito como agente Java do Surefire para silenciar e ser fut-proof.

---

## 5. Análise por etapa

### 5.1 Etapa 2 — Arquitetura e POO
- **Aprovado:** separação de camadas, injeção por construtor, DTOs `record`, exceções de domínio com base abstrata (`BusinessException`), `equals/hashCode` por `id` com `getClass().hashCode()` (padrão JPA correto), `open-in-view=false`.
- **A melhorar:** redundância de validação (M-06), `getSelene`... nada de acoplamento grave; entidades não são expostas.
- **Ausente:** nenhuma interface de serviço (aceitável em projetos pequenos; em banco grande, interfaces ajudam testes/mocks — item de discussão, não erro).

### 5.2 Etapa 3 — Regras bancárias
- **BigDecimal/precisão (aprovado):** escala validada, `HALF_EVEN`, `DECIMAL(19,2)` alinhado entidade↔schema.
- **Atomicidade (aprovado no design, não comprovado):** débito+crédito na mesma `@Transactional`; lock pessimista em ordem crescente reduz deadlock.
- **Não comprovado:** ausência de saldo negativo e ausência de deadlock **não têm teste** (A-02). É a maior dívida "de alegação".
- **Idempotência:** ausente (A-01).
- **Consistência saldo × extrato:** não há teste somando transações e comparando ao saldo (gap — Etapa 5).

### 5.3 Etapa 4 — Segurança
- Sem auth/authz (C-01); JDBC sem TLS (C-02); PII exposta (A-04); Swagger aberto (A-05); sem logging de erro (A-06).
- **Segredos:** **OK** — nada versionado; `.gitignore` eficaz (verificado com `git add -n`); credenciais por env.
- **Não expor senhas no relatório:** nenhuma senha real foi acessada ou incluída (a única ocorrência é placeholder/variável vazia de teste).

### 5.4 Etapa 5 — Testes (cobertura e lacunas)
**Cobertos (confirmado):** criação/normalização/duplicidade de cliente; criação de conta; colisão de número; depósito/saque/transferência felizes; saldo insuficiente (saque e transferência); `valor` não positivo; mesma conta; ordem de lock; ausência de cliente/conta; validação MVC; Swagger.

**Não cobertos (confirmado):**
1. Concorrência real (saldo negativo, deadlock).
2. Rollback/atomicidade comprovada (falha no meio da transferência).
3. Idempotência.
4. Violação de constraint de banco → 409 (A-03).
5. `AccountNumberGenerationException` (esgotar 20 tentativas).
6. `@Digits` > 2 casas via HTTP e magnitude > 17 dígitos.
7. Limites de paginação (size>100, page<0, size=0).
8. Ordenação do extrato e inclusão como origem **e** destino (transferência aparece para ambas as contas).
9. Consistência `saldo == Σ transações`.
10. Fuso horário (A-07).
11. Repositório/JPQL/`@Lock` em banco.
12. Integração MySQL real (M-10).

### 5.5 Etapa 6 — Documentação vs código
- **Correto:** nº de testes (37), portas/paths do Swagger, limites de paginação (1–100), `DECIMAL(19,2)`, exemplos JSON coerentes com DTOs, seção de limitações de segurança.
- **A ajustar:**
  - `README.md:43` afirma prevenção de saldo negativo/deadlock **sem teste** → suavizar para "designed to…" (ver A-02).
  - `README.md:603`/`OpenApiConfig.java:33` citam MIT sem `LICENSE` (M-09).
  - Reforçar que os testes de integração usam **H2**, não MySQL.

---

## 6. Prioridades antes da publicação no GitHub

**Bloqueantes leves (rápidos e de alto valor)**
1. Suavizar alegações não comprovadas (A-02 · README/Javadoc). *(sem código de negócio)*
2. Adicionar `LICENSE` ou remover menção MIT (M-09).
3. Confirmar `ddl-auto=none` e remover config morta (M-07).
4. Documentar explicitamente H2-nos-testes e limitações (Etapa 6).

**Alta prioridade (esforço moderado)**
5. Mapear `DataIntegrityViolationException` → 409 (A-03).
6. Adicionar logging no handler genérico (A-06).
7. Testes de concorrência + consistência saldo×extrato (A-02, Etapa 5).
8. Padronizar fuso em UTC (A-07).

**Média prioridade (evolução)**
9. Remover métodos mortos (M-01) ou criar features que os usem.
10. Validação de CPF com dígito verificador (M-04).
11. Handlers 405/400 faltantes (M-03).
12. `@DataJpaTest` + Testcontainers MySQL (M-10/M-11).

**Opcional/estratégico (diferencial de portfólio)**
13. Spring Security com Basic Auth + papéis (C-01).
14. Idempotência de transferências (A-01).
15. Mascaramento de PII (A-04).
16. Perfis com Swagger desabilitado em prod (A-05).

---

## 7. Perguntas de entrevista sobre o projeto

1. Por que `BigDecimal` em vez de `double`? O que é `HALF_EVEN` e por que a escala é validada antes de arredondar?
2. Como a transferência garante atomicidade? O que aconteceria se o crédito falhasse após o débito?
3. Por que **lock pessimista** e não otimista aqui? Em que cenário o otimista seria melhor?
4. Por que bloquear as contas em **ordem crescente de id**? Como isso evita deadlock?
5. Como você provaria (teste) que duas transferências concorrentes não geram saldo negativo? *(Ponto de atenção atual.)*
6. `ddl-auto=none` + `schema.sql` manual: quais as vantagens em relação a `create-drop`? Onde entra Flyway/Liquibase?
7. O que é o problema TOCTOU no `existsByCpf` + `save`, e como o índice único e o tratamento de `DataIntegrityViolationException` resolvem?
8. Por que `open-in-view=false`? Como isso evita o anti-padrão de lazy loading na view?
9. Como o `@RestControllerAdvice` transforma exceções de domínio em status HTTP? Por que não vazar stack trace?
10. Diferença entre teste de slice (`@WebMvcTest`), unitário com Mockito e integração (`@SpringBootTest` + H2/Testcontainers). Que risco existe em subir tudo com H2?
11. Como a API trata dados pessoais (LGPD)? O que é minimização e mascaramento de PII?
12. O que é idempotência em pagamentos/transferências e como implementaria uma `Idempotency-Key`?
13. Por que `useSSL=false` é aceitável só em dev? Como configurar TLS com o MySQL?
14. Se `size` da paginação vier 100000, o que acontece? Como proteger contra abuso?
15. Um endpoint que devolve 500 em vez de 409: qual erro de arquitetura isso revela?

---

## 8. Plano de melhorias por etapas

### Etapa A — Higiene e honestidade documental (sem risco)
- Ajustar README/Javadoc (A-02), adicionar `LICENSE` (M-09), remover config morta (M-07), documentar H2 nos testes (Etapa 6). *(arquivos de doc/config apenas)*

### Etapa B — Robustez de erro e observabilidade
- Handler para `DataIntegrityViolationException` → 409 (A-03); handlers 405/400 (M-03); logging (A-06). Testes: violação de constraint, método não suportado. *(sem tocar o banco)*

### Etapa C — Correção monetária/temporal
- Fuso UTC (A-07); limite de magnitude no serviço (M-05); validação de CPF com dígito verificador (M-04). Testes correspondentes.

### Etapa D — Prova de segurança transacional
- Testes de concorrência (A-02) e de rollback/atomicidade; teste de consistência saldo×extrato.

### Etapa E — Qualidade de dados e banco
- `@DataJpaTest` + Testcontainers MySQL (M-10/M-11); limpar índices redundantes no schema (M-08); avaliar `@Version` (M-02).

### Etapa F — Segurança (estratégica)
- Spring Security + papéis (C-01); mascaramento de PII (A-04); perfis com Swagger off (A-05); idempotência (A-01) — negocia a mudança de schema.

Cada etapa entra com testes novos; a suíte cresce de 37 para ~50+.

---

## 9. Perguntas para o desenvolvedor decidir (participação)

1. **Escopo de segurança:** quer deixar a API **pública** como portfólio (documentando limitações) ou adicionar **Spring Security** básico já nesta rodada? (impacto: esforço × nota de avaliação)
2. **Idempotência:** aceita **alterar o schema** (nova coluna/tabela de idempotência) ou prefere manter o schema congelado e apenas documentar?
3. **Fuso horário:** migrar para **UTC/`Instant`** (mudança de comportamento) ou manter `LocalDateTime` e ajustar apenas o `hibernate.jdbc.time_zone`?
4. **CPF:** implementar **dígito verificador** agora ou manter validação de formato?
5. **Testes de integração:** introduzir **Testcontainers (Docker)** no projeto — disponível na sua máquina/CI?
6. **Métodos mortos:** **remover** ou **criar endpoints** (ex.: buscar cliente por CPF) para justificá-los?
7. **Alegações do README:** suavizar agora para "designed to…" e depois comprovar com testes, ou manter e comprovar antes de publicar?
8. **Mascaramento de PII:** mascarar CPF/e-mail nas respostas públicas pode conflitar com a ideia de "CRUD demonstrativo" — prefere mascarar ou expor como está (documentado)?

---

## 10. Conclusão

O projeto está **pronto para ser publicado como portfólio**, desde que: (a) as **alegações** de concorrência sejam suavizadas até existir teste, e (b) as **limitações de segurança** (já parcialmente descritas) fiquem claras. Nenhum **código de negócio** foi alterado nesta auditoria; não toquei no MySQL, nas tabelas, nos saldos nem nas credenciais, não criei commits e não publiquei nada.

**Nenhuma evidência foi inventada.** Itens marcados como *confirmados* foram verificados por leitura de código/execução de build; itens marcados como *risco potencial* dependem de configuração em tempo de execução (ex.: fuso horário).

---

## 11. Anexo — Status das correções aplicadas

> Atualizado a cada etapa aprovada. Suíte: **55 testes**, todos verdes (`mvnw test`). Sem commits/push; banco/schema intactos.

### Etapa A — Higiene documental (concluída)
| Item | Ação | Arquivo |
|---|---|---|
| A-02 | Alegação de concorrência suavizada para "designed to…" | `README.md` (Features), Javadoc `ContaService.transferir`, descrição `TransferenciaController` |
| M-09 | Criado arquivo de licença MIT; README alinhado | `LICENSE`, `README.md` |
| M-07 | Removida config morta `hibernate.format_sql=true` | `application.properties` |
| C-02 | Limitação de JDBC sem TLS documentada | `README.md` (Security Limitations) |
| — | Nova seção **Roadmap** com pendências | `README.md` |

### Etapa B — Robustez de erro e observabilidade (concluída)
| Item | Ação | Evidência |
|---|---|---|
| A-03 | `DataIntegrityViolationException` → **409** (mensagem genérica) | `GlobalExceptionHandler.tratarViolacaoIntegridade`; teste `ClienteControllerTest` |
| A-06 | Logging: `log.error` no handler genérico e `log.warn` em 409/405 | `GlobalExceptionHandler` (SLF4J) |
| M-03 | Handlers novos: **405** (`HttpRequestMethodNotSupportedException`), **400** (`MissingServletRequestParameterException`, `ConstraintViolationException`) | `GlobalExceptionHandler`; testes correspondentes |
| — | +6 testes (Cliente 7, Conta 10, Transferência 5, novo `GlobalExceptionHandlerTest` 2) | suíte 43 |

### Etapa C — Correção monetária/temporal (concluída)
| Item | Ação | Evidência |
|---|---|---|
| M-04 | CPF com **dígitos verificadores** (mod 11) + rejeição de 11 dígitos iguais | `ClienteService.validarCpf`; testes `ClienteServiceTest` (+2) |
| M-05 | Limite de **magnitude** no serviço (≤ `99999999999999999.99`) | `ContaService.VALOR_MAXIMO`; teste `ContaServiceTest` (+1) |
| A-07 | Timestamps gerados em **UTC** | `Transacao.prePersist` (`LocalDateTime.now(ZoneOffset.UTC)`); `hibernate.jdbc.time_zone=UTC`; novo `TransacaoTest` (2) |
| — | CPF de exemplo `12345678901` (inválido) → `12345678909` (válido) em docs/OpenAPI/testes | `README.md`, `ClienteController`, DTOs, testes |

### Etapa D — Prova de segurança transacional (concluída)
| Item | Ação | Evidência |
|---|---|---|
| A-02 | Prova de concorrência: 4 threads × 25 transferências em direções opostas entre as mesmas 2 contas; saldo total constante (2000.00), nenhum saldo negativo, sem deadlock, 100 transações registradas | `ContaServiceIntegrationTest.devePreservarSaldoTotalSobTransferenciasConcorrentes` (H2 real, `@SpringBootTest`) |
| A-03 (rollback) | Atomicidade: falha simulada no `TransacaoRepository.save` **não** deixa débito/crédito aplicados; rollback integral | `ContaServiceIntegrationTest.deveManterSaldosQuandoORegistroDaTransacaoFalhar` (`@MockitoSpyBean`) |
| Etapa 5 | Consistência: `saldo == Σ transações` (depósito − saque ∓ transferência) conferido via `consultarExtrato` | `ContaServiceIntegrationTest.saldoDeveSerConsistenteComOHistoricoDeTransacoes` |
| — | +3 testes; README atualizado (contagem 51, Features, Roadmap sem itens já concluídos) | suíte 51 |

### Etapa E — Qualidade de dados e banco (concluída parcialmente)
| Item | Ação | Evidência |
|---|---|---|
| M-10 | Testes de integração contra **MySQL real** via **Testcontainers** (`mysql:8.4.7`), executados por padrão e **auto-ignorados sem Docker** | `RepositorioMySqlIntegrationTest` (`@DataJpaTest` + `@ServiceConnection`, `@Testcontainers(disabledWithoutDocker = true)`); deps `testcontainers-mysql`, `testcontainers-junit-jupiter`, `spring-boot-starter-data-jpa-test`, `spring-boot-testcontainers` |
| M-11 | Validação das **queries derivadas/JPQL** contra o engine real (não apenas mocks): ordenação do extrato por `data_hora DESC, id DESC` considerando origem **e** destino; `findByIdWithLock` (`@Lock(PESSIMISTIC_WRITE)`) executa em MySQL | `RepositorioMySqlIntegrationTest.deveOrdenarExtratoConsiderandoOrigemEDestino`, `deveRecuperarContaComBloqueioPessimista` |
| — | Validação das **constraints** no banco (unique CPF, unique número de conta → `DataIntegrityViolationException`) | `RepositorioMySqlIntegrationTest.deveImpedirCpfDuplicado`, `deveImpedirNumeroDeContaDuplicado` |
| M-08 | Índices redundantes (`idx_transacao_origem`, `idx_transacao_destino` — prefixos de `idx_transacao_*_data_hora`) **mantidos e apenas documentados** | decidido: **schema congelado**; sem alteração em `schema.sql` |
| M-02 | `@Version` (optimistic lock) **não implementado** — exigiria coluna `version` (mudança de schema) | decidido: **schema congelado**; permanece no Roadmap |
| — | +4 testes; README atualizado (contagem 55, tabela, stack Testcontainers/MySQL) | suíte 55 |

> **Nota M-08 (documentação):** os índices simples `idx_transacao_origem` e `idx_transacao_destino` são redundantes, pois já são o prefixo à esquerda de `idx_transacao_origem_data_hora (conta_origem_id, data_hora DESC)` e `idx_transacao_destino_data_hora (conta_destino_id, data_hora DESC)`. A remoção reduziria custo de escrita, mas foi **adiada** por decisão de congelamento do schema.

### Etapa F — Segurança, PII e exposição de docs (concluída)
| Item | Ação | Evidência |
|---|---|---|
| C-01 | **Spring Security** (HTTP Basic, `SessionCreationPolicy.STATELESS`, CSRF off): escrita (`POST /clientes`, `/contas`, `/transferencias`, `/contas/*/depositos`, `/contas/*/saques`) exige `ROLE_ADMIN`; leitura exige autenticação; `permitAll` em `/v3/api-docs/**`, `/swagger-ui/**`, `/swagger-ui.html`, `/error`; `BCryptPasswordEncoder` + `InMemoryUserDetailsManager` (usuários `admin`/`cliente` via `app.security.*`) | `config/SecurityConfig.java`; `application.properties`; `SecurityIntegrationTest` (401/403/200, docs público) |
| C-01 (testes de camada) | Slices de controller ajustados para não depender de auth (`@AutoConfigureMockMvc(addFilters = false)`) mantendo o foco original | `ClienteControllerTest`, `ContaControllerTest`, `TransferenciaControllerTest` |
| A-04 | **Mascaramento de PII** em `ClienteResponse`: CPF → `***.***.789-09`, e-mail → `an***@example.com` (helpers null-safe); exemplos OpenAPI atualizados | `dto/cliente/ClienteResponse.java`; `ClienteResponseTest` (3); asserções em `ClienteServiceTest`/`ClienteControllerTest` |
| A-05 | **Swagger/OpenAPI desligado no perfil `prod`** | `application-prod.properties`; `SwaggerProfileIntegrationTest` (2) |
| A-01 | **Idempotência de transferência** não implementada — exigiria chave persistida (mudança de schema); documentada como limitação | decidido: **schema congelado**; permanece no Roadmap (README) |
| — | `.env.example` com credenciais demo (`APP_ADMIN_*`, `APP_CLIENT_*`) | `.env.example` |
| — | +10 testes; README atualizado (contagem 65, seção Authentication, exemplos JSON mascarados, Env Vars) | suíte 65 |

> **Nota C-01 (escopo):** a autenticação é **nível demonstração** — usuários in-memory com senhas padrão de desenvolvimento, sem store de usuários, JWT nem ownership por cliente. É adequada para portfólio, **não** para produção. As credenciais padrão devem ser sobrescritas via `APP_*` em qualquer ambiente compartilhado.

### Pendentes (próximas etapas)
- Nenhuma etapa pendente do plano A–F. Itens que exigem **mudança de schema** (idempotência A-01, `@Version` M-02, remoção de índices M-08) permanecem **documentados no Roadmap** por decisão de congelamento do schema.

> **Decisões do desenvolvedor já registradas:** C-01/C-02 → documentar agora, segurança depois; A-02 → suavizar + testes de concorrência (Etapa D); A-01/M-02/M-08 → **congelar o schema** e apenas documentar; Etapas A, B, C, D, E e F concluídas.
