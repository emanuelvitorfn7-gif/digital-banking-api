# Finalização Full Stack — Relatório Técnico

Projeto: **Digital Banking — Full Stack Application**
Back-End: Java 25 + Spring Boot 4.1.1 + Spring Security + JPA + MySQL 8.4.7
Front-End: React 19 + TypeScript + Vite 8 + Tailwind CSS 4 + React Router 7
Data da revisão: 2026-10-11

> Parecer final ao fim do documento.

---

## 1. Escopo da etapa

Revisão final de código, segurança, testes, interface, README e preparação para
publicação no GitHub. Nenhuma regra bancária, schema ou dado do MySQL foi
alterado. Nenhum commit/push foi executado.

---

## 2. Arquivos criados ou modificados

### Modificados (raiz)
| Arquivo | Alteração |
|---------|-----------|
| `README.md` | Reescrito como **Full Stack Application**: fluxo React → Vite Proxy → Spring Boot → MySQL, testes do Front-End, resultados verificados e seção **Screenshots** (placeholders, sem imagens inventadas). |
| `.gitignore` | Adicionado bloco **Node/Front-End** (`frontend/node_modules`, `dist`, `coverage`, `.vite`, `*.local`). |

### Criados — `frontend/` (58 arquivos versionáveis)
- **Config/raiz:** `package.json`, `package-lock.json`, `index.html`, `tsconfig.json`,
  `vite.config.ts` (proxy `/api` + Vitest), `.env.example`, `.env.test`, `.gitignore`.
- **`src/api/`:** `client.ts`, `endpoints.ts`, `types.ts`, `credentials.ts` (+ `client.test.ts`).
- **`src/auth/`:** `AuthContext.tsx`.
- **`src/lib/`:** `formatters.ts`, `masks.ts`, `validators.ts` (+ testes).
- **`src/hooks/`:** `useRequest.ts`.
- **`src/components/ui/`:** `Spinner`, `Button`, `Card`, `Alert`, `TextField`,
  `EmptyState`, `ConfirmModal`, `ApiErrorAlert`, `CurrencyInput` (+ teste), `LinkButton`.
- **`src/components/layout/`:** `DisclaimerBanner`, `RoleBadge`, `RequireAuth`,
  `RequireRole`, `AppShell`.
- **`src/components/data/`:** `ContaCard`, `TransacaoResult`; `src/components/ContaIdPicker.tsx`.
- **`src/features/`:** `auth/LoginPage` (+ teste), `dashboard/DashboardPage`,
  `clientes/{Novo,Detalhe}ClientePage`, `contas/{Abrir,Deposito(+teste),Saque}Page`,
  `transferencias/TransferenciaPage`, `extrato/ExtratoPage`, `sobre/SobrePage`.
- **`src/mocks/`:** `handlers.ts`, `server.ts`; **`src/test/`:** `setup.ts`.
- **`src/` raiz:** `main.tsx`, `App.tsx`, `index.css`, `vite-env.d.ts`.

### Criados — raiz
| Arquivo | Descrição |
|---------|-----------|
| `FINALIZACAO_FULLSTACK.md` | Este relatório. |

---

## 3. Revisão de código (ETAPA 1)

- `tsconfig.json` usa `strict`, `noUnusedLocals` e `noUnusedParameters` — o
  `tsc --noEmit` (TypeScript 7.0.2) passa com **0 erros**, garantindo que não há
  imports/variáveis não utilizados.
- **Correção aplicada:** o tipo `ValorRequest` estava definido e não utilizado.
  Passou a ser efetivamente usado em `src/api/endpoints.ts` (`satisfies ValorRequest`
  nos corpos de depósito e saque) — elimina código morto sem mudar comportamento.
- **Limpeza:** arquivos temporários `frontend/vite-dev.log` e
  `frontend/vite-dev.err.log` (gerados durante a validação de integração). Estavam
  segurados pelo processo do Vite em execução; permanecem no disco mas são
  **ignorados pelo Git** (`*.log`). Removíveis após encerrar o Vite.
