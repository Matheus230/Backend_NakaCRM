package com.nakacorp.backend.model;

import com.nakacorp.backend.model.enums.OrigemLead;
import com.nakacorp.backend.model.enums.StatusLead;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "tb_cliente")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Long id;

    @NotBlank
    @Size(max = 255)
    @Column(name = "nome", nullable = false, length = 255)
    private String nome;

    @Email
    @NotBlank
    @Size(max = 255)
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Size(max = 20)
    @Column(name = "telefone", length = 20)
    private String telefone;

    @Column(name = "endereco", columnDefinition = "TEXT")
    private String endereco;

    @Size(max = 100)
    @Column(name = "cidade", length = 100)
    private String cidade;

    @Size(max = 2)
    @Column(name = "estado", length = 2)
    private String estado;

    @Size(max = 10)
    @Column(name = "cep", length = 10)
    private String cep;

    @Size(max = 255)
    @Column(name = "empresa", length = 255)
    private String empresa;

    @Size(max = 100)
    @Column(name = "cargo", length = 100)
    private String cargo;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "origem_lead", nullable = false, length = 30)
    private OrigemLead origemLead;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_lead", length = 30)
    private StatusLead statusLead = StatusLead.NOVO;

    @Column(name = "data_primeiro_contato")
    private LocalDateTime dataPrimeiroContato;

    @Column(name = "data_ultima_interacao")
    private LocalDateTime dataUltimaInteracao;

    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String observacoes;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "cliente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private LeadOrigem leadOrigem;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ClienteInteresse> interesses;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<InteracaoCliente> interacoes;

    public Cliente() {}

    public Cliente(String nome, String email, OrigemLead origemLead) {
        this.nome = nome;
        this.email = email;
        this.origemLead = origemLead;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }

    public String getEmpresa() { return empresa; }
    public void setEmpresa(String empresa) { this.empresa = empresa; }

    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }

    public OrigemLead getOrigemLead() { return origemLead; }
    public void setOrigemLead(OrigemLead origemLead) { this.origemLead = origemLead; }

    public StatusLead getStatusLead() { return statusLead; }
    public void setStatusLead(StatusLead statusLead) { this.statusLead = statusLead; }

    public LocalDateTime getDataPrimeiroContato() { return dataPrimeiroContato; }
    public void setDataPrimeiroContato(LocalDateTime dataPrimeiroContato) { this.dataPrimeiroContato = dataPrimeiroContato; }

    public LocalDateTime getDataUltimaInteracao() { return dataUltimaInteracao; }
    public void setDataUltimaInteracao(LocalDateTime dataUltimaInteracao) { this.dataUltimaInteracao = dataUltimaInteracao; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LeadOrigem getLeadOrigem() { return leadOrigem; }
    public void setLeadOrigem(LeadOrigem leadOrigem) { this.leadOrigem = leadOrigem; }

    public List<ClienteInteresse> getInteresses() { return interesses; }
    public void setInteresses(List<ClienteInteresse> interesses) { this.interesses = interesses; }

    public List<InteracaoCliente> getInteracoes() { return interacoes; }
    public void setInteracoes(List<InteracaoCliente> interacoes) { this.interacoes = interacoes; }
}