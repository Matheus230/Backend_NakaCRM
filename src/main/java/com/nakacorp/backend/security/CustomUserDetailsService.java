package com.nakacorp.backend.security;

import com.nakacorp.backend.model.Usuario;
import com.nakacorp.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Autowired
    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));

        if (!usuario.getAtivo()) {
            throw new UsernameNotFoundException("Usuário inativo: " + email);
        }

        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + usuario.getTipoUsuario().name());

        return new User(
                usuario.getEmail(),
                usuario.getSenhaHash(),
                Collections.singletonList(authority)
        );
    }

    public UserDetails loadUserById(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + id));

        if (!usuario.getAtivo()) {
            throw new UsernameNotFoundException("Usuário inativo: " + id);
        }

        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + usuario.getTipoUsuario().name());

        return new User(
                usuario.getEmail(),
                usuario.getSenhaHash(),
                Collections.singletonList(authority)
        );
    }
}