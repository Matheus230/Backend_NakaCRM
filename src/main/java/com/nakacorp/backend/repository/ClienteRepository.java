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

    /**
     * Busca cliente por ID com fetch join otimizado para evitar N+1 queries.
     *
     * @param id ID do cliente
     * @return Optional com cliente e seus relacionamentos carregados
     */
    @Query("SELECT c FROM Cliente c " +
           "LEFT JOIN FETCH c.leadOrigem " +
           "LEFT JOIN FETCH c.interesses " +
           "WHERE c.id = :id")
    Optional<Cliente> findByIdWithRelations(@Param("id") Long id);

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

    /**
     * Query otimizada para buscar leads que precisam de follow-up.
     * Evita carregar todos os clientes na memória.
     *
     * @param seteDiasAtras Data de corte (7 dias atrás)
     * @param statusAtivos Lista de status considerados ativos
     * @return Lista de clientes que precisam de acompanhamento
     */
    @Query("SELECT c FROM Cliente c WHERE " +
           "(c.dataUltimaInteracao IS NULL OR c.dataUltimaInteracao < :seteDiasAtras) " +
           "AND c.statusLead IN :statusAtivos")
    List<Cliente> findLeadsToFollow(@Param("seteDiasAtras") LocalDateTime seteDiasAtras,
                                    @Param("statusAtivos") List<StatusLead> statusAtivos);

    /**
     * Query otimizada para buscar leads quentes do dia.
     *
     * @param inicioHoje Início do dia atual
     * @param statusQuentes Lista de status considerados quentes
     * @return Lista de leads quentes com atividade recente
     */
    @Query("SELECT c FROM Cliente c WHERE " +
           "c.statusLead IN :statusQuentes " +
           "AND c.dataUltimaInteracao IS NOT NULL " +
           "AND c.dataUltimaInteracao > :inicioHoje")
    List<Cliente> findLeadsHotToday(@Param("inicioHoje") LocalDateTime inicioHoje,
                                    @Param("statusQuentes") List<StatusLead> statusQuentes);

    @Query("SELECT COUNT(c) FROM Cliente c WHERE c.statusLead = :status")
    long countByStatusLead(@Param("status") StatusLead status);

    boolean existsByEmail(String email);
}