-- =================================================
-- CRM DATABASE - POSTGRESQL DUMP (UPDATED TO BIGINT)
-- =================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =================================================
-- TABLES
-- =================================================

-- Usuários do sistema
CREATE TABLE tb_usuario (
                            id_usuario SERIAL PRIMARY KEY,
                            nome VARCHAR(100) NOT NULL,
                            email VARCHAR(255) UNIQUE NOT NULL,
                            senha_hash VARCHAR(255) NOT NULL,
                            tipo_usuario VARCHAR(20) NOT NULL CHECK (tipo_usuario IN ('ADMIN', 'VENDEDOR')),
                            google_id VARCHAR(255) UNIQUE,
                            ativo BOOLEAN DEFAULT true,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Clientes/Leads
CREATE TABLE tb_cliente (
                            id_cliente SERIAL PRIMARY KEY,
                            nome VARCHAR(255) NOT NULL,
                            email VARCHAR(255) UNIQUE NOT NULL,
                            telefone VARCHAR(20),
                            endereco TEXT,
                            cidade VARCHAR(100),
                            estado VARCHAR(2),
                            cep VARCHAR(10),
                            empresa VARCHAR(255),
                            cargo VARCHAR(100),
                            origem_lead VARCHAR(30) NOT NULL CHECK (origem_lead IN ('GOOGLE_FORMS', 'LANDING_PAGE', 'MANUAL')),
                            status_lead VARCHAR(30) DEFAULT 'NOVO' CHECK (status_lead IN ('NOVO', 'CONTATADO', 'QUALIFICADO', 'OPORTUNIDADE', 'CLIENTE', 'PERDIDO')),
                            data_primeiro_contato TIMESTAMP,
                            data_ultima_interacao TIMESTAMP,
                            observacoes TEXT,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Produtos
CREATE TABLE tb_produto (
                            id_produto SERIAL PRIMARY KEY,
                            nome VARCHAR(255) NOT NULL,
                            descricao TEXT,
                            categoria VARCHAR(100),
                            preco DECIMAL(10,2) NOT NULL,
                            pago BOOLEAN,
                            tipo_pagamento VARCHAR(20) CHECK (tipo_pagamento IN ('CARTAO', 'PIX', 'BOLETO')),
                            tipo_cobranca VARCHAR(20) DEFAULT 'UNICO' CHECK (tipo_cobranca IN ('UNICO', 'MENSAL', 'ANUAL')),
                            ativo BOOLEAN DEFAULT true,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Origem detalhada dos leads
CREATE TABLE tb_lead_origem (
                                id_lead_origem SERIAL PRIMARY KEY,
                                id_cliente INTEGER UNIQUE NOT NULL,
                                fonte_detalhada VARCHAR(255),
                                utm_source VARCHAR(100),
                                utm_medium VARCHAR(100),
                                utm_campaign VARCHAR(100),
                                user_agent TEXT,
                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                CONSTRAINT fk_lead_origem_cliente FOREIGN KEY (id_cliente) REFERENCES tb_cliente(id_cliente) ON DELETE CASCADE
);

-- Interesses do cliente por produto
CREATE TABLE tb_cliente_interesse (
                                      id_interesse SERIAL PRIMARY KEY,
                                      id_cliente INTEGER NOT NULL,
                                      id_produto INTEGER NOT NULL,
                                      nivel_interesse VARCHAR(20) DEFAULT 'MEDIO' CHECK (nivel_interesse IN ('BAIXO', 'MEDIO', 'ALTO')),
                                      observacoes TEXT,
                                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                      CONSTRAINT fk_interesse_cliente FOREIGN KEY (id_cliente) REFERENCES tb_cliente(id_cliente) ON DELETE CASCADE,
                                      CONSTRAINT fk_interesse_produto FOREIGN KEY (id_produto) REFERENCES tb_produto(id_produto) ON DELETE CASCADE,
                                      CONSTRAINT uk_cliente_produto UNIQUE (id_cliente, id_produto)
);

-- Timeline de interações com cliente
CREATE TABLE tb_interacao_cliente (
                                      id_interacao SERIAL PRIMARY KEY,
                                      id_cliente INTEGER NOT NULL,
                                      id_usuario INTEGER,
                                      tipo_interacao VARCHAR(30) NOT NULL CHECK (tipo_interacao IN ('EMAIL', 'TELEFONE', 'WHATSAPP', 'FORM_SUBMIT', 'SITE_VISIT', 'NOTA_INTERNA')),
                                      descricao TEXT NOT NULL,
                                      dados_extras JSONB,
                                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                      CONSTRAINT fk_interacao_cliente FOREIGN KEY (id_cliente) REFERENCES tb_cliente(id_cliente) ON DELETE CASCADE,
                                      CONSTRAINT fk_interacao_usuario FOREIGN KEY (id_usuario) REFERENCES tb_usuario(id_usuario) ON DELETE SET NULL
);

-- =================================================
-- MIGRATION TO BIGINT
-- =================================================

-- Drop foreign key constraints first
ALTER TABLE tb_lead_origem DROP CONSTRAINT fk_lead_origem_cliente;
ALTER TABLE tb_cliente_interesse DROP CONSTRAINT fk_interesse_cliente;
ALTER TABLE tb_cliente_interesse DROP CONSTRAINT fk_interesse_produto;
ALTER TABLE tb_interacao_cliente DROP CONSTRAINT fk_interacao_cliente;
ALTER TABLE tb_interacao_cliente DROP CONSTRAINT fk_interacao_usuario;

-- Convert all primary keys
ALTER TABLE tb_usuario ALTER COLUMN id_usuario TYPE BIGINT;
ALTER TABLE tb_cliente ALTER COLUMN id_cliente TYPE BIGINT;
ALTER TABLE tb_produto ALTER COLUMN id_produto TYPE BIGINT;
ALTER TABLE tb_lead_origem ALTER COLUMN id_lead_origem TYPE BIGINT;
ALTER TABLE tb_cliente_interesse ALTER COLUMN id_interesse TYPE BIGINT;
ALTER TABLE tb_interacao_cliente ALTER COLUMN id_interacao TYPE BIGINT;

-- Convert all foreign keys
ALTER TABLE tb_lead_origem ALTER COLUMN id_cliente TYPE BIGINT;
ALTER TABLE tb_cliente_interesse ALTER COLUMN id_cliente TYPE BIGINT;
ALTER TABLE tb_cliente_interesse ALTER COLUMN id_produto TYPE BIGINT;
ALTER TABLE tb_interacao_cliente ALTER COLUMN id_cliente TYPE BIGINT;
ALTER TABLE tb_interacao_cliente ALTER COLUMN id_usuario TYPE BIGINT;

-- Re-add constraints
ALTER TABLE tb_lead_origem ADD CONSTRAINT fk_lead_origem_cliente FOREIGN KEY (id_cliente) REFERENCES tb_cliente(id_cliente) ON DELETE CASCADE;
ALTER TABLE tb_cliente_interesse ADD CONSTRAINT fk_interesse_cliente FOREIGN KEY (id_cliente) REFERENCES tb_cliente(id_cliente) ON DELETE CASCADE;
ALTER TABLE tb_cliente_interesse ADD CONSTRAINT fk_interesse_produto FOREIGN KEY (id_produto) REFERENCES tb_produto(id_produto) ON DELETE CASCADE;
ALTER TABLE tb_interacao_cliente ADD CONSTRAINT fk_interacao_cliente FOREIGN KEY (id_cliente) REFERENCES tb_cliente(id_cliente) ON DELETE CASCADE;
ALTER TABLE tb_interacao_cliente ADD CONSTRAINT fk_interacao_usuario FOREIGN KEY (id_usuario) REFERENCES tb_usuario(id_usuario) ON DELETE SET NULL;

-- =================================================
-- INDEXES
-- =================================================

CREATE INDEX idx_cliente_status_lead ON tb_cliente(status_lead);
CREATE INDEX idx_cliente_origem_lead ON tb_cliente(origem_lead);
CREATE INDEX idx_cliente_email ON tb_cliente(email);

CREATE INDEX idx_interacao_cliente_data ON tb_interacao_cliente(id_cliente, created_at DESC);

CREATE INDEX idx_produto_ativo ON tb_produto(ativo, categoria);

CREATE INDEX idx_usuario_ativo ON tb_usuario(ativo, tipo_usuario);

-- =================================================
-- TRIGGERS PARA UPDATED_AT
-- =================================================

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_tb_usuario_updated_at BEFORE UPDATE ON tb_usuario
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_tb_cliente_updated_at BEFORE UPDATE ON tb_cliente
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_tb_produto_updated_at BEFORE UPDATE ON tb_produto
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =================================================
-- DADOS INICIAIS
-- =================================================

INSERT INTO tb_usuario (nome, email, senha_hash, tipo_usuario) VALUES
    ('Administrador', 'admin@empresa.com', 'admin', 'ADMIN');

-- =================================================
-- VIEWS ÚTEIS
-- =================================================

-- View de leads qualificados
CREATE VIEW vw_leads_qualificados AS
SELECT
    c.id_cliente,
    c.nome,
    c.email,
    c.telefone,
    c.empresa,
    c.status_lead,
    c.origem_lead,
    c.data_primeiro_contato,
    lo.utm_source,
    lo.utm_campaign
FROM tb_cliente c
         LEFT JOIN tb_lead_origem lo ON c.id_cliente = lo.id_cliente
WHERE c.status_lead IN ('QUALIFICADO', 'OPORTUNIDADE');

-- View de produtos com interesse
CREATE VIEW vw_produtos_interesse AS
SELECT
    p.id_produto,
    p.nome as produto_nome,
    p.categoria,
    p.preco,
    COUNT(ci.id_cliente) as total_interessados,
    COUNT(CASE WHEN ci.nivel_interesse = 'ALTO' THEN 1 END) as interesse_alto
FROM tb_produto p
         LEFT JOIN tb_cliente_interesse ci ON p.id_produto = ci.id_produto
WHERE p.ativo = true
GROUP BY p.id_produto, p.nome, p.categoria, p.preco
ORDER BY total_interessados DESC;