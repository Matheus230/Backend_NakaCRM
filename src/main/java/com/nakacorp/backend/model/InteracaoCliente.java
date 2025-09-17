package com.nakacorp.backend.model;

import com.nakacorp.backend.model.enums.TipoInteracao;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "tb_interacao_cliente")
public class InteracaoCliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_interacao")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_cliente", nullable = false)
    @NotNull
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "tipo_interacao", nullable = false, length = 30)
    private TipoInteracao tipoInteracao;

    @NotBlank
    @Column(name = "descricao", nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dados_extras", columnDefinition = "jsonb")
    private Map<String, Object> dadosExtras;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public InteracaoCliente() {}

    public InteracaoCliente(Cliente cliente, TipoInteracao tipoInteracao, String descricao) {
        this.cliente = cliente;
        this.tipoInteracao = tipoInteracao;
        this.descricao = descricao;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public TipoInteracao getTipoInteracao() { return tipoInteracao; }
    public void setTipoInteracao(TipoInteracao tipoInteracao) { this.tipoInteracao = tipoInteracao; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public Map<String, Object> getDadosExtras() { return dadosExtras; }
    public void setDadosExtras(Map<String, Object> dadosExtras) { this.dadosExtras = dadosExtras; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}