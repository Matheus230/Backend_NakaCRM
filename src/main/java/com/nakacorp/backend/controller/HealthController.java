
package com.nakacorp.backend.controller;

import com.nakacorp.backend.dto.res.ApiResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller REST para Health Check e Status da Aplicação
 * <p>
 * Fornece endpoints públicos para verificação da saúde da aplicação,
 * incluindo status básico, verificação detalhada de dependências (banco de dados),
 * informações de versão e build, e métricas do sistema (memória, processadores).
 * Útil para monitoramento, troubleshooting e integração com ferramentas de observabilidade.
 * </p>
 *
 * @author Klleriston Andrade
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/health")
@Tag(name = "Health Check", description = "Endpoints para verificação de saúde da aplicação")
@PermitAll
public class HealthController {

    @Autowired(required = false)
    private BuildProperties buildProperties;

    @Autowired
    private DataSource dataSource;

    @GetMapping
    @Operation(summary = "Health check básico", description = "Verifica se a aplicação está funcionando")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("service", "CRM Backend API");

        return ResponseEntity.ok(ApiResponseDto.success("Serviço funcionando normalmente", health));
    }

    @GetMapping("/detailed")
    @Operation(summary = "Health check detalhado", description = "Verifica saúde detalhada da aplicação e dependências")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> detailedHealth() {
        Map<String, Object> health = new HashMap<>();
        Map<String, Object> checks = new HashMap<>();

        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("service", "CRM Backend API");

        if (buildProperties != null) {
            Map<String, Object> build = new HashMap<>();
            build.put("version", buildProperties.getVersion());
            build.put("name", buildProperties.getName());
            build.put("time", buildProperties.getTime());
            health.put("build", build);
        }

        Map<String, Object> database = new HashMap<>();
        try (Connection connection = dataSource.getConnection()) {
            database.put("status", "UP");
            database.put("database", connection.getMetaData().getDatabaseProductName());
            database.put("version", connection.getMetaData().getDatabaseProductVersion());
            database.put("url", connection.getMetaData().getURL().replaceAll("password=[^&]*", "password=***"));
        } catch (Exception e) {
            database.put("status", "DOWN");
            database.put("error", e.getMessage());
        }
        checks.put("database", database);

        Map<String, Object> system = new HashMap<>();
        Runtime runtime = Runtime.getRuntime();
        system.put("processors", runtime.availableProcessors());
        system.put("totalMemory", runtime.totalMemory());
        system.put("freeMemory", runtime.freeMemory());
        system.put("maxMemory", runtime.maxMemory());
        system.put("usedMemory", runtime.totalMemory() - runtime.freeMemory());
        checks.put("system", system);

        health.put("checks", checks);

        return ResponseEntity.ok(ApiResponseDto.success("Health check detalhado", health));
    }

    @GetMapping("/version")
    @Operation(summary = "Versão da aplicação", description = "Retorna informações de versão e build")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> version() {
        Map<String, Object> version = new HashMap<>();

        if (buildProperties != null) {
            version.put("version", buildProperties.getVersion());
            version.put("name", buildProperties.getName());
            version.put("group", buildProperties.getGroup());
            version.put("artifact", buildProperties.getArtifact());
            version.put("time", buildProperties.getTime());
        } else {
            version.put("version", "development");
            version.put("name", "CRM Backend");
            version.put("status", "Build properties not available");
        }

        version.put("java.version", System.getProperty("java.version"));
        version.put("spring.version", org.springframework.core.SpringVersion.getVersion());

        return ResponseEntity.ok(ApiResponseDto.success("Informações de versão", version));
    }

    @GetMapping("/database")
    @Operation(summary = "Status do banco de dados", description = "Verifica conectividade e status do banco")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> databaseHealth() {
        Map<String, Object> dbStatus = new HashMap<>();

        try (Connection connection = dataSource.getConnection()) {
            dbStatus.put("status", "UP");
            dbStatus.put("database", connection.getMetaData().getDatabaseProductName());
            dbStatus.put("version", connection.getMetaData().getDatabaseProductVersion());
            dbStatus.put("driver", connection.getMetaData().getDriverName());
            dbStatus.put("driverVersion", connection.getMetaData().getDriverVersion());
            dbStatus.put("url", connection.getMetaData().getURL().replaceAll("password=[^&]*", "password=***"));
            dbStatus.put("autoCommit", connection.getAutoCommit());
            dbStatus.put("readOnly", connection.isReadOnly());
            dbStatus.put("transactionIsolation", connection.getTransactionIsolation());

            boolean validConnection = connection.isValid(5);
            dbStatus.put("validConnection", validConnection);

            if (validConnection) {
                return ResponseEntity.ok(ApiResponseDto.success("Banco de dados conectado", dbStatus));
            } else {
                dbStatus.put("status", "DOWN");
                return ResponseEntity.status(503)
                        .body(ApiResponseDto.error("Conexão com banco inválida"));
            }

        } catch (Exception e) {
            dbStatus.put("status", "DOWN");
            dbStatus.put("error", e.getMessage());
            dbStatus.put("errorType", e.getClass().getSimpleName());

            return ResponseEntity.status(503)
                    .body(ApiResponseDto.error("Erro na conexão com banco de dados"));
        }
    }
}