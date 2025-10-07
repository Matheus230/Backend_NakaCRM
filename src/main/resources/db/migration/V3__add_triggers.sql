-- =================================================
-- TRIGGERS FOR UPDATED_AT
-- =================================================

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply trigger to tables with updated_at
CREATE TRIGGER update_tb_usuario_updated_at
    BEFORE UPDATE ON tb_usuario
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_tb_cliente_updated_at
    BEFORE UPDATE ON tb_cliente
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_tb_produto_updated_at
    BEFORE UPDATE ON tb_produto
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
