package com.nakacorp.backend.controller;

import com.nakacorp.backend.dto.req.UsuarioRequestDto;
import com.nakacorp.backend.dto.req.UsuarioUpdateDto;
import com.nakacorp.backend.dto.res.ApiResponseDto;
import com.nakacorp.backend.dto.res.PageResponseDto;
import com.nakacorp.backend.dto.res.UsuarioResponseDto;
import com.nakacorp.backend.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para gerenciamento de Usuários do Sistema
 * <p>
 * Gerencia os usuários que têm acesso ao sistema CRM, incluindo
 * administradores e vendedores. Controla criação, atualização,
 * ativação/desativação e exclusão de contas de usuário.
 * Acesso restrito a administradores para operações sensíveis.
 * </p>
 *
 * @author Klleriston Andrade
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/usuarios")
@Tag(name = "Usuários", description = "Gestão de usuários do sistema")
@SecurityRequirement(name = "bearer-jwt")
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final UsuarioService usuarioService;

    /**
     * Construtor com injeção de dependência do serviço de usuários.
     *
     * @param usuarioService serviço de gerenciamento de usuários do sistema
     */
    @Autowired
    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    @Operation(summary = "Listar usuários com paginação", description = "Lista todos os usuários do sistema com paginação")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto<PageResponseDto<UsuarioResponseDto>>> findAll(
            @Parameter(description = "Número da página (inicia em 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Campo para ordenação") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Direção da ordenação") @RequestParam(defaultValue = "desc") String sortDirection) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<UsuarioResponseDto> result = usuarioService.findAll(pageable);
        PageResponseDto<UsuarioResponseDto> response = PageResponseDto.fromPage(result);

        return ResponseEntity.ok(ApiResponseDto.success(response));
    }

    @GetMapping("/ativos")
    @Operation(summary = "Listar usuários ativos", description = "Lista apenas os usuários ativos do sistema")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<ApiResponseDto<List<UsuarioResponseDto>>> findAtivos() {
        List<UsuarioResponseDto> usuarios = usuarioService.findAtivos();
        return ResponseEntity.ok(ApiResponseDto.success(usuarios));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuário por ID", description = "Retorna os dados de um usuário específico")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto<UsuarioResponseDto>> findById(
            @Parameter(description = "ID do usuário") @PathVariable Long id) {

        return usuarioService.findById(id)
                .map(usuario -> ResponseEntity.ok(ApiResponseDto.success(usuario)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponseDto.error("Usuário não encontrado")));
    }

    @PostMapping
    @Operation(summary = "Criar novo usuário", description = "Cria um novo usuário no sistema")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto<UsuarioResponseDto>> create(
            @Parameter(description = "Dados do usuário") @RequestBody @Valid UsuarioRequestDto request) {

        try {
            UsuarioResponseDto usuario = usuarioService.create(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponseDto.success("Usuário criado com sucesso", usuario));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseDto.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar usuário", description = "Atualiza os dados de um usuário existente")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto<UsuarioResponseDto>> update(
            @Parameter(description = "ID do usuário") @PathVariable Long id,
            @Parameter(description = "Novos dados do usuário") @RequestBody @Valid UsuarioUpdateDto request) {

        try {
            UsuarioResponseDto usuario = usuarioService.update(id, request);
            return ResponseEntity.ok(ApiResponseDto.success("Usuário atualizado com sucesso", usuario));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseDto.error(e.getMessage()));
        }
    }

    @PatchMapping("/{id}/toggle-ativo")
    @Operation(summary = "Alternar status ativo", description = "Ativa ou desativa um usuário")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto<UsuarioResponseDto>> toggleAtivo(
            @Parameter(description = "ID do usuário") @PathVariable Long id) {

        try {
            usuarioService.deactivate(id); // Método pode ser melhorado para toggle
            return ResponseEntity.ok(ApiResponseDto.success("Status do usuário alterado com sucesso", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseDto.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir usuário", description = "Remove um usuário do sistema (operação irreversível)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto<Void>> delete(
            @Parameter(description = "ID do usuário") @PathVariable Long id) {

        try {
            usuarioService.delete(id);
            return ResponseEntity.ok(ApiResponseDto.success("Usuário excluído com sucesso", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDto.error(e.getMessage()));
        }
    }
}