# Digital Banking — Full Stack Application

A **full-stack** application that simulates core digital banking operations —
customer onboarding, account management, deposits, withdrawals, transfers, and
account statements.

- **Back-End** — a RESTful API built with **Java 25** and **Spring Boot 4.1.1**,
  secured with HTTP Basic, persisting to **MySQL** through Spring Data JPA.
- **Front-End** — a single-page app in [`frontend/`](frontend) built with
  **React 19 + TypeScript + Vite 8 + Tailwind CSS 4**, consuming the API through a
  Vite dev proxy (no CORS configuration required).

This project was built as a **portfolio project** to demonstrate clean layered
architecture, transactional consistency for money movements, robust input
validation, consistent error handling, automated testing, and a clean React front
end wired to real backend endpoints.

> ⚠️ This is a study/portfolio project. It is **not** intended for production use.
> See the [Security Limitations](#-security-limitations) section.

---

## Table of Contents

- [Features](#-features)
- [Technologies](#-technologies)
- [Architecture](#-architecture)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
- [Front-End](#-front-end-react--typescript--vite)
- [MySQL Configuration](#-mysql-configuration)
- [Environment Variables](#-environment-variables)
- [Maven Wrapper Commands](#-maven-wrapper-commands)
- [REST API Endpoints](#-rest-api-endpoints)
- [JSON Examples](#-json-examples)
- [API Documentation (Swagger UI)](#-api-documentation-swagger-ui)
- [Automated Tests](#-automated-tests)
- [Screenshots](#-screenshots)
- [Security Limitations](#-security-limitations)
- [Roadmap](#-roadmap)
- [License](#-license)

---

## ✨ Features

- **Customer management** — create and retrieve customers with unique CPF and email validation.
- **Account management** — open accounts for existing customers with an automatically generated account number and zero initial balance.
- **Deposits** — credit an account, recording a `DEPOSITO` transaction.
- **Withdrawals** — debit an account, blocking operations that would exceed the available balance.
- **Transfers** — move money between two accounts atomically (debit + credit in a single transaction).
- **Authentication & authorization** — HTTP Basic security with roles (`CLIENTE` read-only, `ADMIN` write). Swagger/OpenAPI remains public.
- **PII masking** — CPF and e-mail are masked in public responses (`***.***.789-09`, `an***@domain`).
- **Concurrency safety** — during transfers, both accounts are locked with a **pessimistic write lock** in ascending `id` order. Integration tests run parallel transfers in both directions and assert that the combined balance is conserved and neither account goes negative, with no deadlock.
- **Account statement** — paginated transaction history for a given account (as origin or destination), newest first.
- **Input validation** — Jakarta Bean Validation on every request payload.
- **CPF check-digit validation** — full CPF validation with the mod-11 check digits, beyond length/format.
- **UTC timestamps** — transaction timestamps are generated and stored in UTC for consistency.
- **Consistent error responses** — a global exception handler returns a uniform error body with the correct HTTP status.
- **Interactive documentation** — OpenAPI 3 specification with Swagger UI.
- **Web UI (bonus)** — a React + TypeScript single-page app in [`frontend/`](frontend) that exercises every endpoint with a fintech-style interface.

---

## 🛠 Technologies

| Category            | Technology                                              |
|---------------------|---------------------------------------------------------|
| Language            | Java 25 (LTS)                                           |
| Framework           | Spring Boot 4.1.1                                       |
| Web layer           | Spring Web MVC (`spring-boot-starter-webmvc`)           |
| Persistence         | Spring Data JPA / Hibernate ORM                         |
| Security            | Spring Security (HTTP Basic, in-memory roles)           |
| Validation          | Jakarta Bean Validation (`spring-boot-starter-validation`) |
| Database            | MySQL 8.4.7                                             |
| API Documentation   | springdoc-openapi 3.1.1 (OpenAPI 3 + Swagger UI)        |
| Build tool          | Maven (via Maven Wrapper)                               |
| Testing             | JUnit 5, Mockito, Spring Test, MockMvc, Testcontainers  |
| Test databases      | H2 (in-memory) and MySQL 8.4.7 (Testcontainers), test scope only |
| Front-End (bonus)   | React 19, TypeScript, Vite 8, Tailwind CSS 4, React Router 7   |
| Front-End tests     | Vitest, React Testing Library, MSW                              |

---

## 🏛 Architecture

### Full-stack request flow

```
Browser (React SPA, :5173)
        │  fetch("/api/…")                  ← same-origin, no CORS
        ▼
Vite dev server (:5173)
        │  proxy /api → http://localhost:8081
        ▼
Spring Boot API (:8081)
        │  HTTP Basic auth → Controller → Service → Repository (JPA)
        ▼
MySQL 8.4.7 (digital_banking)
```

In development the React app calls the relative path `/api/*`; the **Vite dev
proxy** forwards it to the Spring Boot server, so both run on separate ports
without any CORS configuration. For a production build you would serve
`frontend/dist` from a static host and point `VITE_API_BASE_URL` at the deployed
API (see [Front-End](#-front-end-react--typescript--vite)).

### Backend layers

The backend follows the classic **Controller → Service → Repository** layered
architecture, which keeps HTTP concerns, business rules, and data access clearly
separated.

```
                ┌─────────────────────────────────────────────┐
   HTTP         │                 Controller                   │
  request  ───► │  - Maps and validates HTTP input             │
                │  - Delegates to the service layer            │
                │  - Returns ResponseEntity + status codes     │
                └───────────────────────┬─────────────────────┘
                                        │
                                        ▼
                ┌─────────────────────────────────────────────┐
                │                  Service                     │
                │  - Business rules and orchestration          │
                │  - Transaction boundaries (@Transactional)   │
                │  - Pessimistic locking for transfers         │
                │  - Throws domain/business exceptions         │
                └───────────────────────┬─────────────────────┘
                                        │
                                        ▼
                ┌─────────────────────────────────────────────┐
                │                 Repository                    │
                │  - Spring Data JPA interfaces                 │
                │  - Derived queries, pagination, row locks     │
                └───────────────────────┬─────────────────────┘
                                        │
                                        ▼
                ┌─────────────────────────────────────────────┐
                │                   Database                    │
                │  - MySQL 8.4.7 (clientes, contas, transacoes) │
                └─────────────────────────────────────────────┘
```

### Layer responsibilities

| Layer          | Package                    | Responsibility                                                                 |
|----------------|----------------------------|--------------------------------------------------------------------------------|
| **Controller** | `controller`               | HTTP mapping, request validation, response status codes, `Location` headers.   |
| **Service**    | `service`                  | Business rules, value validation, balance checks, transactional consistency.   |
| **Repository** | `repository`               | Data access via Spring Data JPA derived queries and locking.                    |
| **Entity**     | `entity`                   | JPA-mapped persistence model.                                                   |
| **DTO**        | `dto`                      | Immutable request/response records that decouple the API from the persistence model. |
| **Exception**  | `exception`                | Domain-specific exceptions mapped to HTTP statuses.                            |
| **Handler**    | `handler`                  | `@RestControllerAdvice` translating exceptions into a uniform error response.  |
| **Config**     | `config`                   | OpenAPI metadata, application configuration and the Spring Security filter chain. |

---

## 📁 Project Structure

```
digital-banking-api/
├── .mvn/
│   └── wrapper/
│       └── maven-wrapper.properties       # Pinned Maven distribution (3.9.16)
├── src
│   ├── main
│   │   ├── java/com/digitalbanking/api
│   │   │   ├── DigitalBankingApiApplication.java   # Spring Boot entry point
│   │   │   ├── config
│   │   │   │   ├── OpenApiConfig.java              # OpenAPI/Swagger metadata
│   │   │   │   └── SecurityConfig.java             # HTTP Basic security + roles
│   │   │   ├── controller                          # REST controllers
│   │   │   │   ├── ClienteController.java
│   │   │   │   ├── ContaController.java
│   │   │   │   └── TransferenciaController.java
│   │   │   ├── dto                                 # Request/response records
│   │   │   │   ├── cliente
│   │   │   │   ├── comum
│   │   │   │   ├── conta
│   │   │   │   └── operacao
│   │   │   ├── entity                              # JPA entities
│   │   │   │   ├── Cliente.java
│   │   │   │   ├── Conta.java
│   │   │   │   ├── Transacao.java
│   │   │   │   └── enums/TipoTransacao.java
│   │   │   ├── exception                           # Domain exceptions
│   │   │   ├── handler
│   │   │   │   └── GlobalExceptionHandler.java     # Uniform error handling
│   │   │   ├── repository                          # Spring Data JPA repositories
│   │   │   │   ├── ClienteRepository.java
│   │   │   │   ├── ContaRepository.java
│   │   │   │   └── TransacaoRepository.java
│   │   │   ├── service                             # Business logic
│   │   │   │   ├── ClienteService.java
│   │   │   │   └── ContaService.java
│   │   │   └── util                                # Cross-cutting helpers
│   │   │       └── PiiMasker.java                  # CPF/e-mail masking
│   │   └── resources
│   │       ├── application.properties              # App configuration (default profile)
│   │       ├── application-prod.properties         # prod: Swagger/OpenAPI disabled
│   │       └── db/schema.sql                       # Manual DDL script (optional)
│   └── test
│       └── java/com/digitalbanking/api
│           ├── OpenApiDocumentationTest.java
│           ├── SecurityIntegrationTest.java        # 401/403/200, Swagger public
│           ├── SwaggerProfileIntegrationTest.java  # A-05: docs off in prod profile
│           ├── controller                          # MockMvc slice tests
│           │   ├── ClienteControllerTest.java
│           │   ├── ContaControllerTest.java
│           │   └── TransferenciaControllerTest.java
│           ├── dto/cliente
│           │   └── ClienteResponseTest.java        # PII masking
│           ├── repository
│           │   └── RepositorioMySqlIntegrationTest.java  # MySQL integration (Testcontainers)
│           └── service                             # Unit + integration tests
│               ├── ClienteServiceTest.java
│               ├── ContaServiceTest.java
│               └── ContaServiceIntegrationTest.java  # H2 integration (concurrency, atomicity)
├── frontend/                               # React + TypeScript + Vite single-page app
│   ├── src
│   │   ├── api/                            # fetch client, DTO types, endpoint wrappers
│   │   ├── auth/                           # in-memory auth context (basic credentials)
│   │   ├── components/                     # layout, UI primitives, data components
│   │   ├── features/                       # login, dashboard, clientes, contas, transferencias, extrato
│   │   ├── hooks/                          # useRequest (loading/error state)
│   │   ├── lib/                            # formatters, masks, validators
│   │   ├── mocks/                          # MSW handlers/server (tests only)
│   │   └── test/                           # Vitest setup
│   ├── .env.example                        # Front-End env sample
│   ├── index.html
│   ├── package.json
│   ├── tsconfig.json
│   └── vite.config.ts                      # dev proxy (/api → Back-End) + Vitest config
├── .env.example                            # Sample environment variables
├── .gitignore
├── LICENSE                                 # MIT license
├── mvnw                                    # Maven Wrapper (Unix)
├── mvnw.cmd                                # Maven Wrapper (Windows)
├── pom.xml
└── README.md
```

---

## 🚀 Getting Started

### Prerequisites

- **JDK 25** (e.g. Eclipse Temurin 25)
- **MySQL 8.4.x** running locally (or reachable via JDBC)
- No local Maven installation required — the **Maven Wrapper** is included.

Verify Java:

```bash
java -version
```

### 1. Clone the repository

```bash
git clone https://github.com/emanuelvitorfn7-gif/digital-banking-api.git
cd digital-banking-api
```

### 2. Create the database and tables

Create a MySQL database named `digital_banking` and apply the schema using the
provided script (`src/main/resources/db/schema.sql`). See
[MySQL Configuration](#-mysql-configuration).

### 3. Configure environment variables

```bash
# Linux / macOS
export DB_USERNAME=root
export DB_PASSWORD=your_password
```

```powershell
# Windows PowerShell
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "your_password"
```

### 4. Run the application

```bash
# Linux / macOS
./mvnw spring-boot:run
```

```powershell
# Windows PowerShell
.\mvnw.cmd spring-boot:run
```

The API starts on **http://localhost:8080**.

---

## 🖥 Front-End (React + TypeScript + Vite)

A companion single-page application lives in [`frontend/`](frontend). It is a **UI
layer only** — every action calls the real REST API above; it holds no business
logic and no database access of its own.

### Stack

| Category    | Technology                                    |
|-------------|-----------------------------------------------|
| UI library  | React 19                                      |
| Language    | TypeScript                                    |
| Build tool  | Vite 8                                        |
| Styling     | Tailwind CSS 4                                 |
| Routing     | React Router 7                                |
| HTTP client | Native `fetch` (no Axios)                      |
| Tests       | Vitest, React Testing Library, MSW             |

### Prerequisites

- **Node.js 20+** and **npm** (built and tested with Node 24 / npm 11).
- The **Back-End running** (see [Getting Started](#-getting-started)).

### Install and run

```bash
cd frontend
npm install
npm run dev
```

The app starts on **http://localhost:5173**. In development, Vite proxies `/api/*`
to the Back-End, so **no CORS configuration is required on the Back-End**.

> The dev proxy target defaults to `http://localhost:8081`. If your Back-End runs
> on the default port `8080` (see [Getting Started](#-getting-started)), set
> `VITE_API_TARGET=http://localhost:8080` in `frontend/.env`.

Create your own env file if needed:

```bash
cp .env.example .env
```

| Variable            | Default                 | Description                                              |
|---------------------|-------------------------|----------------------------------------------------------|
| `VITE_API_BASE_URL` | `/api`                  | URL prefix the browser uses (routed through the Vite proxy). |
| `VITE_API_TARGET`   | `http://localhost:8081` | Back-End URL the dev server proxies `/api` to.            |

### Scripts

| Task             | Command           |
|------------------|-------------------|
| Dev server       | `npm run dev`     |
| Production build | `npm run build`   |
| Preview build    | `npm run preview` |
| Tests            | `npm test`        |
| Tests (watch)    | `npm run test:watch` |
| Type check       | `npm run typecheck`  |

### Running both together

1. Start the Back-End: `.\mvnw.cmd spring-boot:run` (Windows) / `./mvnw spring-boot:run` (Unix).
2. In another terminal, start the Front-End: `cd frontend && npm run dev`.
3. Open **http://localhost:5173** and log in with `admin/admin123` (write) or `cliente/cliente123` (read-only).

### Screens

- **Login** — authenticates with the Back-End HTTP Basic credentials.
- **Dashboard** — looks up an account by id and shows its balance and holder.
- **Clientes** — create a customer, retrieve one by id.
- **Contas** — open an account for an existing customer.
- **Depósito** — credit an account.
- **Saque** — debit an account (overdraft is rejected by the API).
- **Transferência** — move money between two accounts.
- **Extrato** — paginated transaction statement.
- **Sobre** — project and security notes.

> **Security limitations of the UI.** Authentication is demo-grade: credentials are
> validated with a read-only probe against the API (there is no login endpoint) and
> are kept **only in memory** — never in `localStorage`/`sessionStorage` — so they
> are lost on refresh and no password is ever embedded in the bundle. Because the
> API exposes no account/customer listing, some flows require typing an id
> manually. The UI is for demonstration only; do **not** enter real personal data.

---

## 🐬 MySQL Configuration

The application connects to a database named **`digital_banking`** and does **not**
manage the schema automatically (`spring.jpa.hibernate.ddl-auto=none`). Tables must
exist beforehand.

Connection settings live in `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/digital_banking?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=none
spring.sql.init.mode=never
spring.jpa.open-in-view=false
```

### Create the schema manually

The application does **not** create the `digital_banking` database. The
`schema.sql` script begins with `USE digital_banking;`, so the database **must
already exist** before you run it. Create it first:

```sql
CREATE DATABASE IF NOT EXISTS digital_banking
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;
```

Then apply the tables:

1. Open **MySQL Workbench** and connect to your server.
2. Run the `CREATE DATABASE` statement above (or otherwise ensure a
   `digital_banking` database exists).
3. Open the SQL script `src/main/resources/db/schema.sql`.
4. Execute it against your server.

The script creates three tables:

| Table        | Purpose                                              |
|--------------|------------------------------------------------------|
| `clientes`   | Customers (unique `cpf` and `email`).                |
| `contas`     | Bank accounts (unique `numero`, `saldo DECIMAL(19,2)`, FK to `clientes`). |
| `transacoes` | Money movements (`tipo`, `valor DECIMAL(19,2)`, FKs to `contas`). |

> **`IF NOT EXISTS` is not a migration tool.** The script is safe to re-run
> because `CREATE TABLE IF NOT EXISTS` simply skips a table that already exists.
> It does **not** migrate data, alter columns, add or drop constraints, or repair
> an outdated schema: an existing table with a different structure is left
> untouched. Structural changes must be applied with explicit `ALTER TABLE`
> statements or a migration tool (e.g. Flyway/Liquibase).

> The `schema.sql` script is **never executed automatically** by the application
> (`spring.sql.init.mode=never`). It is a reference script for the DBA/developer.

---

## 🔐 Environment Variables

| Variable               | Required | Default     | Description                                                        |
|------------------------|----------|-------------|--------------------------------------------------------------------|
| `DB_USERNAME`          | No       | `root`      | MySQL username.                                                    |
| `DB_PASSWORD`          | **Yes**  | —           | MySQL password. The app **fails fast** at startup if it is missing. |
| `APP_ADMIN_USERNAME`   | No       | `admin`     | Username of the demo `ADMIN` user (write access).                  |
| `APP_ADMIN_PASSWORD`   | No       | `admin123`  | Password of the demo `ADMIN` user. **Override outside local dev.** |
| `APP_CLIENT_USERNAME`  | No       | `cliente`   | Username of the demo `CLIENTE` user (read-only).                   |
| `APP_CLIENT_PASSWORD`  | No       | `cliente123`| Password of the demo `CLIENTE` user. **Override outside local dev.** |

Database credentials are **never** hard-coded in the repository — only the
`${DB_PASSWORD}` and `${DB_USERNAME:root}` placeholders appear in
`application.properties`. The demo security users have local-development defaults
for convenience; they **must** be overridden in any shared or production
environment.

Copy `.env.example` to `.env` and fill in your own values, or export the variables
in your shell.

> **Why is `application-prod.properties` committed?** That file **is versioned on
> purpose**, unlike other local configuration files. It contains **no secrets or
> credentials** — only `springdoc.api-docs.enabled=false` and
> `springdoc.swagger-ui.enabled=false` — so it is safe to share and keeps the `prod`
> profile reproducible. Files that typically hold machine-specific values or
> credentials (`.env`, `application-local.*`, `application-dev.*`) are excluded by
> `.gitignore`.

---

## 📦 Maven Wrapper Commands

The project ships with the Maven Wrapper, so a local Maven install is not required.

| Task                     | Linux / macOS                        | Windows (PowerShell)                  |
|--------------------------|--------------------------------------|---------------------------------------|
| Compile                  | `./mvnw compile`                     | `.\mvnw.cmd compile`                  |
| Run tests                | `./mvnw test`                        | `.\mvnw.cmd test`                     |
| Package (JAR)            | `./mvnw package`                     | `.\mvnw.cmd package`                  |
| Package without tests    | `./mvnw package -DskipTests`         | `.\mvnw.cmd package -DskipTests`      |
| Run the app              | `./mvnw spring-boot:run`             | `.\mvnw.cmd spring-boot:run`          |
| Clean build              | `./mvnw clean package`               | `.\mvnw.cmd clean package`            |

The produced artifact is a Spring Boot executable JAR under `target/`.

---

## 🌐 REST API Endpoints

Base URL: `http://localhost:8080`

| Method | Path                          | Description                          | Role required | Success | Errors                       |
|--------|-------------------------------|--------------------------------------|---------------|---------|------------------------------|
| `POST` | `/clientes`                   | Create a customer                    | `ADMIN`       | `201`   | `400`, `401`, `403`, `409`   |
| `GET`  | `/clientes/{id}`              | Get a customer by id                 | authenticated | `200`   | `401`, `404`                 |
| `POST` | `/contas`                     | Open an account                      | `ADMIN`       | `201`   | `400`, `401`, `403`, `404`   |
| `GET`  | `/contas/{id}`                | Get an account by id                 | authenticated | `200`   | `401`, `404`                 |
| `POST` | `/contas/{id}/depositos`      | Deposit into an account              | `ADMIN`       | `200`   | `400`, `401`, `403`, `404`   |
| `POST` | `/contas/{id}/saques`         | Withdraw from an account             | `ADMIN`       | `200`   | `400`, `401`, `403`, `404`, `409` |
| `GET`  | `/contas/{id}/extrato`        | Paginated account statement          | authenticated | `200`   | `401`, `404`                 |
| `POST` | `/transferencias`             | Transfer between accounts            | `ADMIN`       | `200`   | `400`, `401`, `403`, `404`, `409` |
| `GET`  | `/v3/api-docs`, `/swagger-ui/**` | OpenAPI docs / Swagger UI         | public        | `200`   | —                            |

### Authentication

Write operations require the `ADMIN` role; read operations require any
authenticated user (`ADMIN` or `CLIENTE`). Swagger/OpenAPI endpoints are public.
The API uses **HTTP Basic**, configured with in-memory demo users:

| User      | Role      | Permissions                          |
|-----------|-----------|--------------------------------------|
| `admin`   | `ADMIN`   | Read + write (create, deposit, withdraw, transfer). |
| `cliente` | `CLIENTE` | Read-only (customers, accounts, statements). |

Example request:

```bash
curl -u admin:admin123 -X POST http://localhost:8080/clientes \
     -H "Content-Type: application/json" \
     -d '{"nome":"Ana","cpf":"12345678909","email":"ana@example.com"}'
```

> Credentials default to `admin/admin123` and `cliente/cliente123` for local
> development only; override them with the `APP_*` environment variables.

### HTTP status codes

| Status | Meaning                                                                       |
|--------|-------------------------------------------------------------------------------|
| `200`  | Operation completed successfully.                                             |
| `201`  | Resource created (response includes a `Location` header).                     |
| `400`  | Invalid input, malformed body, or a business rule violation (e.g. same-account transfer). |
| `401`  | Missing or invalid credentials.                                               |
| `403`  | Authenticated but lacking the required role (write operations need `ADMIN`).  |
| `404`  | Customer or account not found.                                                |
| `409`  | Conflict — duplicate CPF/email, insufficient balance.                         |
| `500`  | Unexpected server error (generic message, no internal details leaked).        |

### Statement pagination

`GET /contas/{id}/extrato` accepts two optional query parameters:

| Parameter | Default | Range     | Description                    |
|-----------|---------|-----------|--------------------------------|
| `page`    | `0`     | `>= 0`    | Page number (zero-based).      |
| `size`    | `10`    | `1..100`  | Number of items per page.      |

---

## 📨 JSON Examples

> All data below is **fictitious** and contains **no real personal information**.

### Create a customer — `POST /clientes`

**Request**

```json
{
  "nome": "Ana Silva",
  "cpf": "12345678909",
  "email": "ana.silva@example.com"
}
```

**Response `201 Created`** (`Location: /clientes/1`) — PII is masked

```json
{
  "id": 1,
  "nome": "Ana Silva",
  "cpf": "***.***.789-09",
  "email": "an***@example.com"
}
```

### Open an account — `POST /contas`

**Request**

```json
{
  "clienteId": 1
}
```

**Response `201 Created`** (`Location: /contas/1`)

```json
{
  "id": 1,
  "numero": "00000042",
  "saldo": 0.00,
  "clienteId": 1,
  "clienteNome": "Ana Silva"
}
```

### Deposit — `POST /contas/1/depositos`

**Request**

```json
{
  "valor": 50.00
}
```

**Response `200 OK`**

```json
{
  "id": 10,
  "tipo": "DEPOSITO",
  "valor": 50.00,
  "dataHora": "2026-10-11T17:51:00",
  "contaOrigemId": null,
  "contaDestinoId": 1
}
```

### Withdraw — `POST /contas/1/saques`

**Request**

```json
{
  "valor": 20.00
}
```

**Response `200 OK`**

```json
{
  "id": 11,
  "tipo": "SAQUE",
  "valor": 20.00,
  "dataHora": "2026-10-11T18:10:00",
  "contaOrigemId": 1,
  "contaDestinoId": null
}
```

### Transfer — `POST /transferencias`

**Request**

```json
{
  "contaOrigemId": 1,
  "contaDestinoId": 2,
  "valor": 30.00
}
```

**Response `200 OK`**

```json
{
  "id": 12,
  "tipo": "TRANSFERENCIA",
  "valor": 30.00,
  "dataHora": "2026-10-11T18:30:00",
  "contaOrigemId": 1,
  "contaDestinoId": 2
}
```

### Statement — `GET /contas/1/extrato?page=0&size=10`

**Response `200 OK`**

```json
{
  "content": [
    {
      "id": 11,
      "tipo": "SAQUE",
      "valor": 20.00,
      "dataHora": "2026-10-11T18:10:00",
      "contaOrigemId": 1,
      "contaDestinoId": null
    },
    {
      "id": 10,
      "tipo": "DEPOSITO",
      "valor": 50.00,
      "dataHora": "2026-10-11T17:51:00",
      "contaOrigemId": null,
      "contaDestinoId": 1
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 2,
  "totalPages": 1
}
```

### Error response example — `409 Conflict`

```json
{
  "timestamp": "2026-10-11T20:51:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "Saldo insuficiente para realizar o saque.",
  "details": []
}
```

### Validation error example — `400 Bad Request`

```json
{
  "timestamp": "2026-10-11T20:51:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Dados de entrada inválidos.",
  "details": [
    "email: O e-mail informado é inválido.",
    "nome: O nome é obrigatório."
  ]
}
```

---

## 📖 API Documentation (Swagger UI)

The API ships with an OpenAPI 3 specification generated by **springdoc-openapi**.
Once the application is running:

| Resource              | URL                                              |
|-----------------------|--------------------------------------------------|
| Swagger UI            | `http://localhost:8080/swagger-ui/index.html`    |
| OpenAPI JSON          | `http://localhost:8080/v3/api-docs`              |
| OpenAPI YAML          | `http://localhost:8080/v3/api-docs.yaml`         |

The documentation includes:

- **Title:** Digital Banking API
- **Version:** 1.0.0
- **Description:** in Portuguese
- Per-endpoint summaries, descriptions, HTTP status codes, and JSON examples.

> Endpoints can also be disabled per profile. The bundled `prod` profile already
> sets `springdoc.api-docs.enabled=false` and `springdoc.swagger-ui.enabled=false`
> (`application-prod.properties`); activate it with `--spring.profiles.active=prod`.

---

## 🧪 Automated Tests

The project ships **90 automated tests** — **65 Back-End (Java)** and **25
Front-End (Vitest + React Testing Library + MSW)** — all passing.

**Back-End:**

```bash
./mvnw clean verify      # Linux / macOS
.\mvnw.cmd clean verify  # Windows
```

**Front-End:**

```bash
cd frontend
npm test                 # run once (vitest run)
npm run test:watch       # watch mode
```

| Test class                     | Type                | Tests | What it covers                                             |
|--------------------------------|---------------------|-------|------------------------------------------------------------|
| `ClienteServiceTest`           | Unit (Mockito)      | 7     | Customer creation, CPF normalization/check digits, duplicate detection. |
| `ContaServiceTest`             | Unit (Mockito)      | 13    | Account creation, deposit, withdrawal, transfer, limits.   |
| `ContaServiceIntegrationTest`  | Integration (H2)    | 3     | Concurrency, rollback atomicity, balance/statement consistency. |
| `RepositorioMySqlIntegrationTest` | Integration (MySQL) | 4  | Derived queries, origin/destination ordering, `@Lock(PESSIMISTIC_WRITE)` and unique constraints on real MySQL. |
| `ClienteResponseTest`          | Unit (DTO)          | 3     | CPF/e-mail masking, including short/absent values.         |
| `SecurityIntegrationTest`      | Integration (H2)    | 5     | 401 without credentials, 403 with insufficient role, 201 with `ADMIN`, public Swagger, masked CPF. |
| `SwaggerProfileIntegrationTest`| Integration (H2)    | 2     | OpenAPI/Swagger disabled under the `prod` profile (A-05).  |
| `TransacaoTest`                | Unit (entity)       | 2     | UTC timestamp generation on persist.                       |
| `GlobalExceptionHandlerTest`   | Unit                | 2     | Missing parameter and constraint violation error mapping.  |
| `ClienteControllerTest`        | Web slice (MockMvc) | 7     | HTTP status codes, payloads and error mapping.             |
| `ContaControllerTest`          | Web slice (MockMvc) | 10    | Account, deposit, withdrawal, statement endpoints.         |
| `TransferenciaControllerTest`  | Web slice (MockMvc) | 5     | Transfer endpoint, error mapping.                          |
| `OpenApiDocumentationTest`     | Integration         | 2     | `/v3/api-docs` and Swagger UI availability.                |
| **Total**                      |                     | **65**|                                                            |

> **Docker is required for 4 of the 65 tests.** `RepositorioMySqlIntegrationTest`
> runs against a real MySQL 8.4.7 container started by **Testcontainers**. When
> Docker is not available, those 4 tests are **skipped automatically**
> (`@Testcontainers(disabledWithoutDocker = true)`) and the remaining **61 tests**
> still run and pass.

- **Unit tests** isolate the service layer with Mockito.
- **Web slice tests** (`@WebMvcTest`) verify the controller layer and error mapping.
- **Integration tests** boot the full context against an in-memory **H2** database
  (test scope only) to validate the generated OpenAPI endpoints, and — for the
  transfers flow — run parallel threads that assert balance conservation, atomic
  rollback on failure, and balance/statement consistency.
- **MySQL integration tests** (`RepositorioMySqlIntegrationTest`, 4 tests) require
  **Docker**: they start a real MySQL 8.4.7 container via **Testcontainers** and are
  skipped automatically when Docker is not available.

### Front-End tests (25)

| Test file                              | Type           | Tests  | What it covers                                              |
|----------------------------------------|----------------|--------|-------------------------------------------------------------|
| `lib/formatters.test.ts`               | Unit           | 4      | BRL currency formatting and UTC date formatting.            |
| `lib/masks.test.ts`                    | Unit           | 7      | Digit extraction, CPF mask, currency mask and parsing.      |
| `lib/validators.test.ts`               | Unit           | 5      | CPF check digits (mod-11) and e-mail validation.            |
| `api/client.test.ts`                   | Unit (MSW)     | 3      | JSON success, 409 message mapping, 401 without body.        |
| `components/ui/CurrencyInput.test.tsx` | Component      | 2      | Masked input and validation message.                        |
| `features/auth/LoginPage.test.tsx`     | Component      | 2      | Invalid-credentials error and successful navigation.        |
| `features/contas/DepositoPage.test.tsx`| Component      | 2      | Deposit flow with updated balance and pre-submit validation.|
| **Total**                              |                | **25** |                                                             |

> The front-end tests use **MSW** to mock the HTTP layer, so they run without a
> live backend. They cover masking/formatting/validation logic, the API client
> error mapping, and the login and deposit flows via React Testing Library.

### Results (verified)

| Suite                      | Command                   | Result                          |
|----------------------------|---------------------------|---------------------------------|
| Back-End                   | `.\mvnw.cmd clean verify` | **65 / 65 passed** (0 skipped)  |
| Front-End                  | `npm test`                | **25 / 25 passed**              |
| Front-End type check       | `npm run typecheck`       | **0 TypeScript errors**         |
| Front-End production build | `npm run build`           | **success**                     |

> Docker was available during the last Back-End run, so the 4 Testcontainers
> tests executed against an isolated MySQL container (they were **not** skipped).

---

## 📸 Screenshots

> **Screenshots coming soon.** Captures of the running application (login,
> dashboard with a real returned balance, account statement, and transfer
> confirmation) are being prepared and will be added to a `docs/screenshots/`
> folder in a future update. No placeholder or fabricated images are included
> in this repository at the moment.

---

## 🔒 Security Limitations

This project is a **learning/portfolio project** and intentionally focuses on
backend architecture rather than production-grade security. Known limitations:

- **Authentication is demo-grade** — HTTP Basic with two **in-memory** users and
  local-development default passwords (`admin/admin123`, `cliente/cliente123`).
  There is no user store, registration, token/JWT, refresh, or per-customer
  ownership model. All `ADMIN` users can act on all accounts.
- **No TLS/HTTPS termination** in the application itself — with HTTP Basic, the
  credentials travel base64-encoded (not encrypted) unless TLS is terminated in
  front of the app.
- **Database connection is not encrypted in the default profile** — the JDBC URL
  uses `useSSL=false&allowPublicKeyRetrieval=true`, which is convenient for a
  local MySQL instance but transmits credentials and data in clear text. Use
  `sslMode=REQUIRED` (or `VERIFY_CA`) with a trusted certificate outside local
  development.
- **No rate limiting** or brute-force protection.
- **No audit logging** of sensitive operations.
- **Transfer idempotency** is not implemented. A robust solution would require
  persisting an idempotency key (a schema change), which was intentionally left
  out. Concurrent duplicate transfers are not deduplicated.
- **PII masking** covers CPF and e-mail in `ClienteResponse`; the customer name is
  still returned in full, and masking is not role-dependent.
- **Database credentials** are supplied via environment variables and must be kept
  out of version control. `.gitignore` excludes `.env` and local profile files;
  `application-prod.properties` is the deliberate exception (it contains no secrets).
- **Swagger UI / OpenAPI endpoints** are public and enabled by default; they are
  disabled only when the `prod` profile is active.
- **Error messages** return generic text for unexpected errors to avoid leaking
  internal details, but this is not a substitute for a full security review.
- **The Front-End is demo-grade too** — it validates credentials via a read-only
  probe and keeps them only in memory (see the [Front-End](#-front-end-react--typescript--vite)
  section). It is a UI demonstration, not a hardened client.

Do **not** deploy this project to the public internet as-is.

---

## 🔭 Roadmap

Planned improvements (not implemented yet). These were deliberately left out of
scope to keep the schema and the domain rules stable.

- **Transfer idempotency** via an `Idempotency-Key` header (requires a schema change).
- **Real authentication/authorization** — replace the in-memory demo users with a
  persistent identity provider (JWT/OAuth2) and add per-customer ownership checks.
- **Optimistic locking** (`@Version`) on accounts as a complement to pessimistic locks.
- **Relaxed/role-based PII masking** and minimization of personal data in responses.

---

## 📄 License

This project is released under the **MIT License**. See the [`LICENSE`](LICENSE)
file for the full text.

Copyright (c) 2026 Emanuel Vitor Fernandes Nascimento.
