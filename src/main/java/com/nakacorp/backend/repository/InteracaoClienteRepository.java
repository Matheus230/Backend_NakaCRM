package com.nakacorp.backend.repository;

import com.nakacorp.backend.model.InteracaoCliente;
import com.nakacorp.backend.model.enums.TipoInteracao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InteracaoClienteRepository extends JpaRepository<InteracaoCliente, Long> {

    List<InteracaoCliente> findByClienteIdOrderByCreatedAtDesc(Long clienteId);

    List<InteracaoCliente> findByUsuarioId(Long usuarioId);

    List<InteracaoCliente> findByTipoInteracao(TipoInteracao tipoInteracao);

    @Query("SELECT i FROM InteracaoCliente i WHERE i.cliente.id = :clienteId AND i.tipoInteracao = :tipo ORDER BY i.createdAt DESC")
    List<InteracaoCliente> findByClienteIdAndTipoInteracao(@Param("clienteId") Long clienteId, @Param("tipo") TipoInteracao tipo);

    @Query("SELECT i FROM InteracaoCliente i WHERE i.createdAt BETWEEN :inicio AND :fim ORDER BY i.createdAt DESC")
    List<InteracaoCliente> findByCreatedAtBetween(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("SELECT i FROM InteracaoCliente i WHERE i.cliente.id = :clienteId AND i.createdAt BETWEEN :inicio AND :fim ORDER BY i.createdAt DESC")
    List<InteracaoCliente> findByClienteIdAndCreatedAtBetween(@Param("clienteId") Long clienteId, @Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);
}