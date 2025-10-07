-- =================================================
-- USEFUL VIEWS
-- =================================================

-- View of qualified leads
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

-- View of products with interest stats
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
