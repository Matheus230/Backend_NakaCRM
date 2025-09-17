package com.nakacorp.backend.repository;

import com.nakacorp.backend.model.ClienteInteresse;
import com.nakacorp.backend.model.enums.NivelInteresse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteInteresseRepository extends JpaRepository<ClienteInteresse, Long> {

    List<ClienteInteresse> findByClienteId(Long clienteId);

    List<ClienteInteresse> findByProdutoId(Long produtoId);

    Optional<ClienteInteresse> findByClienteIdAndProdutoId(Long clienteId, Long produtoId);

    List<ClienteInteresse> findByNivelInteresse(NivelInteresse nivelInteresse);

    @Query("SELECT ci FROM ClienteInteresse ci WHERE ci.cliente.id = :clienteId AND ci.nivelInteresse = :nivel")
    List<ClienteInteresse> findByClienteIdAndNivelInteresse(@Param("clienteId") Long clienteId, @Param("nivel") NivelInteresse nivel);

    @Query("SELECT COUNT(ci) FROM ClienteInteresse ci WHERE ci.produto.id = :produtoId")
    long countByProdutoId(@Param("produtoId") Long produtoId);

    @Query("SELECT COUNT(ci) FROM ClienteInteresse ci WHERE ci.produto.id = :produtoId AND ci.nivelInteresse = :nivel")
    long countByProdutoIdAndNivelInteresse(@Param("produtoId") Long produtoId, @Param("nivel") NivelInteresse nivel);
}
