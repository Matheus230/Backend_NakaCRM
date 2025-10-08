# 🧪 Guia de Testes - Backend NakaCRM

Este guia explica como testar o MVP do backend NakaCRM hands-on, incluindo todas as rotas disponíveis e o fluxo completo de uso.

## 📋 Índice

1. [Preparação do Ambiente](#preparação-do-ambiente)
2. [Dados de Teste](#dados-de-teste)
3. [Fluxo de Autenticação](#fluxo-de-autenticação)
4. [Gestão de Clientes/Leads](#gestão-de-clientesleads)
5. [Gestão de Produtos](#gestão-de-produtos)
6. [Interações com Clientes](#interações-com-clientes)
7. [Sistema de Email](#sistema-de-email)
8. [Dashboard e Estatísticas](#dashboard-e-estatísticas)

---

## 🚀 Preparação do Ambiente

### 1. Iniciar Serviços

```bash
# Iniciar banco de dados e MailHog
docker-compose up -d

# Verificar se os serviços estão rodando
docker ps

# Injetar dados de teste (se ainda não fez)
docker exec -i crm-postgres psql -U crm_user -d crm_db < mock-data.sql
```

### 2. Iniciar Aplicação Spring Boot

```bash
# Via Maven
./mvnw spring-boot:run

# Ou via IDE (IntelliJ/Eclipse)
# Run BackendApplication.java
```

### 3. Acessar Serviços

- **API Backend**: http://localhost:8080/api
- **Swagger UI**: http://localhost:8080/api/swagger-ui.html
- **MailHog (Emails)**: http://localhost:8025
- **Base URL**: Todas as rotas começam com `/api`

---

## 📊 Dados de Teste

### Usuários Disponíveis

| Email | Senha | Tipo | Descrição |
|-------|-------|------|-----------|
| `admin@nakacorp.com` | `senha123` | ADMIN | Administrador com acesso total |
| `joao@nakacorp.com` | `senha123` | VENDEDOR | Vendedor João |
| `maria@nakacorp.com` | `senha123` | VENDEDOR | Vendedora Maria |

### Clientes por Status

- **NOVO** (2): Carlos Mendes, Ana Paula Santos
- **CONTATADO** (2): Roberto Silva, Juliana Ferreira
- **QUALIFICADO** (2): Pedro Oliveira, Fernanda Lima
- **OPORTUNIDADE** (2): Ricardo Costa, Lucia Martins
- **CLIENTE** (2): Marcos Souza, Beatriz Alves
- **PERDIDO** (1): Gabriel Rocha

### Produtos Disponíveis

1. Plano Básico CRM - R$ 99,90/mês
2. Plano Premium CRM - R$ 299,90/mês
3. Consultoria em Vendas - R$ 1.500,00
4. Treinamento Equipe - R$ 2.500,00
5. Plano Anual Premium - R$ 2.999,00/ano

---

## 🔐 Fluxo de Autenticação

### 1. Login (Obter Token JWT)

**Por que primeiro?** Todas as rotas (exceto login) requerem autenticação via JWT.

```bash
POST /api/auth/login
Content-Type: application/json

{
  "email": "admin@nakacorp.com",
  "password": "senha123"
}
```

**Resposta:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "expiresIn": 86400000,
  "usuario": {
    "id": 1,
    "nome": "Admin Silva",
    "email": "admin@nakacorp.com",
    "tipoUsuario": "ADMIN"
  }
}
```

**⚠️ IMPORTANTE**: Copie o `token` e use em todas as próximas requisições no header:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 2. Refresh Token (Renovar Token)

**Por que usar?** Quando o token expirar (24h), renove sem fazer login novamente.

```bash
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### 3. Logout

```bash
POST /api/auth/logout
Authorization: Bearer {seu-token}
```

---

## 👥 Gestão de Clientes/Leads

### 1. Listar Todos os Clientes

**Por que primeiro?** Ver todos os leads no sistema e entender o funil de vendas.

```bash
GET /api/clientes
Authorization: Bearer {seu-token}
```

**Resposta**: Lista com 11 clientes nos diferentes estágios do funil.

### 2. Buscar Cliente por ID

```bash
GET /api/clientes/1
Authorization: Bearer {seu-token}
```

**Resposta**: Detalhes completos do Carlos Mendes (NOVO).

### 3. Filtrar Clientes por Status

**Por que usar?** Gerenciar o funil de vendas por estágio.

```bash
# Ver apenas leads NOVOS (precisa de primeira ação)
GET /api/clientes?status=NOVO

# Ver apenas OPORTUNIDADES (próximos de fechar)
GET /api/clientes?status=OPORTUNIDADE

# Ver CLIENTES ativos (já fecharam)
GET /api/clientes?status=CLIENTE
```

### 4. Buscar Clientes por Nome ou Email

```bash
# Por nome
GET /api/clientes/buscar?termo=carlos

# Por email
GET /api/clientes/buscar?termo=carlos.mendes@empresa1.com
```

### 5. Criar Novo Lead

**Por que testar?** Simular captura de lead de um formulário.

```bash
POST /api/clientes
Authorization: Bearer {seu-token}
Content-Type: application/json

{
  "nome": "João Teste",
  "email": "joao.teste@email.com",
  "telefone": "(11) 99999-9999",
  "empresa": "Empresa Teste LTDA",
  "cargo": "Gerente",
  "origemLead": "LANDING_PAGE",
  "observacoes": "Lead de teste via API"
}
```

**Resposta**: Lead criado com status `NOVO` e email de boas-vindas enviado automaticamente.

### 6. Atualizar Cliente

**Por que usar?** Atualizar informações ou mover no funil.

```bash
PUT /api/clientes/1
Authorization: Bearer {seu-token}
Content-Type: application/json

{
  "nome": "Carlos Mendes",
  "email": "carlos.mendes@empresa1.com",
  "telefone": "(11) 98765-4321",
  "statusLead": "CONTATADO",
  "observacoes": "Primeiro contato realizado por telefone"
}
```

### 7. Deletar Cliente

```bash
DELETE /api/clientes/1
Authorization: Bearer {seu-token}
```

---

## 🛍️ Gestão de Produtos

### 1. Listar Todos os Produtos

**Por que primeiro?** Ver o catálogo disponível para oferecer aos clientes.

```bash
GET /api/produtos
Authorization: Bearer {seu-token}
```

### 2. Buscar Produto por ID

```bash
GET /api/produtos/1
Authorization: Bearer {seu-token}
```

### 3. Filtrar Produtos Ativos

```bash
GET /api/produtos?ativo=true
```

### 4. Criar Novo Produto

```bash
POST /api/produtos
Authorization: Bearer {seu-token}
Content-Type: application/json

{
  "nome": "Plano Empresarial",
  "descricao": "Plano para grandes empresas",
  "categoria": "Software",
  "preco": 999.90,
  "tipoCobranca": "MENSAL",
  "ativo": true
}
```

### 5. Atualizar Produto

```bash
PUT /api/produtos/1
Authorization: Bearer {seu-token}
Content-Type: application/json

{
  "nome": "Plano Básico CRM",
  "preco": 89.90,
  "ativo": true
}
```

---

## 💬 Interações com Clientes

### 1. Listar Interações de um Cliente

**Por que importante?** Ver todo o histórico de contatos com o cliente (timeline).

```bash
GET /api/clientes/5/interacoes
Authorization: Bearer {seu-token}
```

**Resposta**: Timeline completo do Pedro Oliveira (QUALIFICADO) com todas as interações.

### 2. Registrar Nova Interação

**Por que usar?** Documentar cada contato com o cliente.

```bash
POST /api/clientes/5/interacoes
Authorization: Bearer {seu-token}
Content-Type: application/json

{
  "tipoInteracao": "TELEFONE",
  "descricao": "Ligação para agendar reunião de apresentação",
  "dadosExtras": {
    "duracao": "10min",
    "proximo_passo": "enviar_proposta"
  }
}
```

**Tipos de Interação Disponíveis:**
- `EMAIL` - Email enviado/recebido
- `TELEFONE` - Ligação telefônica
- `WHATSAPP` - Mensagem WhatsApp
- `FORM_SUBMIT` - Formulário preenchido
- `SITE_VISIT` - Visita ao site
- `NOTA_INTERNA` - Nota interna da equipe

### 3. Registrar Interesse em Produto

**Por que usar?** Conectar cliente a produtos específicos.

```bash
POST /api/clientes/5/interesses
Authorization: Bearer {seu-token}
Content-Type: application/json

{
  "idProduto": 2,
  "nivelInteresse": "ALTO",
  "observacoes": "Cliente quer demonstração do Plano Premium"
}
```

### 4. Listar Interesses do Cliente

```bash
GET /api/clientes/5/interesses
Authorization: Bearer {seu-token}
```

---

## 📧 Sistema de Email

### 1. Enviar Email de Boas-Vindas

**Por que testar?** Automaticamente enviado ao criar novo lead, mas pode reenviar.

```bash
POST /api/clientes/1/enviar-boas-vindas
Authorization: Bearer {seu-token}
```

**Resultado**: Email enviado para o MailHog. Acesse http://localhost:8025 para visualizar.

### 2. Enviar Email de Follow-Up

**Por que usar?** Fazer acompanhamento personalizado.

```bash
POST /api/clientes/3/enviar-follow-up
Authorization: Bearer {seu-token}
Content-Type: application/json

{
  "mensagem": "Gostaria de agendar uma reunião para apresentar nossa solução CRM. Você tem disponibilidade esta semana?"
}
```

### 3. Enviar Email Promocional

```bash
POST /api/clientes/5/enviar-promocional
Authorization: Bearer {seu-token}
Content-Type: application/json

{
  "titulo": "Plano Premium com 20% de Desconto",
  "descricao": "Oferta exclusiva válida até o final do mês. Aproveite!"
}
```

### 4. Enviar Email de Cobrança - Lembrete

**Por que usar?** Avisar cliente sobre pagamento próximo do vencimento.

```bash
POST /api/emails/lembrete-cobranca
Authorization: Bearer {seu-token}
Content-Type: application/json

{
  "destinatario": "marcos.souza@empresa2.com",
  "nomeCliente": "Marcos Souza",
  "numeroFatura": "INV-2024-001",
  "dataVencimento": "15/10/2024",
  "descricao": "Mensalidade Plano Premium - Outubro/2024",
  "valor": 299.90,
  "linkPagamento": "https://pay.nakacorp.com/inv/001"
}
```

### 5. Enviar Email de Cobrança - Vencido

**Por que usar?** Notificar sobre pagamento atrasado com multas.

```bash
POST /api/emails/cobranca-vencida
Authorization: Bearer {seu-token}
Content-Type: application/json

{
  "destinatario": "cliente@email.com",
  "nomeCliente": "Nome do Cliente",
  "numeroFatura": "INV-2024-002",
  "dataVencimento": "01/10/2024",
  "diasAtraso": 7,
  "descricao": "Mensalidade Plano Premium - Setembro/2024",
  "valorOriginal": 299.90,
  "taxaMulta": 2.0,
  "valorMulta": 5.99,
  "valorJuros": 2.10,
  "valorTotal": 307.99,
  "linkPagamento": "https://pay.nakacorp.com/inv/002"
}
```

### 6. Enviar Email de Confirmação de Pagamento

**Por que usar?** Confirmar recebimento do pagamento.

```bash
POST /api/emails/confirmacao-pagamento
Authorization: Bearer {seu-token}
Content-Type: application/json

{
  "destinatario": "marcos.souza@empresa2.com",
  "nomeCliente": "Marcos Souza",
  "numeroFatura": "INV-2024-001",
  "dataPagamento": "14/10/2024",
  "metodoPagamento": "PIX",
  "descricao": "Mensalidade Plano Premium - Outubro/2024",
  "valor": 299.90,
  "codigoConfirmacao": "PAY-ABC123XYZ",
  "linkRecibo": "https://receipts.nakacorp.com/abc123xyz"
}
```

**📬 Visualizar Emails**: Acesse http://localhost:8025 para ver todos os emails enviados.

---

## 📊 Dashboard e Estatísticas

### 1. Estatísticas Gerais

**Por que usar?** Visão geral do funil de vendas e métricas.

```bash
GET /api/dashboard/estatisticas
Authorization: Bearer {seu-token}
```

**Resposta:**
```json
{
  "totalClientes": 11,
  "clientesPorStatus": {
    "NOVO": 2,
    "CONTATADO": 2,
    "QUALIFICADO": 2,
    "OPORTUNIDADE": 2,
    "CLIENTE": 2,
    "PERDIDO": 1
  },
  "totalInteracoes": 43,
  "leadsMes": 11,
  "taxaConversao": 18.18
}
```

### 2. Listar Clientes Recentes

**Por que usar?** Ver leads mais novos que precisam de atenção.

```bash
GET /api/clientes/recentes?limit=5
Authorization: Bearer {seu-token}
```

### 3. Buscar Origem dos Leads

```bash
GET /api/clientes/1/origem
Authorization: Bearer {seu-token}
```

---

## 🔄 Fluxo Completo de Teste (Hands-On)

### Cenário 1: Lead Nova -> Cliente

```bash
# 1. Login
POST /api/auth/login
{ "email": "joao@nakacorp.com", "password": "senha123" }

# 2. Ver leads NOVOS (Ana Paula Santos - ID 2)
GET /api/clientes?status=NOVO

# 3. Ver detalhes da Ana Paula
GET /api/clientes/2

# 4. Ver histórico de interações
GET /api/clientes/2/interacoes

# 5. Registrar primeiro contato
POST /api/clientes/2/interacoes
{
  "tipoInteracao": "TELEFONE",
  "descricao": "Primeira ligação - Startup muito interessada"
}

# 6. Atualizar status para CONTATADO
PUT /api/clientes/2
{ "statusLead": "CONTATADO" }

# 7. Enviar email de follow-up
POST /api/clientes/2/enviar-follow-up
{ "mensagem": "Oi Ana! Foi ótimo conversar com você..." }

# 8. Registrar interesse em produto
POST /api/clientes/2/interesses
{ "idProduto": 2, "nivelInteresse": "ALTO" }

# 9. Qualificar lead
PUT /api/clientes/2
{ "statusLead": "QUALIFICADO", "observacoes": "Budget aprovado" }

# 10. Enviar proposta (registrar interação)
POST /api/clientes/2/interacoes
{
  "tipoInteracao": "EMAIL",
  "descricao": "Proposta comercial enviada",
  "dadosExtras": { "valor": "299.90", "desconto": "10%" }
}

# 11. Mover para OPORTUNIDADE
PUT /api/clientes/2
{ "statusLead": "OPORTUNIDADE" }

# 12. Cliente fechou! Mover para CLIENTE
PUT /api/clientes/2
{ "statusLead": "CLIENTE", "observacoes": "Contrato assinado!" }

# 13. Enviar email promocional
POST /api/clientes/2/enviar-promocional
{
  "titulo": "Módulo Avançado Disponível",
  "descricao": "Conheça nosso novo módulo..."
}
```

### Cenário 2: Gestão de Cobranças

```bash
# 1. Cliente ativo (Marcos Souza - ID 9)
GET /api/clientes/9

# 2. Enviar lembrete de pagamento (3 dias antes)
POST /api/emails/lembrete-cobranca
{
  "destinatario": "marcos.souza@empresa2.com",
  "nomeCliente": "Marcos Souza",
  "numeroFatura": "INV-2024-123",
  "dataVencimento": "20/10/2024",
  "descricao": "Mensalidade Premium - Outubro",
  "valor": 299.90,
  "linkPagamento": "https://pay.nakacorp.com/123"
}

# 3. Checar email no MailHog: http://localhost:8025

# 4. Simular pagamento vencido (7 dias depois)
POST /api/emails/cobranca-vencida
{
  "destinatario": "marcos.souza@empresa2.com",
  "nomeCliente": "Marcos Souza",
  "numeroFatura": "INV-2024-123",
  "dataVencimento": "20/10/2024",
  "diasAtraso": 7,
  "descricao": "Mensalidade Premium - Outubro",
  "valorOriginal": 299.90,
  "taxaMulta": 2.0,
  "valorMulta": 5.99,
  "valorJuros": 2.10,
  "valorTotal": 307.99,
  "linkPagamento": "https://pay.nakacorp.com/123"
}

# 5. Cliente pagou! Enviar confirmação
POST /api/emails/confirmacao-pagamento
{
  "destinatario": "marcos.souza@empresa2.com",
  "nomeCliente": "Marcos Souza",
  "numeroFatura": "INV-2024-123",
  "dataPagamento": "27/10/2024",
  "metodoPagamento": "PIX",
  "descricao": "Mensalidade Premium - Outubro",
  "valor": 307.99,
  "codigoConfirmacao": "PAY-XYZ789",
  "linkRecibo": "https://receipts.nakacorp.com/xyz789"
}

# 6. Registrar pagamento no histórico
POST /api/clientes/9/interacoes
{
  "tipoInteracao": "NOTA_INTERNA",
  "descricao": "Pagamento recebido com atraso de 7 dias"
}
```

### Cenário 3: Dashboard e Análises

```bash
# 1. Ver estatísticas gerais
GET /api/dashboard/estatisticas

# 2. Ver leads mais recentes
GET /api/clientes/recentes?limit=5

# 3. Ver todos OPORTUNIDADES (próximos de fechar)
GET /api/clientes?status=OPORTUNIDADE

# 4. Ver produtos mais populares
GET /api/produtos

# 5. Ver clientes por origem
GET /api/clientes?origem=LANDING_PAGE
```

---

## 🐛 Troubleshooting

### Erro 401 - Unauthorized

**Causa**: Token expirado ou inválido.

**Solução**: Faça login novamente e copie o novo token.

### Erro 403 - Forbidden

**Causa**: Usuário sem permissão para a operação.

**Solução**: Use um usuário ADMIN para operações administrativas.

### Emails não aparecem no MailHog

**Causa**: MailHog não está rodando.

**Solução**:
```bash
docker-compose up -d crm-mailhog
```

### Erro de conexão com banco

**Causa**: PostgreSQL não está rodando.

**Solução**:
```bash
docker-compose up -d crm-postgres
```

---

## 📝 Notas Importantes

1. **Tokens JWT**: Expiram em 24h. Use refresh token para renovar.

2. **Emails Assíncronos**: Emails são enviados de forma assíncrona. Aguarde alguns segundos antes de checar o MailHog.

3. **Validações**: Campos obrigatórios são validados. Erros 400 indicam dados inválidos.

4. **CORS**: Frontend permitido nas portas 3000, 5173 e 4200 (React, Vite, Angular).

5. **Paginação**: Algumas rotas suportam `?page=0&size=20` para paginação.

6. **Busca**: Use `?termo=busca` para filtrar resultados.

---

## 🎯 Próximos Passos

1. ✅ Testar autenticação e obter token
2. ✅ Explorar clientes em diferentes estágios do funil
3. ✅ Registrar interações e mover leads pelo funil
4. ✅ Testar sistema de emails de cobrança
5. ✅ Visualizar estatísticas do dashboard
6. ✅ Integrar com frontend (ver FRONTEND_INTEGRATION.md)

---

**💡 Dica**: Use o Swagger UI (http://localhost:8080/api/swagger-ui.html) para testar as rotas de forma visual!
