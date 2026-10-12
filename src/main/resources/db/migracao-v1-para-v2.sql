-- =============================================================================
-- Digital Banking API - migracao v1 -> v2
--
-- A versao simplificada deixou de usar as tabelas `clientes` e `transacoes`
-- e passou a guardar o titular direto na tabela `contas` (coluna `titular`).
--
-- Este script PRESERVA os dados existentes: cada conta recebe como titular o
-- nome do cliente que era o seu dono (via a antiga FK cliente_id) antes de a
-- coluna ser removida.
--
-- ATENCAO: faca um backup antes de executar:
--   mysqldump -u root -p digital_banking > backup_digital_banking.sql
--
-- Execute manualmente (MySQL Workbench ou linha de comando), com o banco
-- digital_banking existente.
-- =============================================================================

USE digital_banking;

-- 1) Adiciona a coluna titular (temporariamente nula).
ALTER TABLE contas ADD COLUMN titular VARCHAR(120) NULL AFTER numero;

-- 2) Preenche o titular a partir do cliente vinculado (dados preservados).
UPDATE contas c
JOIN clientes cl ON c.cliente_id = cl.id
SET c.titular = cl.nome
WHERE c.titular IS NULL;

-- 3) Protege contas orfas (caso existam) para permitir o NOT NULL.
UPDATE contas SET titular = 'Titular nao informado' WHERE titular IS NULL;

-- 4) Torna o titular obrigatorio.
ALTER TABLE contas MODIFY COLUMN titular VARCHAR(120) NOT NULL;

-- 5) Remove a antiga relacao com clientes.
ALTER TABLE contas DROP FOREIGN KEY fk_conta_cliente;
ALTER TABLE contas DROP KEY idx_conta_cliente;
ALTER TABLE contas DROP COLUMN cliente_id;

-- 6) Remove as tabelas que nao sao mais usadas.
DROP TABLE IF EXISTS transacoes;
DROP TABLE IF EXISTS clientes;
