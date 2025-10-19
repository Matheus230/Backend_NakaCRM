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

    /**
     * Busca próximas interações agendadas (baseado em dados_extras com campo 'dataAgendada').
     * Como próximas tarefas são armazenadas em JSONB, esta query busca interações recentes
     * que possam conter informações de agendamento futuro.
     *
     * @param dataLimite Data limite para considerar interações
     * @return Lista de interações com potencial de agendamento
     */
    @Query("SELECT i FROM InteracaoCliente i " +
           "LEFT JOIN FETCH i.cliente c " +
           "LEFT JOIN FETCH i.usuario u " +
           "WHERE i.tipoInteracao IN ('TELEFONE', 'EMAIL') " +
           "AND i.createdAt >= :dataLimite " +
           "ORDER BY i.createdAt ASC")
    List<InteracaoCliente> findProximasInteracoes(@Param("dataLimite") LocalDateTime dataLimite);
}