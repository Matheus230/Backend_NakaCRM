-- =================================================
-- CRM DATABASE - INITIAL SCHEMA
-- =================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =================================================
-- TABLES
-- =================================================

-- Usuários do sistema
CREATE TABLE tb_usuario (
    id_usuario BIGSERIAL PRIMARY KEY,
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
    id_cliente BIGSERIAL PRIMARY KEY,
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
    id_produto BIGSERIAL PRIMARY KEY,
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
    id_lead_origem BIGSERIAL PRIMARY KEY,
    id_cliente BIGINT UNIQUE NOT NULL,
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
    id_interesse BIGSERIAL PRIMARY KEY,
    id_cliente BIGINT NOT NULL,
    id_produto BIGINT NOT NULL,
    nivel_interesse VARCHAR(20) DEFAULT 'MEDIO' CHECK (nivel_interesse IN ('BAIXO', 'MEDIO', 'ALTO')),
    observacoes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_interesse_cliente FOREIGN KEY (id_cliente) REFERENCES tb_cliente(id_cliente) ON DELETE CASCADE,
    CONSTRAINT fk_interesse_produto FOREIGN KEY (id_produto) REFERENCES tb_produto(id_produto) ON DELETE CASCADE,
    CONSTRAINT uk_cliente_produto UNIQUE (id_cliente, id_produto)
);

-- Timeline de interações com cliente
CREATE TABLE tb_interacao_cliente (
    id_interacao BIGSERIAL PRIMARY KEY,
    id_cliente BIGINT NOT NULL,
    id_usuario BIGINT,
    tipo_interacao VARCHAR(30) NOT NULL CHECK (tipo_interacao IN ('EMAIL', 'TELEFONE', 'WHATSAPP', 'FORM_SUBMIT', 'SITE_VISIT', 'NOTA_INTERNA')),
    descricao TEXT NOT NULL,
    dados_extras JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_interacao_cliente FOREIGN KEY (id_cliente) REFERENCES tb_cliente(id_cliente) ON DELETE CASCADE,
    CONSTRAINT fk_interacao_usuario FOREIGN KEY (id_usuario) REFERENCES tb_usuario(id_usuario) ON DELETE SET NULL
);
