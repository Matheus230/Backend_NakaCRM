package com.nakacorp.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuração do Swagger/OpenAPI para documentação da API.
 * <p>
 * Configura informações da API, esquema de segurança JWT e servidores.
 * A documentação fica disponível em /swagger-ui/index.html
 * </p>
 *
 * @author Klleriston Andrade
 * @version 1.0
 * @since 1.0
 */
@Configuration
public class SwaggerConfig {

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * Configuração do OpenAPI com informações da aplicação e segurança JWT.
     *
     * @return OpenAPI configurado
     */
    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearer-jwt";

        return new OpenAPI()
            .info(new Info()
                .title("NakaCRM API")
                .version("1.0.0")
                .description("""
                    ## Backend do Sistema CRM - Gestão de Leads e Clientes

                    ### Funcionalidades
                    - ✅ Gestão completa de Clientes/Leads
                    - ✅ Rastreamento de funil de vendas
                    - ✅ Timeline de interações
                    - ✅ Gestão de produtos
                    - ✅ Dashboard com métricas
                    - ✅ Autenticação JWT + OAuth2

                    ### Autenticação
                    A API usa JWT Bearer tokens. Para autenticar:
                    1. Faça login em `/api/auth/login`
                    2. Copie o token retornado
                    3. Clique no botão "Authorize" e cole o token

                    ### Códigos de Status
                    - **200**: Sucesso
                    - **201**: Criado
                    - **400**: Dados inválidos
                    - **401**: Não autenticado
                    - **403**: Sem permissão
                    - **404**: Não encontrado
                    - **409**: Conflito (ex: email duplicado)
                    - **500**: Erro interno
                    """)
                .contact(new Contact()
                    .name("NakaCorp")
                    .email("contato@nakacorp.com")
                    .url("https://nakacorp.com"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")))
            .servers(List.of(
                new Server()
                    .url(baseUrl + "/api")
                    .description("Servidor Local"),
                new Server()
                    .url("https://api.nakacorp.com/api")
                    .description("Servidor de Produção")
            ))
            .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
            .components(new Components()
                .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                    .name(securitySchemeName)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("Insira o token JWT recebido no login")));
    }
}
