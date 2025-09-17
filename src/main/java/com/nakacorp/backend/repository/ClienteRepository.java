package com.nakacorp.backend.repository;

import com.nakacorp.backend.model.Cliente;
import com.nakacorp.backend.model.enums.OrigemLead;
import com.nakacorp.backend.model.enums.StatusLead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByEmail(String email);

    List<Cliente> findByStatusLead(StatusLead statusLead);

    List<Cliente> findByOrigemLead(OrigemLead origemLead);

    @Query("SELECT c FROM Cliente c WHERE c.statusLead IN :status")
    List<Cliente> findByStatusLeadIn(@Param("status") List<StatusLead> status);

    @Query("SELECT c FROM Cliente c WHERE c.nome ILIKE %:nome%")
    List<Cliente> findByNomeContainingIgnoreCase(@Param("nome") String nome);

    @Query("SELECT c FROM Cliente c WHERE c.empresa ILIKE %:empresa%")
    List<Cliente> findByEmpresaContainingIgnoreCase(@Param("empresa") String empresa);

    @Query("SELECT c FROM Cliente c WHERE c.createdAt BETWEEN :inicio AND :fim")
    List<Cliente> findByCreatedAtBetween(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("SELECT COUNT(c) FROM Cliente c WHERE c.statusLead = :status")
    long countByStatusLead(@Param("status") StatusLead status);

    boolean existsByEmail(String email);
}