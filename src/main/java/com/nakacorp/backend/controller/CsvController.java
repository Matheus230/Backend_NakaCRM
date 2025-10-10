package com.nakacorp.backend.controller;

import com.nakacorp.backend.dto.res.ApiResponseDto;
import com.nakacorp.backend.service.CsvExportService;
import com.nakacorp.backend.service.CsvImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller REST para importação e exportação de dados em CSV.
 *
 * Permite exportar e importar dados de Clientes, Produtos e Interações
 * em formato CSV para backup, migração ou análise externa.
 *
 * Permissões:
 * - Exportação: ADMIN ou VENDEDOR
 * - Importação: Apenas ADMIN
 *
 * @author Klleriston Andrade
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/csv")
@Tag(name = "CSV Import/Export", description = "Importação e exportação de dados em CSV")
@SecurityRequirement(name = "bearer-jwt")
public class CsvController {

    private final CsvExportService exportService;
    private final CsvImportService importService;

    private static final DateTimeFormatter FILENAME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    @Autowired
    public CsvController(CsvExportService exportService, CsvImportService importService) {
        this.exportService = exportService;
        this.importService = importService;
    }

    /**
     * Exporta todos os clientes para CSV.
     *
     * @return arquivo CSV com todos os clientes
     */
    @GetMapping("/export/clientes")
    @Operation(summary = "Exportar clientes", description = "Exporta todos os clientes para arquivo CSV")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<byte[]> exportarClientes() {
        try {
            byte[] csvBytes = exportService.exportarClientes();
            String filename = String.format("clientes_%s.csv",
                LocalDateTime.now().format(FILENAME_FORMATTER));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(new MediaType("text", "csv", java.nio.charset.StandardCharsets.UTF_8));
            headers.setContentDispositionFormData("attachment", filename);
            headers.setCacheControl("no-cache, no-store, must-revalidate");

            return ResponseEntity.ok()
                .headers(headers)
                .body(csvBytes);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Exporta todos os produtos para CSV.
     *
     * @return arquivo CSV com todos os produtos
     */
    @GetMapping("/export/produtos")
    @Operation(summary = "Exportar produtos", description = "Exporta todos os produtos para arquivo CSV")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<byte[]> exportarProdutos() {
        try {
            byte[] csvBytes = exportService.exportarProdutos();
            String filename = String.format("produtos_%s.csv",
                LocalDateTime.now().format(FILENAME_FORMATTER));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(new MediaType("text", "csv", java.nio.charset.StandardCharsets.UTF_8));
            headers.setContentDispositionFormData("attachment", filename);
            headers.setCacheControl("no-cache, no-store, must-revalidate");

            return ResponseEntity.ok()
                .headers(headers)
                .body(csvBytes);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Exporta todas as interações para CSV.
     *
     * @return arquivo CSV com todas as interações
     */
    @GetMapping("/export/interacoes")
    @Operation(summary = "Exportar interações", description = "Exporta todas as interações para arquivo CSV")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<byte[]> exportarInteracoes() {
        try {
            byte[] csvBytes = exportService.exportarInteracoes();
            String filename = String.format("interacoes_%s.csv",
                LocalDateTime.now().format(FILENAME_FORMATTER));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(new MediaType("text", "csv", java.nio.charset.StandardCharsets.UTF_8));
            headers.setContentDispositionFormData("attachment", filename);
            headers.setCacheControl("no-cache, no-store, must-revalidate");

            return ResponseEntity.ok()
                .headers(headers)
                .body(csvBytes);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Exporta todos os dados do sistema (clientes + produtos + interações).
     *
     * @return arquivo CSV combinado
     */
    @GetMapping("/export/completo")
    @Operation(summary = "Exportar backup completo", description = "Exporta todos os dados do sistema em um único arquivo CSV")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportarTodosDados() {
        try {
            byte[] csvBytes = exportService.exportarTodosDados();
            String filename = String.format("backup_completo_%s.csv",
                LocalDateTime.now().format(FILENAME_FORMATTER));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(new MediaType("text", "csv", java.nio.charset.StandardCharsets.UTF_8));
            headers.setContentDispositionFormData("attachment", filename);
            headers.setCacheControl("no-cache, no-store, must-revalidate");

            return ResponseEntity.ok()
                .headers(headers)
                .body(csvBytes);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Importa clientes a partir de um arquivo CSV.
     *
     * Formato esperado do CSV:
     * Nome, Email, Telefone, Empresa, Cargo, Cidade, Estado, CEP, Endereco, Origem Lead, Status Lead, Observacoes
     *
     * @param file arquivo CSV com clientes
     * @return resultado da importação
     */
    @PostMapping(value = "/import/clientes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Importar clientes", description = "Importa clientes em lote a partir de arquivo CSV")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> importarClientes(
            @Parameter(description = "Arquivo CSV com clientes")
            @RequestParam("file") MultipartFile file) {

        try {
            // Validar arquivo
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("Arquivo vazio"));
            }

            if (!file.getOriginalFilename().toLowerCase().endsWith(".csv")) {
                return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("Apenas arquivos CSV são permitidos"));
            }

            CsvImportService.ImportResult result = importService.importarClientes(file);

            Map<String, Object> response = new HashMap<>();
            response.put("total", result.getTotal());
            response.put("sucessos", result.getSucessos());
            response.put("erros", result.getErros());
            response.put("mensagensErro", result.getMensagensErro());

            if (result.getErros() > 0) {
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .body(ApiResponseDto.success(
                        String.format("Importação concluída com %d sucessos e %d erros",
                            result.getSucessos(), result.getErros()),
                        response
                    ));
            }

            return ResponseEntity.ok(ApiResponseDto.success(
                String.format("%d clientes importados com sucesso", result.getSucessos()),
                response
            ));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.error("Erro ao processar arquivo: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.error("Erro inesperado: " + e.getMessage()));
        }
    }

    /**
     * Importa produtos a partir de um arquivo CSV.
     *
     * Formato esperado do CSV:
     * Nome, Descricao, Categoria, Preco, Tipo Cobranca, Tipo Pagamento, Ativo
     *
     * @param file arquivo CSV com produtos
     * @return resultado da importação
     */
    @PostMapping(value = "/import/produtos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Importar produtos", description = "Importa produtos em lote a partir de arquivo CSV")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> importarProdutos(
            @Parameter(description = "Arquivo CSV com produtos")
            @RequestParam("file") MultipartFile file) {

        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("Arquivo vazio"));
            }

            if (!file.getOriginalFilename().toLowerCase().endsWith(".csv")) {
                return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("Apenas arquivos CSV são permitidos"));
            }

            CsvImportService.ImportResult result = importService.importarProdutos(file);

            Map<String, Object> response = new HashMap<>();
            response.put("total", result.getTotal());
            response.put("sucessos", result.getSucessos());
            response.put("erros", result.getErros());
            response.put("mensagensErro", result.getMensagensErro());

            if (result.getErros() > 0) {
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .body(ApiResponseDto.success(
                        String.format("Importação concluída com %d sucessos e %d erros",
                            result.getSucessos(), result.getErros()),
                        response
                    ));
            }

            return ResponseEntity.ok(ApiResponseDto.success(
                String.format("%d produtos importados com sucesso", result.getSucessos()),
                response
            ));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.error("Erro ao processar arquivo: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.error("Erro inesperado: " + e.getMessage()));
        }
    }

    /**
     * Retorna template CSV para importação de clientes.
     *
     * @return arquivo CSV template
     */
    @GetMapping("/template/clientes")
    @Operation(summary = "Download template clientes", description = "Baixa arquivo CSV template para importação de clientes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> downloadTemplateClientes() {
        String template = "Nome,Email,Telefone,Empresa,Cargo,Cidade,Estado,CEP,Endereco,Origem Lead,Status Lead,Observacoes\n" +
                         "João Silva,joao@example.com,(11) 98765-4321,Empresa XYZ,Gerente,São Paulo,SP,01234-567,Rua Exemplo 123,LANDING_PAGE,NOVO,Cliente interessado em produto A";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "csv", java.nio.charset.StandardCharsets.UTF_8));
        headers.setContentDispositionFormData("attachment", "template_clientes.csv");

        return ResponseEntity.ok()
            .headers(headers)
            .body(template.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    /**
     * Retorna template CSV para importação de produtos.
     *
     * @return arquivo CSV template
     */
    @GetMapping("/template/produtos")
    @Operation(summary = "Download template produtos", description = "Baixa arquivo CSV template para importação de produtos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> downloadTemplateProdutos() {
        String template = "Nome,Descricao,Categoria,Preco,Tipo Cobranca,Tipo Pagamento,Ativo\n" +
                         "Plano Premium,Acesso completo a todas funcionalidades,Software,199.90,MENSAL,CARTAO,true";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "csv", java.nio.charset.StandardCharsets.UTF_8));
        headers.setContentDispositionFormData("attachment", "template_produtos.csv");

        return ResponseEntity.ok()
            .headers(headers)
            .body(template.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
}