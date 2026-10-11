-- =============================================================================
-- Digital Banking API - Schema de criacao das tabelas
-- Banco: digital_banking (MySQL 8.4.7)
--
-- IMPORTANTE:
--   * Script idempotente: usa CREATE TABLE IF NOT EXISTS (nao recria tabelas existentes).
--   * NAO contem DROP nem TRUNCATE.
--   * NAO altera, exclui ou recria bancos existentes.
--   * As tabelas criadas sao apenas: clientes, contas, transacoes.
--   * Valores monetarios em DECIMAL(19,2).
--   * Aplicar manualmente (MySQL Workbench) na conexao apontando para digital_banking.
-- =============================================================================

USE digital_banking;

-- -----------------------------------------------------------------------------
-- Tabela: clientes
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS clientes (
    id     BIGINT       NOT NULL AUTO_INCREMENT,
    nome   VARCHAR(120) NOT NULL,
    cpf    VARCHAR(11)  NOT NULL,
    email  VARCHAR(150) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_cliente_cpf   UNIQUE (cpf),
    CONSTRAINT uk_cliente_email UNIQUE (email)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

-- -----------------------------------------------------------------------------
-- Tabela: contas
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS contas (
    id         BIGINT        NOT NULL AUTO_INCREMENT,
    numero     VARCHAR(20)   NOT NULL,
    saldo      DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    cliente_id BIGINT        NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_conta_numero UNIQUE (numero),
    KEY idx_conta_cliente (cliente_id),
    CONSTRAINT fk_conta_cliente FOREIGN KEY (cliente_id)
        REFERENCES clientes (id)
        ON UPDATE RESTRICT
        ON DELETE RESTRICT
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

-- -----------------------------------------------------------------------------
-- Tabela: transacoes
--   conta_origem_id  -> conta debitada (SAQUE, TRANSFERENCIA)
--   conta_destino_id -> conta creditada (DEPOSITO, TRANSFERENCIA)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS transacoes (
    id              BIGINT        NOT NULL AUTO_INCREMENT,
    tipo            VARCHAR(20)   NOT NULL,
    valor           DECIMAL(19,2) NOT NULL,
    data_hora       DATETIME(6)   NOT NULL,
    conta_origem_id  BIGINT       NULL,
    conta_destino_id BIGINT       NULL,
    PRIMARY KEY (id),
    KEY idx_transacao_origem (conta_origem_id),
    KEY idx_transacao_destino (conta_destino_id),
    KEY idx_transacao_origem_data_hora (conta_origem_id, data_hora DESC),
    KEY idx_transacao_destino_data_hora (conta_destino_id, data_hora DESC),
    CONSTRAINT fk_transacao_conta_origem FOREIGN KEY (conta_origem_id)
        REFERENCES contas (id)
        ON UPDATE RESTRICT
        ON DELETE RESTRICT,
    CONSTRAINT fk_transacao_conta_destino FOREIGN KEY (conta_destino_id)
        REFERENCES contas (id)
        ON UPDATE RESTRICT
        ON DELETE RESTRICT
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
