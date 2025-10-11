-- =================================================
-- CLIENTES/LEADS
-- =================================================
INSERT INTO tb_cliente (nome, email, telefone, endereco, cidade, estado, cep, empresa, cargo, origem_lead, status_lead, data_primeiro_contato, data_ultima_interacao, observacoes) VALUES
                                                                                                                                                                                       ('Leonardo Contador', 'leonardo.contador@empresa.com', '(11) 98765-4321', 'Rua das Flores, 123', 'São Paulo', 'SP', '01234-567', 'Contabilidade Express', 'Contador', 'MANUAL', 'QUALIFICADO', NOW() - INTERVAL '5 days', NOW() - INTERVAL '1 day', 'Indicação: Carlinhos // evento'),
                                                                                                                                                                                       ('Robson Longo', 'robson.longo@empresa.com', '(11) 97654-3210', 'Av. Paulista, 1000', 'São Paulo', 'SP', '01310-100', 'Tech Solutions', 'Diretor de TI', 'LANDING_PAGE', 'OPORTUNIDADE', NOW() - INTERVAL '10 days', NOW() - INTERVAL '2 days', 'Interesse em CRM corporativo'),
                                                                                                                                                                                       ('Marcelo Info', 'marcelo.info@empresa.com', '(11) 96543-2109', 'Rua da Inovação, 456', 'São Paulo', 'SP', '04567-890', 'Startup Digital', 'CTO', 'GOOGLE_FORMS', 'CONTATADO', NOW() - INTERVAL '15 days', NOW() - INTERVAL '3 days', 'Encontrado no evento de tecnologia'),
                                                                                                                                                                                       ('Portela Comercial', 'portela@empresa.com', '(11) 95432-1098', 'Av. Comercial, 789', 'São Paulo', 'SP', '05678-901', 'Comércio Portela', 'Proprietário', 'MANUAL', 'CLIENTE', NOW() - INTERVAL '30 days', NOW() - INTERVAL '1 day', 'Cliente ativo - renovação anual'),
                                                                                                                                                                                       ('Fabricio Silva', 'fabricio.silva@empresa.com', '(11) 94321-0987', 'Rua Design, 321', 'São Paulo', 'SP', '06789-012', 'Design Studio', 'Designer', 'MANUAL', 'NOVO', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days', 'Instagram // Indicação: Patricio'),
                                                                                                                                                                                       ('Carlos Pai da Karol', 'carlos.sousa@empresa.com', '(11) 93210-9876', 'Rua Família, 654', 'São Paulo', 'SP', '07890-123', 'Negócio Familiar', 'Empresário', 'MANUAL', 'PERDIDO', NOW() - INTERVAL '60 days', NOW() - INTERVAL '45 days', 'Optou por concorrente'),
                                                                                                                                                                                       ('Erika Marketing', 'erika@empresa.com', '(11) 92109-8765', 'Av. Marketing, 987', 'São Paulo', 'SP', '08901-234', 'Agência Digital', 'Gerente de Marketing', 'LANDING_PAGE', 'QUALIFICADO', NOW() - INTERVAL '7 days', NOW() - INTERVAL '1 day', 'Muito interessada em automação'),
                                                                                                                                                                                       ('Roberta Vendas', 'roberta@empresa.com', '(11) 91098-7654', 'Rua Vendas, 147', 'São Paulo', 'SP', '09012-345', 'Vendas Plus', 'Diretora Comercial', 'GOOGLE_FORMS', 'OPORTUNIDADE', NOW() - INTERVAL '4 days', NOW() - INTERVAL '1 day', 'Proposta enviada - aguardando retorno');

-- =================================================
-- PRODUTOS
-- =================================================
INSERT INTO tb_produto (nome, descricao, categoria, preco, pago, tipo_pagamento, tipo_cobranca, ativo) VALUES
                                                                                                           ('Seven', 'Plano completo com todas as funcionalidades', 'CRM Premium', 600.00, true, 'PIX', 'MENSAL', true),
                                                                                                           ('MVI', 'Plano intermediário para médias empresas', 'CRM Intermediário', 360.00, true, 'CARTAO', 'MENSAL', true),
                                                                                                           ('Hub', 'Plano básico para pequenas empresas', 'CRM Básico', 600.00, true, 'PIX', 'ANUAL', true),
                                                                                                           ('Programa 8 Semanas', 'Programa de onboarding e treinamento', 'Treinamento', 150.00, true, 'PIX', 'UNICO', true),
                                                                                                           ('Portela', 'Plano customizado para comércio', 'CRM Especializado', 570.00, true, 'PIX', 'MENSAL', true),
                                                                                                           ('Matéria de Jornal', 'Divulgação em mídia especializada', 'Marketing', 89.97, true, 'PIX', 'UNICO', true);

-- =================================================
-- ORIGEM DOS LEADS
-- =================================================
INSERT INTO tb_lead_origem (id_cliente, fonte_detalhada, utm_source, utm_medium, utm_campaign, user_agent) VALUES
                                                                                                               (35, 'MANUAL', 'Indicação: Carlinhos // evento', 'evt', 'evento', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)'),
                                                                                                               (36, 'MANUAL', 'Instagram ', 'organic', 'crm_corporativo', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)'),
                                                                                                               (37, 'MANUAL', 'N/A', 'N/A', 'N/A', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)'),
                                                                                                               (38, 'MANUAL', 'Indicação: Thiago // evento', 'direct', 'boca_a_boca', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)'),
                                                                                                               (39, 'MANUAL', 'Instagram // Indicação: patricio', 'social', 'Instagram', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)'),
                                                                                                               (40, 'MANUAL', 'n/a', 'social', 'Instagram', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)'),
                                                                                                               (41, 'MANUAL', 'Instagram', 'social', 'Instagram', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)'),
                                                                                                               (42, 'MANUAL', 'Instagram', 'social', 'Instagram', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)');

-- =================================================
-- INTERESSES DOS CLIENTES
-- =================================================
INSERT INTO tb_cliente_interesse (id_cliente, id_produto, nivel_interesse, observacoes) VALUES
                                                                                            (35, 7, 'MEDIO', 'Ainda não decidiu'),
                                                                                            (36, 7, 'ALTO', 'Fechou'),
                                                                                            (37, 8, 'BAIXO', 'Não comprou'),
                                                                                            (38, 7, 'ALTO', 'Fechou'),
                                                                                            (39, 9, 'BAIXO', 'Não comprou'),
                                                                                            (40, 9, 'BAIXO', 'Não comprou'),
                                                                                            (41, 8, 'BAIXO', 'Não comprou'),
                                                                                            (42, 7, 'BAIXO', 'Não comprou');