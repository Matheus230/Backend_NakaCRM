package com.nakacorp.backend.service;

import com.nakacorp.backend.dto.req.UsuarioRequestDto;
import com.nakacorp.backend.dto.req.UsuarioUpdateDto;
import com.nakacorp.backend.dto.res.UsuarioResponseDto;
import com.nakacorp.backend.model.Usuario;
import com.nakacorp.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public Page<UsuarioResponseDto> findAll(Pageable pageable) {
        return usuarioRepository.findAll(pageable)
                .map(UsuarioResponseDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public Optional<UsuarioResponseDto> findById(Long id) {
        return usuarioRepository.findById(id)
                .map(UsuarioResponseDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponseDto> findAtivos() {
        return usuarioRepository.findByAtivoTrue()
                .stream()
                .map(UsuarioResponseDto::fromEntity)
                .toList();
    }

    public UsuarioResponseDto create(UsuarioRequestDto request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email já cadastrado: " + request.email());
        }

        Usuario usuario = new Usuario(
                request.nome(),
                request.email(),
                passwordEncoder.encode(request.senha()),
                request.tipoUsuario()
        );
        usuario.setGoogleId(request.googleId());
        usuario.setAtivo(request.ativo());

        Usuario saved = usuarioRepository.save(usuario);
        return UsuarioResponseDto.fromEntity(saved);
    }

    public UsuarioResponseDto update(Long id, UsuarioUpdateDto request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + id));

        if (request.nome() != null) usuario.setNome(request.nome());
        if (request.email() != null) {
            if (!request.email().equals(usuario.getEmail()) && usuarioRepository.existsByEmail(request.email())) {
                throw new IllegalArgumentException("Email já cadastrado: " + request.email());
            }
            usuario.setEmail(request.email());
        }
        if (request.tipoUsuario() != null) usuario.setTipoUsuario(request.tipoUsuario());
        if (request.googleId() != null) usuario.setGoogleId(request.googleId());
        if (request.ativo() != null) usuario.setAtivo(request.ativo());

        Usuario updated = usuarioRepository.save(usuario);
        return UsuarioResponseDto.fromEntity(updated);
    }

    public void delete(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new IllegalArgumentException("Usuário não encontrado: " + id);
        }
        usuarioRepository.deleteById(id);
    }

    public void deactivate(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + id));
        usuario.setAtivo(false);
        usuarioRepository.save(usuario);
    }
}