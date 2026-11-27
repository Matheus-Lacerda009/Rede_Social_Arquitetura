package com.example.demo.model.chat;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "message_reads")
public class LeituraMensagem {

    @EmbeddedId
    private LeituraMensagemId id;

    @MapsId("mensagemId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "message_id", nullable = false)
    private Mensagem mensagem;

    @Column(name = "read_at", nullable = false)
    private Instant lidaEm;

    protected LeituraMensagem() { }

    public LeituraMensagem(Mensagem mensagem, Long usuarioId) {
        this.mensagem = mensagem;
        this.id = new LeituraMensagemId(mensagem.getId(), usuarioId);
        this.lidaEm = Instant.now();
    }

    public LeituraMensagemId getId() { return id; }
    public Mensagem getMensagem() { return mensagem; }
    public Long getUsuarioId() { return id.getUsuarioId(); }
    public Instant getLidaEm() { return lidaEm; }
}
