package com.nakacorp.backend.controller;

import com.nakacorp.backend.dto.req.LoginRequestDto;
import com.nakacorp.backend.dto.req.UsuarioRequestDto;
import com.nakacorp.backend.dto.res.ApiResponseDto;
import com.nakacorp.backend.dto.res.UsuarioResponseDto;
import com.nakacorp.backend.model.Usuario;
import com.nakacorp.backend.security.JwtTokenProvider;
import com.nakacorp.backend.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller responsável pela autenticação e registro de usuários.
 * <p>
 * Fornece endpoints para login, registro e renovação de tokens JWT.
 * Todos os endpoints são públicos (sem autenticação prévia).
 * </p>
 *
 * @author Klleriston Andrade
 * @version 1.0
 * @since 2025-01-01
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "Endpoints de autenticação e registro")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UsuarioService usuarioService;
    private final com.nakacorp.backend.security.LoginAttemptService loginAttemptService;

    /**
     * Construtor com injeção de dependências.
     *
     * @param authenticationManager gerenciador de autenticação do Spring Security
     * @param jwtTokenProvider      provedor de tokens JWT
     * @param usuarioService        serviço de gerenciamento de usuários
     * @param loginAttemptService   serviço de controle de tentativas de login
     */
    @Autowired
    public AuthController(
            AuthenticationManager authenticationManager,
            JwtTokenProvider jwtTokenProvider,
            UsuarioService usuarioService,
            com.nakacorp.backend.security.LoginAttemptService loginAttemptService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.usuarioService = usuarioService;
        this.loginAttemptService = loginAttemptService;
    }

    /**
     * Realiza o login de um usuário no sistema.
     * <p>
     * Valida as credenciais, verifica se o usuário está ativo e retorna tokens JWT
     * (access token e refresh token) junto com as informações do usuário.
     * </p>
     *
     * @param request credenciais de login (email e senha)
     * @return ResponseEntity contendo tokens JWT e dados do usuário
     * @throws BadCredentialsException se as credenciais forem inválidas
     */
    @PostMapping("/login")
    @Operation(summary = "Login de usuário", description = "Autentica um usuário e retorna um token JWT")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> login(
            @Parameter(description = "Credenciais de login")
            @RequestBody @Valid LoginRequestDto request) {

        try {
            if (loginAttemptService.isBlocked(request.email())) {
                long remainingTime = loginAttemptService.getRemainingBlockTime(request.email());
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(ApiResponseDto.error(
                            String.format("Conta temporariamente bloqueada. Tente novamente em %d minutos.",
                                remainingTime / 60)
                        ));
            }

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.senha())
            );

            Usuario usuario = usuarioService.findByEmail(request.email())
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

            if (!usuario.getAtivo()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponseDto.error("Usuário desativado. Entre em contato com o administrador."));
            }

            loginAttemptService.loginSucceeded(request.email());

            String accessToken = jwtTokenProvider.generateToken(usuario);
            String refreshToken = jwtTokenProvider.generateRefreshToken(usuario);

            Map<String, Object> response = new HashMap<>();
            response.put("accessToken", accessToken);
            response.put("refreshToken", refreshToken);
            response.put("tokenType", "Bearer");
            response.put("expiresIn", jwtTokenProvider.getExpirationTimeInSeconds());
            response.put("usuario", UsuarioResponseDto.fromEntity(usuario));

            return ResponseEntity.ok(ApiResponseDto.success("Login realizado com sucesso", response));

        } catch (BadCredentialsException e) {
            loginAttemptService.loginFailed(request.email());
            int attempts = loginAttemptService.getAttempts(request.email());

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponseDto.error(
                        String.format("Email ou senha inválidos. Tentativa %d de 5.", attempts)
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("Erro ao realizar login: " + e.getMessage()));
        }
    }

    /**
     * Registra um novo usuário no sistema.
     * <p>
     * Cria uma nova conta de usuário e retorna automaticamente os tokens JWT
     * para autenticação imediata após o registro.
     * </p>
     *
     * @param request dados do novo usuário
     * @return ResponseEntity contendo tokens JWT e dados do usuário criado
     * @throws IllegalArgumentException se o email já estiver cadastrado
     */
    @PostMapping("/register")
    @Operation(summary = "Registrar novo usuário", description = "Cria uma nova conta de usuário no sistema")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> register(
            @Parameter(description = "Dados do novo usuário")
            @RequestBody @Valid UsuarioRequestDto request) {

        try {
            UsuarioResponseDto usuarioDto = usuarioService.create(request);

            Usuario usuario = usuarioService.findByEmail(request.email())
                    .orElseThrow(() -> new IllegalArgumentException("Erro ao buscar usuário criado"));

            String accessToken = jwtTokenProvider.generateToken(usuario);
            String refreshToken = jwtTokenProvider.generateRefreshToken(usuario);

            Map<String, Object> response = new HashMap<>();
            response.put("accessToken", accessToken);
            response.put("refreshToken", refreshToken);
            response.put("tokenType", "Bearer");
            response.put("expiresIn", jwtTokenProvider.getExpirationTimeInSeconds());
            response.put("usuario", usuarioDto);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponseDto.success("Usuário registrado com sucesso", response));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseDto.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("Erro ao registrar usuário: " + e.getMessage()));
        }
    }

    /**
     * Renova o access token usando um refresh token válido.
     * <p>
     * Valida o refresh token fornecido e gera um novo access token,
     * permitindo que o usuário mantenha sua sessão sem fazer login novamente.
     * </p>
     *
     * @param request mapa contendo o refresh token
     * @return ResponseEntity contendo o novo access token
     * @throws IllegalArgumentException se o refresh token não for fornecido
     */
    @PostMapping("/refresh")
    @Operation(summary = "Renovar token", description = "Gera um novo access token usando o refresh token")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> refresh(
            @Parameter(description = "Refresh token")
            @RequestBody Map<String, String> request) {

        try {
            String refreshToken = request.get("refreshToken");

            if (refreshToken == null || refreshToken.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponseDto.error("Refresh token não fornecido"));
            }

            if (!jwtTokenProvider.validateToken(refreshToken) || !jwtTokenProvider.isRefreshToken(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponseDto.error("Refresh token inválido ou expirado"));
            }

            String email = jwtTokenProvider.getEmailFromToken(refreshToken);
            Usuario usuario = usuarioService.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

            if (!usuario.getAtivo()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponseDto.error("Usuário desativado"));
            }

            String newAccessToken = jwtTokenProvider.generateToken(usuario);

            Map<String, Object> response = new HashMap<>();
            response.put("accessToken", newAccessToken);
            response.put("tokenType", "Bearer");
            response.put("expiresIn", jwtTokenProvider.getExpirationTimeInSeconds());

            return ResponseEntity.ok(ApiResponseDto.success("Token renovado com sucesso", response));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("Erro ao renovar token: " + e.getMessage()));
        }
    }

    /**
     * Realiza o logout do usuário no sistema.
     * <p>
     * Limpa o contexto de segurança do Spring Security. Como a aplicação utiliza
     * JWT stateless, o token não é invalidado no servidor (isso deve ser feito no cliente
     * removendo o token do armazenamento local). Este endpoint serve principalmente
     * para limpar a sessão no lado do servidor e pode ser usado para logging/auditoria.
     * </p>
     *
     * @return ResponseEntity com mensagem de sucesso
     */
    @PostMapping("/logout")
    @Operation(summary = "Logout de usuário", description = "Realiza o logout do usuário e limpa o contexto de segurança")
    public ResponseEntity<ApiResponseDto<Void>> logout() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null) {
                SecurityContextHolder.clearContext();
            }

            return ResponseEntity.ok(ApiResponseDto.success("Logout realizado com sucesso", null));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("Erro ao realizar logout: " + e.getMessage()));
        }
    }
}