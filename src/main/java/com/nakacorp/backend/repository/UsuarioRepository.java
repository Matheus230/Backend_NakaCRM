package com.nakacorp.backend.repository;

import com.nakacorp.backend.model.Usuario;
import com.nakacorp.backend.model.enums.TipoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByGoogleId(String googleId);

    List<Usuario> findByAtivoTrue();

    List<Usuario> findByTipoUsuario(TipoUsuario tipoUsuario);

    @Query("SELECT u FROM Usuario u WHERE u.ativo = :ativo AND u.tipoUsuario = :tipo")
    List<Usuario> findByAtivoAndTipoUsuario(@Param("ativo") Boolean ativo, @Param("tipo") TipoUsuario tipo);

    boolean existsByEmail(String email);
}