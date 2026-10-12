-- =============================================================================
-- Digital Banking API - schema (instalacao nova)
-- Banco: digital_banking (MySQL 8)
--
-- Use este script quando o banco ainda NAO existe ou esta vazio.
-- Se voce ja possui as tabelas antigas (clientes/contas/transacoes) com dados,
-- use db/migracao-v1-para-v2.sql para preservar os dados.
--
-- Valor monetario sempre em DECIMAL(19,2).
-- =============================================================================

CREATE DATABASE IF NOT EXISTS digital_banking
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

USE digital_banking;

CREATE TABLE IF NOT EXISTS contas (
    id       BIGINT        NOT NULL AUTO_INCREMENT,
    numero   VARCHAR(20)   NOT NULL,
    titular  VARCHAR(120)  NOT NULL,
    saldo    DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    PRIMARY KEY (id),
    CONSTRAINT uk_conta_numero UNIQUE (numero)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
