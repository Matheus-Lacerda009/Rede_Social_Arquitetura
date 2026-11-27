package com.example.demo.model.chat;

import com.example.demo.enums.chat.TipoConversa;
import com.example.demo.enums.chat.TipoConversaConverter;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "conversations")
public class Conversa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = TipoConversaConverter.class)
    @Column(name = "type", nullable = false, length = 20)
    private TipoConversa tipo;

    @Column(name = "title", length = 100)
    private String titulo;

    @Column(name = "created_by", nullable = false)
    private Long criadoPor;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "updated_at", nullable = false)
    private Instant ultimaAtividadeEm;

    @PrePersist
    void aoCriar() {
        Instant agora = Instant.now();
        if (criadoEm == null) criadoEm = agora;
        if (ultimaAtividadeEm == null) ultimaAtividadeEm = agora;
    }

    @PreUpdate
    void aoAtualizar() {
        ultimaAtividadeEm = Instant.now();
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
