package com.nakacorp.backend.service;

import com.nakacorp.backend.dto.res.LoginResponseDto;
import com.nakacorp.backend.dto.res.UsuarioResponseDto;
import com.nakacorp.backend.model.Usuario;
import com.nakacorp.backend.model.enums.TipoUsuario;
import com.nakacorp.backend.repository.UsuarioRepository;
import com.nakacorp.backend.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Transactional
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    private final Set<String> tokenBlacklist = ConcurrentHashMap.newKeySet();

    @Autowired
    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional(readOnly = true)
    public LoginResponseDto login(String email, String senha) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Credenciais inválidas"));

        if (!usuario.getAtivo()) {
            throw new IllegalArgumentException("Usuário inativo");
        }

        if (!passwordEncoder.matches(senha, usuario.getSenhaHash())) {
            throw new IllegalArgumentException("Credenciais inválidas");
        }

        String token = jwtTokenProvider.generateToken(usuario);
        long expiresIn = jwtTokenProvider.getExpirationTimeInSeconds();

        UsuarioResponseDto usuarioDto = UsuarioResponseDto.fromEntity(usuario);

        return LoginResponseDto.create(token, expiresIn, usuarioDto);
    }

    @Transactional(readOnly = true)
    public LoginResponseDto loginWithGoogle(String googleId) {
        Usuario usuario = usuarioRepository.findByGoogleId(googleId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        if (!usuario.getAtivo()) {
            throw new IllegalArgumentException("Usuário inativo");
        }

        String token = jwtTokenProvider.generateToken(usuario);
        long expiresIn = jwtTokenProvider.getExpirationTimeInSeconds();

        UsuarioResponseDto usuarioDto = UsuarioResponseDto.fromEntity(usuario);

        return LoginResponseDto.create(token, expiresIn, usuarioDto);
    }

    @Transactional(readOnly = true)
    public LoginResponseDto refreshToken(String oldToken) {
        if (tokenBlacklist.contains(oldToken)) {
            throw new IllegalArgumentException("Token inválido");
        }

        if (!jwtTokenProvider.validateToken(oldToken)) {
            throw new IllegalArgumentException("Token expirado ou inválido");
        }

        String email = jwtTokenProvider.getEmailFromToken(oldToken);
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        if (!usuario.getAtivo()) {
            throw new IllegalArgumentException("Usuário inativo");
        }

        tokenBlacklist.add(oldToken);

        String newToken = jwtTokenProvider.generateToken(usuario);
        long expiresIn = jwtTokenProvider.getExpirationTimeInSeconds();

        UsuarioResponseDto usuarioDto = UsuarioResponseDto.fromEntity(usuario);

        return LoginResponseDto.create(newToken, expiresIn, usuarioDto);
    }

    public void logout(String token) {
        if (jwtTokenProvider.validateToken(token)) {
            tokenBlacklist.add(token);
        }
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        String email = authentication.getName();
        return usuarioRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public boolean isTokenBlacklisted(String token) {
        return tokenBlacklist.contains(token);
    }

    @Transactional(readOnly = true)
    public boolean validateUserAccess(String email, TipoUsuario requiredRole) {
        return usuarioRepository.findByEmail(email)
                .map(usuario -> usuario.getAtivo() &&
                        (usuario.getTipoUsuario() == TipoUsuario.ADMIN ||
                                usuario.getTipoUsuario() == requiredRole))
                .orElse(false);
    }

    public void cleanupExpiredTokens() {
        tokenBlacklist.removeIf(token -> !jwtTokenProvider.validateToken(token));
    }
}