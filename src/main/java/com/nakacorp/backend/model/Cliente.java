package com.nakacorp.backend.model;

import com.nakacorp.backend.model.enums.OrigemLead;
import com.nakacorp.backend.model.enums.StatusLead;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entidade que representa um Cliente/Lead no sistema CRM.
 * <p>
 * Um cliente pode ter diferentes status ao longo do funil de vendas:
 * NOVO → CONTATADO → QUALIFICADO → OPORTUNIDADE → CLIENTE/PERDIDO
 * </p>
 *
 * @author Klleriston Andrade
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "tb_cliente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
}