- Sem `console.*`, `TODO`, `FIXME` ou `debugger` no código.
- Dependências conferidas: nenhuma redundante (`react`, `react-dom`,
  `react-router-dom`; dev tools usadas de fato pelos testes/build).
- Nenhum arquivo necessário foi removido.

---

## 4. Revisão de segurança (ETAPA 2)

| Verificação | Resultado |
|-------------|-----------|
| `git ls-files` com `env/secret/password` | Apenas `.env.example` (sem segredos). |
| `frontend/.env` versionado? | Não (ignorado). `.env.test`/`.env.example` sem segredos. |
| `node_modules`, `dist`, logs ignorados | Sim (`git check-ignore` confirma). |
| `localStorage` / `sessionStorage` | **Nenhum uso** no código — apenas citados em textos/comentários. |
| Credenciais | HTTP Basic **demonstrativo**; usuário digitado; senha nunca embutida no bundle; sessão só em memória (`src/api/credentials.ts`). |
| `401` | Validado ao vivo (direto e via proxy). |
| `403` | **Validado ao vivo** (ver §4.1). |
| `404` | Validado no Back-End e pelo proxy. |
| `409` | Mapeado no cliente e coberto por teste unitário (`api/client.test.ts`); **não** executado ao vivo (evita escrita). |
| `500` | Mensagem padrão no cliente; não provocado. |
| CLIENTE em operação administrativa | **Bloqueado (HTTP 403)** — confirmado. |
| Exposição à internet | Servidor local (`localhost:8081`); **não** publicar a API. |

### 4.1 Verificação ao vivo do HTTP 403
`POST /clientes` com credenciais `CLIENTE`:
- Direto no Back-End: **403**
- Via proxy Vite: **403**
- Corpo: `{"timestamp":...,"status":403,"error":"Forbidden","path":"/clientes"}`

Foi enviado corpo inválido (`{}`) de propósito: mesmo que a autorização falhasse,
a validação do payload retornaria 400 **sem escrita** no banco.

---

## 5. Testes (ETAPA 3) — resultados efetivamente observados

| Suíte | Comando | Resultado |
|-------|---------|-----------|
| Back-End | `.\mvnw.cmd clean verify` | **BUILD SUCCESS — 65 testes, 0 falhas, 0 erros, 0 ignorados** |
| Front-End | `npm test` | **7 arquivos, 25 testes, todos passando** |
| Tipo (TS) | `npm run typecheck` | **0 erros** |
| Build de produção | `npm run build` | **sucesso** (CSS 22.23 kB / JS 302.10 kB; gzip 5.18 / 93.10 kB) |

- Os **4 testes Testcontainers** (`RepositorioMySqlIntegrationTest`) **executaram**
  (Docker disponível) contra um container MySQL isolado — **não** tocaram o banco de
  desenvolvimento.
- Testes ignorados **não** foram contados como aprovados (0 ignorados).

### 5.1 Validação de integração end-to-end (banco de desenvolvimento)
Somente operações de **leitura**: conta, saldo, extrato, cliente mascarado, 401,
403 e 404. Confirmado que o banco permaneceu intacto antes/depois.

---

## 6. Interface (ETAPA 4)

- **Dados reais:** o dashboard usa `contasApi.buscarPorId`; saldo, número e titular
  vêm da API. Nenhum saldo/cliente/transação é inventado.
- **Responsividade:** `max-w-*`, breakpoints `sm:`/`md:`, menu mobile com
  `aria-expanded`, tabela do extrato com `overflow-x-auto`.
- **Acessibilidade:** `label`+`htmlFor` (via `useId`), `aria-invalid`/`aria-describedby`
  nos campos, `role="alert"`/`role="status"` nos alertas, diálogo com `role="dialog"`
  e `aria-modal`, ícones `aria-hidden`, foco/ESC no modal.
