package com.nakacorp.backend.repository;

import com.nakacorp.backend.model.Produto;
import com.nakacorp.backend.model.enums.TipoCobranca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    List<Produto> findByAtivoTrue();

    List<Produto> findByCategoria(String categoria);

    List<Produto> findByAtivoTrueAndCategoria(String categoria);

    @Query("SELECT p FROM Produto p WHERE p.nome ILIKE %:nome%")
    List<Produto> findByNomeContainingIgnoreCase(@Param("nome") String nome);

    @Query("SELECT p FROM Produto p WHERE p.preco BETWEEN :precoMin AND :precoMax")
    List<Produto> findByPrecoBetween(@Param("precoMin") BigDecimal precoMin, @Param("precoMax") BigDecimal precoMax);

    List<Produto> findByTipoCobranca(TipoCobranca tipoCobranca);

    @Query("SELECT DISTINCT p.categoria FROM Produto p WHERE p.ativo = true ORDER BY p.categoria")
    List<String> findDistinctCategorias();
}