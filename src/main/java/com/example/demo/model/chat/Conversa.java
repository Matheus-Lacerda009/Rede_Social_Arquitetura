package com.example.demo.model.chat;

import com.example.demo.enums.chat.TipoConversa;
import jakarta.persistence.*;

import java.time.Instant;

/**
 * Conversa individual (2 participantes) ou em grupo.
 * Usuários são referenciados apenas por id (sem FK/JPA para o módulo de usuários).
 */
@Entity
@Table(name = "chat_conversas")
public class Conversa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoConversa tipo;

    @Column(length = 150)
    private String titulo;

    @Column(name = "criado_por", nullable = false)
    private Long criadoPor;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "ultima_atividade_em", nullable = false)
    private Instant ultimaAtividadeEm;

    @PrePersist
    void aoCriar() {
        Instant agora = Instant.now();
        if (criadoEm == null) criadoEm = agora;
        if (ultimaAtividadeEm == null) ultimaAtividadeEm = agora;
    }

    public Long getId() { return id; }
    public TipoConversa getTipo() { return tipo; }
    public void setTipo(TipoConversa tipo) { this.tipo = tipo; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public Long getCriadoPor() { return criadoPor; }
    public void setCriadoPor(Long criadoPor) { this.criadoPor = criadoPor; }
    public Instant getCriadoEm() { return criadoEm; }
    public Instant getUltimaAtividadeEm() { return ultimaAtividadeEm; }
    public void setUltimaAtividadeEm(Instant ultimaAtividadeEm) { this.ultimaAtividadeEm = ultimaAtividadeEm; }
}
