# 🚀 Backend NakaCRM

Sistema CRM (Customer Relationship Management) completo desenvolvido com Spring Boot 3.3.5 e Java 21, focado em gestão de leads, pipeline de vendas e relacionamento com clientes.

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

---

## 📋 Índice

- [Funcionalidades](#-funcionalidades)
- [Arquitetura](#-arquitetura)
- [Tecnologias](#-tecnologias)
- [Pré-requisitos](#-pré-requisitos)
- [Instalação](#-instalação)
- [Configuração](#-configuração)
- [Execução](#-execução)
- [API Documentation](#-api-documentation)
- [Banco de Dados](#-banco-de-dados)
- [Segurança](#-segurança)
- [Performance](#-performance)
- [Estrutura do Projeto](#-estrutura-do-projeto)
- [Testes](#-testes)
- [Deploy](#-deploy)
- [Contribuindo](#-contribuindo)

---

## ✨ Funcionalidades

### Gestão de Clientes/Leads
- ✅ CRUD completo de clientes
- ✅ Funil de vendas: NOVO → CONTATADO → QUALIFICADO → OPORTUNIDADE → CLIENTE/PERDIDO
- ✅ Rastreamento de origem (Google Forms, Landing Page, Manual)
- ✅ Timeline completa de interações
- ✅ Filtros avançados e paginação

### Rastreamento UTM
- ✅ Captura de parâmetros UTM (source, medium, campaign)
- ✅ User agent tracking
- ✅ Analytics de origem de leads

### Gestão de Produtos
- ✅ Catálogo de produtos/serviços
- ✅ Tipos de cobrança (Único, Mensal, Anual)
- ✅ Formas de pagamento (Cartão, PIX, Boleto)
- ✅ Relacionamento M:N com clientes (interesses)

### Interações
- ✅ Timeline de comunicações (Email, Telefone, WhatsApp)
- ✅ Registro automático de eventos
- ✅ Notas internas
- ✅ Metadados JSONB para flexibilidade

### Dashboard & Analytics
- ✅ Métricas de conversão
- ✅ Funil de vendas visual
- ✅ Taxa de conversão por origem
- ✅ Leads para follow-up
- ✅ Hot leads do dia

### Autenticação & Autorização
- ✅ JWT com refresh token
- ✅ OAuth2 (Google)
- ✅ RBAC (Admin, Vendedor)
- ✅ Sessões stateless

### Email Marketing
- ✅ Envio assíncrono de emails
- ✅ Templates HTML responsivos
- ✅ Boas-vindas, follow-up, promocionais
- ✅ Integração com MailHog (dev)

---

## 🏗️ Arquitetura

### Padrões Utilizados
- **Layered Architecture**: Controller → Service → Repository
- **DTO Pattern**: Separação entre entidades e transferência de dados
- **Repository Pattern**: Abstração de acesso a dados
- **Dependency Injection**: Inversão de controle com Spring
- **Builder Pattern**: Lombok para construção de objetos
- **Exception Handling**: Hierarquia de exceções customizadas

### Princípios SOLID
- ✅ Single Responsibility Principle
- ✅ Open/Closed Principle
- ✅ Liskov Substitution Principle
- ✅ Interface Segregation Principle
- ✅ Dependency Inversion Principle

---

## 🛠️ Tecnologias

### Core
- **Java 21** - LTS version com Virtual Threads e Pattern Matching
- **Spring Boot 3.3.5** - Framework principal
- **Spring Data JPA** - Persistência de dados
- **Spring Security** - Autenticação e autorização
- **Spring Cache** - Abstração de cache

### Database
- **PostgreSQL 16** - Banco relacional
- **Flyway** - Versionamento de schema
- **HikariCP** - Connection pooling

### Security
- **JWT (jjwt 0.12.6)** - Tokens de autenticação
- **BCrypt** - Hash de senhas
- **OAuth2 Client** - Integração com Google

### Performance
- **Caffeine 3.1.8** - Cache in-memory de alta performance
- **Query Optimization** - Fetch joins e queries nativas

### Utilities
- **Lombok 1.18.30** - Redução de boilerplate
- **Caelum Stella 2.1.6** - Validação CPF/CNPJ
- **Jackson** - Serialização JSON
- **Apache Commons Lang** - Utilidades

### Documentation
- **SpringDoc OpenAPI 3** - Documentação interativa Swagger
- **JavaDoc** - Documentação de código

### DevOps
- **Docker Compose** - Orquestração de containers
- **Maven** - Build e gerenciamento de dependências
- **Actuator** - Monitoramento e health checks

### Testing
- **JUnit 5** - Testes unitários
- **Testcontainers** - Testes de integração
- **Mockito** - Mocking
- **H2** - Banco em memória para testes

---

## 📦 Pré-requisitos

- **Java 21+** ([Download](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html))
- **Maven 3.8+** ([Download](https://maven.apache.org/download.cgi))
- **Docker & Docker Compose** ([Download](https://www.docker.com/products/docker-desktop/))
- **PostgreSQL 16** (opcional - pode usar Docker)

### Verificando instalação

```bash
java -version   # Java 21+
mvn -version    # Maven 3.8+
docker -v       # Docker 20+
```

---

## 🚀 Instalação

### 1. Clone o repositório

```bash
git clone https://github.com/nakacorp/backend-nakacrm.git
cd backend-nakacrm
```

### 2. Configure as variáveis de ambiente

```bash
# Copie o arquivo de exemplo
cp .env.example .env

# Edite o arquivo .env com suas configurações
nano .env
```

**Exemplo de .env:**

```properties
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=crm_db
DB_USER=crm_user
DB_PASSWORD=crm_password

# JWT (CRÍTICO: gere uma chave forte!)
JWT_SECRET=$(openssl rand -hex 64)
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000

# OAuth2 Google
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret

# Email
MAIL_HOST=localhost
MAIL_PORT=1025

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173
```

### 3. Inicie o banco de dados

```bash
# Com Docker Compose (recomendado)
docker-compose up -d crm-postgres

# Ou manualmente com PostgreSQL local
createdb crm_db
createuser crm_user
```

### 4. Compile o projeto

```bash
mvn clean install
```

---

## ⚙️ Configuração

### Perfis de Ambiente

A aplicação suporta diferentes perfis:

- **dev** (padrão): Desenvolvimento local
- **prod**: Produção
- **docker**: Execução em container

Ative um perfil específico:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### Configuração do Flyway

As migrations são aplicadas automaticamente na inicialização:

```
src/main/resources/db/migration/
├── V1__initial_schema.sql
├── V2__add_indexes.sql
├── V3__add_triggers.sql
└── V4__add_views.sql
```

### Configuração de Cache

Caches configurados no `CacheConfig.java`:

| Cache | TTL | Max Size | Uso |
|-------|-----|----------|-----|
| produtos | 1h | 500 | Listagem de produtos |
| clientes | 15min | 1000 | Dados de clientes |
| dashboard-stats | 5min | 100 | Métricas do dashboard |

---

## 🏃 Execução

### Desenvolvimento Local

```bash
# Inicia todos os serviços (DB, MailHog, PgAdmin)
./dev.sh

# Ou manualmente:
docker-compose up -d
mvn spring-boot:run
```

A aplicação estará disponível em:
- **API**: http://localhost:8080/api
- **Swagger**: http://localhost:8080/api/swagger-ui/index.html
- **Actuator**: http://localhost:8080/api/actuator/health
- **MailHog**: http://localhost:8025 (captura de emails)
- **PgAdmin**: http://localhost:5050 (gerenciamento DB)

### Modo Produção

```bash
# Build
mvn clean package -DskipTests

# Execute o JAR
java -jar target/backend.jar \
  --spring.profiles.active=prod \
  -Djwt.secret=$JWT_SECRET \
  -Dspring.datasource.url=$DATABASE_URL
```

### Docker

```bash
# Build da imagem
docker build -t nakacrm-backend .

# Execute
docker run -p 8080:8080 \
  -e JWT_SECRET=$JWT_SECRET \
  -e DB_HOST=postgres \
  nakacrm-backend
```

---

## 📚 API Documentation

### Swagger UI

Acesse a documentação interativa em:
```
http://localhost:8080/api/swagger-ui/index.html
```

### Autenticação

#### 1. Login

```bash
POST /api/auth/login
Content-Type: application/json

{
  "email": "admin@empresa.com",
  "senha": "admin123"
}

# Resposta:
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "tipo": "Bearer",
  "expiresIn": 86400000
}
```

#### 2. Usar o Token

```bash
GET /api/clientes
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

### Endpoints Principais

| Método | Endpoint | Descrição | Auth |
|--------|----------|-----------|------|
| **Auth** ||||
| POST | `/auth/login` | Login | ❌ |
| POST | `/auth/register` | Registro | ❌ |
| POST | `/auth/refresh` | Refresh token | ✅ |
| **Clientes** ||||
| GET | `/clientes` | Listar clientes | ✅ |
| GET | `/clientes/{id}` | Buscar por ID | ✅ |
| POST | `/clientes` | Criar cliente | ✅ |
| PUT | `/clientes/{id}` | Atualizar cliente | ✅ |
| PATCH | `/clientes/{id}/status` | Atualizar status | ✅ |
| DELETE | `/clientes/{id}` | Deletar cliente | ✅ ADMIN |
| **Produtos** ||||
| GET | `/produtos` | Listar produtos | ✅ |
| POST | `/produtos` | Criar produto | ✅ |
| **Dashboard** ||||
| GET | `/dashboard/stats` | Estatísticas gerais | ✅ |
| GET | `/dashboard/conversion-rate` | Taxa de conversão | ✅ |
| GET | `/dashboard/leads/hot-today` | Leads quentes | ✅ |

### Exemplos de Uso

#### Criar Cliente

```bash
curl -X POST http://localhost:8080/api/clientes \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "João Silva",
    "email": "joao@example.com",
    "telefone": "(11) 98765-4321",
    "empresa": "Empresa XYZ",
    "origemLead": "LANDING_PAGE",
    "statusLead": "NOVO"
  }'
```

#### Filtrar Clientes

```bash
# Com paginação e ordenação
curl -X GET "http://localhost:8080/api/clientes?page=0&size=10&sortBy=createdAt&sortDirection=DESC" \
  -H "Authorization: Bearer $TOKEN"
```

---

## 🗄️ Banco de Dados

### Modelo Relacional

```
tb_usuario (1) ──────< (N) tb_interacao_cliente
                              │
                              │
tb_produto (N) ──<>── (N) tb_cliente_interesse
                              │
                              │
tb_cliente (1) ───────< (N) tb_cliente_interesse
     │
     │
     └────────< (1) tb_lead_origem
     │
     └────────< (N) tb_interacao_cliente
```

### Entidades

#### Cliente
- **PK**: id_cliente (BIGINT)
- **Campos**: nome, email, telefone, empresa, cargo, cidade, estado, CEP
- **Enums**: origem_lead, status_lead
- **Timestamps**: data_primeiro_contato, data_ultima_interacao, created_at, updated_at

#### Produto
- **PK**: id_produto (BIGINT)
- **Campos**: nome, descricao, categoria, preco, ativo
- **Enums**: tipo_cobranca, tipo_pagamento

#### Usuario
- **PK**: id_usuario (BIGINT)
- **Campos**: nome, email, senha_hash, ativo, google_id
- **Enum**: tipo_usuario (ADMIN, VENDEDOR)

### Migrations

Execute migrations manualmente:

```bash
mvn flyway:migrate
```

Criar nova migration:

```bash
# Crie arquivo em src/main/resources/db/migration/
# Padrão: V{version}__{description}.sql
# Exemplo: V5__add_cpf_column.sql
```

---

## 🔒 Segurança

### Boas Práticas Implementadas

✅ **Senhas com BCrypt** (cost factor: 12)
✅ **JWT com assinatura HMAC-SHA256**
✅ **Secrets externalizados** (variáveis de ambiente)
✅ **CORS configurável** por ambiente
✅ **HTTPS em produção** (recomendado)
✅ **Rate limiting** (TODO)
✅ **SQL Injection** prevenido (JPA/Hibernate)
✅ **XSS** mitigado (JSON encoding automático)

### Tokens JWT

- **Access Token**: 24 horas (86400000ms)
- **Refresh Token**: 7 dias (604800000ms)
- **Algoritmo**: HS256
- **Claims**: userId, email, roles

### Roles e Permissões

| Endpoint | ADMIN | VENDEDOR |
|----------|-------|----------|
| GET /clientes | ✅ | ✅ |
| POST /clientes | ✅ | ✅ |
| DELETE /clientes | ✅ | ❌ |
| GET /actuator | ✅ | ❌ |

---

## ⚡ Performance

### Otimizações Implementadas

#### 1. **Cache com Caffeine**
- Produtos em cache por 1 hora
- Stats do dashboard por 5 minutos
- Evict automático em operações de escrita

#### 2. **Query Optimization**
```java
// Fetch Joins para evitar N+1
@Query("SELECT c FROM Cliente c " +
       "LEFT JOIN FETCH c.leadOrigem " +
       "LEFT JOIN FETCH c.interesses " +
       "WHERE c.id = :id")
Optional<Cliente> findByIdWithRelations(@Param("id") Long id);
```

#### 3. **Paginação**
Todos os endpoints de listagem suportam paginação:
```
GET /clientes?page=0&size=20
```

#### 4. **Índices de Banco**
- status_lead, origem_lead (clientes)
- categoria, ativo (produtos)
- created_at para ordenação temporal

#### 5. **Connection Pooling (HikariCP)**
```properties
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
```

### Métricas

Disponíveis via Actuator:

```bash
# Métricas de cache
curl http://localhost:8080/api/actuator/metrics/cache.gets

# Health check
curl http://localhost:8080/api/actuator/health
```

---

## 📁 Estrutura do Projeto

```
src/main/java/com/nakacorp/backend/
├── config/                 # Configurações Spring
│   ├── BeanConfig.java
│   ├── CacheConfig.java
│   └── SwaggerConfig.java
├── controller/             # REST Controllers
│   ├── ClienteController.java
│   ├── DashboardController.java
│   └── GlobalExceptionHandler.java
├── dto/
│   ├── req/               # Request DTOs
│   └── res/               # Response DTOs
├── exception/             # Exceções customizadas
│   ├── BusinessException.java
│   ├── ResourceNotFoundException.java
│   └── DuplicateResourceException.java
├── model/                 # Entidades JPA
│   ├── Cliente.java
│   ├── Usuario.java
│   ├── Produto.java
│   └── enums/
├── repository/            # JPA Repositories
│   ├── ClienteRepository.java
│   └── ProdutoRepository.java
├── security/              # Segurança JWT
│   ├── JwtTokenProvider.java
│   ├── SecurityConfig.java
│   └── JwtAuthenticationFilter.java
├── service/               # Lógica de negócio
│   ├── ClienteService.java
│   ├── DashboardService.java
│   └── EmailService.java
└── validation/            # Validadores customizados
    ├── CPFValidator.java
    └── CNPJValidator.java

src/main/resources/
├── db/migration/          # Flyway migrations
│   ├── V1__initial_schema.sql
│   ├── V2__add_indexes.sql
│   └── V3__add_triggers.sql
├── application.properties
└── application-dev.yml
```

---

## 🧪 Testes

### Executar Testes

```bash
# Todos os testes
mvn test

# Apenas testes unitários
mvn test -Dtest="*Test"

# Apenas testes de integração
mvn test -Dtest="*IT"

# Com coverage
mvn test jacoco:report
```

### Coverage Report

Relatório gerado em: `target/site/jacoco/index.html`

Meta: **80%+ de cobertura**

---

## 🚢 Deploy

### Heroku

```bash
# Criar app
heroku create nakacrm-backend

# Adicionar PostgreSQL
heroku addons:create heroku-postgresql:essential-0

# Configurar variáveis
heroku config:set JWT_SECRET=$(openssl rand -hex 64)

# Deploy
git push heroku main
```

### AWS (Elastic Beanstalk)

```bash
# Criar environment
eb init -p java-21 nakacrm-backend

# Deploy
eb create nakacrm-backend-env
eb deploy
```

### Docker Production

```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/backend.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## 📝 Contribuindo

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

### Padrões de Código

- ✅ Seguir convenções Java
- ✅ Adicionar JavaDoc em métodos públicos
- ✅ Escrever testes para novas funcionalidades
- ✅ Manter cobertura acima de 80%
- ✅ Usar Lombok para reduzir boilerplate

---

## 📄 Licença


---

## 👥 Autores

**NakaCorp** - *Desenvolvimento Inicial*

---

## 🙏 Agradecimentos

- Spring Boot Team
- PostgreSQL Community
- Caelum Stella (validação CPF/CNPJ)
- Todos os contribuidores!

---

## 📞 Suporte

- 📧 Email: *********
- 📝 Issues: [GitHub Issues](https://github.com/nakacorp/backend-nakacrm/issues)
- 📖 Wiki: [GitHub Wiki](https://github.com/nakacorp/backend-nakacrm/wiki)

---
