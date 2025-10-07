-- =================================================
-- INDEXES FOR PERFORMANCE
-- =================================================

-- Clientes indexes
CREATE INDEX idx_cliente_status_lead ON tb_cliente(status_lead);
CREATE INDEX idx_cliente_origem_lead ON tb_cliente(origem_lead);
CREATE INDEX idx_cliente_email ON tb_cliente(email);
CREATE INDEX idx_cliente_empresa ON tb_cliente(empresa);
CREATE INDEX idx_cliente_data_ultima_interacao ON tb_cliente(data_ultima_interacao);
CREATE INDEX idx_cliente_created_at ON tb_cliente(created_at);

-- Interações index for timeline queries
CREATE INDEX idx_interacao_cliente_data ON tb_interacao_cliente(id_cliente, created_at DESC);
CREATE INDEX idx_interacao_tipo ON tb_interacao_cliente(tipo_interacao);

-- Produtos indexes
CREATE INDEX idx_produto_ativo ON tb_produto(ativo, categoria);
CREATE INDEX idx_produto_categoria ON tb_produto(categoria);

-- Usuários indexes
CREATE INDEX idx_usuario_ativo ON tb_usuario(ativo, tipo_usuario);
CREATE INDEX idx_usuario_email ON tb_usuario(email);

-- Lead origem indexes
CREATE INDEX idx_lead_origem_utm_source ON tb_lead_origem(utm_source);
CREATE INDEX idx_lead_origem_utm_campaign ON tb_lead_origem(utm_campaign);

-- Cliente interesse indexes
CREATE INDEX idx_interesse_cliente ON tb_cliente_interesse(id_cliente);
CREATE INDEX idx_interesse_produto ON tb_cliente_interesse(id_produto);
CREATE INDEX idx_interesse_nivel ON tb_cliente_interesse(nivel_interesse);
