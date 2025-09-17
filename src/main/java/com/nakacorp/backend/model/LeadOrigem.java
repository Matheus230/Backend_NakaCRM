package com.nakacorp.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_lead_origem")
public class LeadOrigem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_lead_origem")
    private Long id;

    @OneToOne
    @JoinColumn(name = "id_cliente", nullable = false, unique = true)
    @NotNull
    private Cliente cliente;

    @Size(max = 255)
    @Column(name = "fonte_detalhada", length = 255)
    private String fonteDetalhada;

    @Size(max = 100)
    @Column(name = "utm_source", length = 100)
    private String utmSource;

    @Size(max = 100)
    @Column(name = "utm_medium", length = 100)
    private String utmMedium;

    @Size(max = 100)
    @Column(name = "utm_campaign", length = 100)
    private String utmCampaign;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public LeadOrigem() {}

    public LeadOrigem(Cliente cliente) {
        this.cliente = cliente;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public String getFonteDetalhada() { return fonteDetalhada; }
    public void setFonteDetalhada(String fonteDetalhada) { this.fonteDetalhada = fonteDetalhada; }

    public String getUtmSource() { return utmSource; }
    public void setUtmSource(String utmSource) { this.utmSource = utmSource; }

    public String getUtmMedium() { return utmMedium; }
    public void setUtmMedium(String utmMedium) { this.utmMedium = utmMedium; }

    public String getUtmCampaign() { return utmCampaign; }
    public void setUtmCampaign(String utmCampaign) { this.utmCampaign = utmCampaign; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}