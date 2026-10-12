# Digital Banking

Aplicação bancária desenvolvida com Java, Spring Boot, MySQL e React, com foco no desenvolvimento de APIs REST, programação orientada a objetos e integração entre front-end e back-end.

## Tecnologias utilizadas

**Back-end**
- Java 25
- Spring Boot 4
- Spring Data JPA
- Maven

**Front-end**
- React 19
- TypeScript
- Vite

**Banco de dados**
- MySQL

## Funcionalidades

- Criação de contas bancárias.
- Consulta de contas e saldo.
- Depósitos.
- Saques.
- Validação de valores e saldo disponível.

Regras aplicadas:

- Não aceita valores iguais ou menores que zero.
- Não aceita saque maior que o saldo disponível.
- Não permite operação em conta inexistente.
- Os valores monetários usam `BigDecimal` e as operações rodam em transação, com bloqueio da conta para evitar saldo inconsistente.

### Endpoints da API

| Método | Rota                    | Função          |
|--------|-------------------------|-----------------|
| POST   | `/contas`               | Criar conta     |
| GET    | `/contas`               | Listar contas   |
| GET    | `/contas/{id}`          | Consultar conta |
| GET    | `/contas/{id}/saldo`    | Consultar saldo |
| POST   | `/contas/{id}/deposito` | Depositar       |
| POST   | `/contas/{id}/saque`    | Sacar           |

## Demonstração da interface

Capturas reais da aplicação em execução (banco de demonstração).

**Tela inicial — lista de contas**

![Tela inicial com a lista de contas](docs/screenshots/tela-inicial.png)

**Criação de conta**

![Formulário de criação de conta](docs/screenshots/criar-conta.png)

**Operações bancárias — saldo, depósito e saque**

![Tela de operações de uma conta](docs/screenshots/operacoes.png)

## Como executar

Instruções para Windows com VS Code.

**Pré-requisitos**
- JDK 25 (o projeto é configurado para Java 25).
- MySQL em execução em `localhost:3306`.
- Node.js 20 ou superior (testado com Node 24).
- Git, caso queira clonar o repositório.

**1. Clonar o repositório**

```powershell
git clone https://github.com/emanuelvitorfn7-gif/digital-banking-api.git
cd digital-banking-api
```

**2. Criar o banco de dados**

O banco se chama `digital_banking` e não é criado automaticamente pela aplicação.
Execute o script de criação do schema:

```powershell
mysql -u root -p < src\main\resources\db\schema.sql
```

> Se o comando `mysql` não estiver no PATH, use o caminho completo do executável do
> seu MySQL. Se você já possui o banco criado na versão antiga (tabelas
> `clientes`/`transacoes`) e quer preservar os dados, use
> `src\main\resources\db\migracao-v1-para-v2.sql` no lugar do `schema.sql`.

**3. Configurar as variáveis de ambiente**

As credenciais do banco são lidas de variáveis de ambiente (padrões: usuário `root`, senha vazia):

```powershell
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = ""   # informe a senha do seu MySQL, se houver
```

**4. Back-end (Spring Boot)** — em um terminal do VS Code:

```powershell
.\mvnw.cmd spring-boot:run
```

A API sobe em **http://localhost:8081**.

**5. Front-end (React)** — em outro terminal:

```powershell
cd frontend
npm install
npm run dev
```

A interface abre em **http://localhost:5173**. Durante o desenvolvimento, o Vite
encaminha as chamadas `/api/*` para `http://localhost:8081`, então não é preciso
configurar CORS.

## Próximas melhorias

- Transferências entre contas.
- Histórico de transações.
- Autenticação de usuários.
- Melhorias na interface.
- Deploy demonstrativo.

## Sobre o projeto

O Digital Banking é um projeto de estudo e prática de desenvolvimento back-end e
integração com banco de dados. A ideia é evoluir gradualmente, começando por uma
base simples e funcional (contas, depósitos e saques) e adicionando novos recursos
com o tempo, sempre mantendo o código organizado e fácil de entender.
