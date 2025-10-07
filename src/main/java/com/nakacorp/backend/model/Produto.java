package com.nakacorp.backend.model;

import com.nakacorp.backend.model.enums.TipoCobranca;
import com.nakacorp.backend.model.enums.TipoPagamento;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entidade que representa um Produto no sistema CRM.
 * <p>
 * Produtos podem ter diferentes tipos de cobrança (UNICO, MENSAL, ANUAL)
 * e formas de pagamento (CARTAO, PIX, BOLETO).
 * </p>
 *
 * @author Klleriston Andrade
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "tb_produto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_produto")
    private Long id;

    @NotBlank
    @Size(max = 255)
    @Column(name = "nome", nullable = false, length = 255)
    private String nome;

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    @Size(max = 100)
    @Column(name = "categoria", length = 100)
    private String categoria;

    @NotNull
    @Column(name = "preco", nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    @Column(name = "pago")
    private Boolean pago;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pagamento", length = 20)
    private TipoPagamento tipoPagamento;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cobranca", length = 20)
    private TipoCobranca tipoCobranca = TipoCobranca.UNICO;

    @Column(name = "ativo")
    private Boolean ativo = true;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "produto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ClienteInteresse> interesses;
}