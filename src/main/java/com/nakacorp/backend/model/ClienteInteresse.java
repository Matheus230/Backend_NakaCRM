package com.nakacorp.backend.model;

import com.nakacorp.backend.model.enums.NivelInteresse;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_cliente_interesse")
public class ClienteInteresse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_interesse")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_cliente", nullable = false)
    @NotNull
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "id_produto", nullable = false)
    @NotNull
    private Produto produto;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_interesse", length = 20)
    private NivelInteresse nivelInteresse = NivelInteresse.MEDIO;

    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String observacoes;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public ClienteInteresse() {}

    public ClienteInteresse(Cliente cliente, Produto produto) {
        this.cliente = cliente;
        this.produto = produto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public Produto getProduto() { return produto; }
    public void setProduto(Produto produto) { this.produto = produto; }

    public NivelInteresse getNivelInteresse() { return nivelInteresse; }
    public void setNivelInteresse(NivelInteresse nivelInteresse) { this.nivelInteresse = nivelInteresse; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}