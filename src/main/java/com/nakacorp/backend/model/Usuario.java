package com.nakacorp.backend.model;

import com.nakacorp.backend.model.enums.TipoUsuario;
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
 * Entidade que representa um Usuário do sistema CRM.
 * <p>
 * Usuários podem ser do tipo ADMIN ou VENDEDOR, com diferentes níveis de acesso.
 * Suporta autenticação por email/senha e OAuth2 (Google).
 * </p>
 *
 * @author Klleriston Andrade
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "tb_usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Email
    @NotBlank
    @Size(max = 255)
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @NotBlank
    @Size(max = 255)
    @Column(name = "senha_hash", nullable = false, length = 255)
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "tipo_usuario", nullable = false, length = 20)
    private TipoUsuario tipoUsuario;

    @Size(max = 255)
    @Column(name = "google_id", unique = true, length = 255)
    private String googleId;

    @Column(name = "ativo")
    private Boolean ativo = true;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<InteracaoCliente> interacoes;
}