- **Formulários:** validação local antes do envio (CPF com dígitos verificadores,
  e-mail, ids, valores) + mensagens de erro da API (`ApiErrorAlert`).
- **Carregamento:** `Spinner` + botões em estado `loading`.
- **Navegação:** React Router com `RequireAuth`/`RequireRole` e menu responsivo.
- **Aviso:** banner e rodapé reforçam "não é um banco real".

---

## 6b. Interface — endereços
- Front-End: `http://localhost:5173`
- Back-End: `http://localhost:8081` (também ativo em `:8080`)
- Swagger UI: `http://localhost:8081/swagger-ui/index.html`

---

## 7. Pendências e limitações

1. **Screenshots ausentes** — a seção do README usa placeholders. É necessário
   capturar as telas reais e salvá-las em `docs/screenshots/` (`login.png`,
   `dashboard.png`, `extrato.png`, `transferencia.png`).
2. **Fluxos de escrita (depósito/saque/transferência) e HTTP 409 não validados ao
   vivo pelo Front-End** — exigiriam um banco isolado. Cobertos por testes
   automatizados (Front-End com MSW; Back-End em H2/Testcontainers). Para validar
   ao vivo sem afetar o banco atual, usar um banco/instância separados.
3. **Autenticação demonstrativa** — HTTP Basic com usuários em memória; sem TLS,
   sem sessão/JWT, sem rate limiting, sem ownership por cliente. Não publicar a API.
3. **Listeners antigos** em `:8080` e `:8081` — não foram reiniciados nem
   interrompidos durante a revisão.
4. **Logs temporários do Vite** seguem no disco (ignorados pelo Git).

---

## 8. Como reproduzir

### Back-End
```powershell
# Pré-requisitos: JDK 25, MySQL 8.4.x (base digital_banking + schema aplicado)
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "sua_senha"
.\mvnw.cmd clean verify          # 65 testes
.\mvnw.cmd spring-boot:run       # sobe em :8080 (ou -Dserver.port=8081)
```

### Front-End
```bash
cd frontend
npm install
npm run dev                      # http://localhost:5173 (proxy /api → :8081)
npm test                         # 25 testes
npm run build                    # build de produção em dist/
```

Login demonstrativo: `admin/admin123` (escrita) ou `cliente/cliente123` (leitura).

---

## 8b. Publicação no GitHub (a autorizar)
- `git status`: modificados `README.md`, `.gitignore`; novo `frontend/`.
- **Incluir:** `frontend/src`, `frontend/package.json`, `frontend/package-lock.json`,
  `frontend/index.html`, `frontend/tsconfig.json`, `frontend/vite.config.ts`,
  `frontend/.env.example`, `frontend/.env.test`, `frontend/.gitignore`.
- **Ignorados:** `frontend/node_modules`, `frontend/dist`, `frontend/*.log`, `.env`.
- Nenhum commit/push realizado até este ponto.

---

## 9. Revisão de segurança — resumo
Sem segredos no versionamento; credenciais apenas em memória; autorização real no
Back-End (403 confirmado para `CLIENTE`); PII mascarada pela API; HTTP Basic
demonstrativo e sem TLS; API restrita a `localhost`. **Não publicar a API na
internet.**

---

## 10. Parecer final

**PRONTO COM RESSALVAS.**

O código está limpo (0 erros de TypeScript, sem código morto, sem segredos no Git),
as 90 suítes de teste passam (65 Back-End + 25 Front-End), o build de produção
funciona e a integração de leitura, autenticação e autorização (401/403/404) foi
validada ponta a ponta contra os servidores.

Ressalvas não bloqueantes para um projeto de portfólio:
1. incluir as capturas de tela reais;
2. registrar que os fluxos de escrita e o HTTP 409 foram validados por testes
   automatizados/ambiente isolado, e não ao vivo no banco de desenvolvimento;
3. manter o aviso de segurança (autenticação demonstrativa, não é um banco real,
   não expor a API à internet).

Aguardando sua autorização para publicar a nova versão no GitHub.
