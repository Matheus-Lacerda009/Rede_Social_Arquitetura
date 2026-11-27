package com.example.demo.model.chat;

import com.example.demo.enums.chat.PapelParticipante;
import com.example.demo.enums.chat.PapelParticipanteConverter;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "conversation_participants")
public class ParticipanteConversa {

    @EmbeddedId
    private ParticipanteConversaId id;

    @MapsId("conversaId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversa conversa;

    @Convert(converter = PapelParticipanteConverter.class)
    @Column(name = "role", nullable = false, length = 20)
    private PapelParticipante papel;

    @Column(name = "joined_at", nullable = false)
    private Instant entradaEm;

    /** Nulo enquanto o usuário estiver participando da conversa. */
    @Column(name = "left_at")
    private Instant saiuEm;

    protected ParticipanteConversa() { }

    public ParticipanteConversa(Conversa conversa, Long usuarioId, PapelParticipante papel) {
        this.conversa = conversa;
        this.id = new ParticipanteConversaId(conversa.getId(), usuarioId);
        this.papel = papel;
        this.entradaEm = Instant.now();
    }

    public boolean isAtivo() { return saiuEm == null; }
    public void sair() { this.saiuEm = Instant.now(); }

    public void reativar(PapelParticipante novoPapel) {
        this.saiuEm = null;
        this.entradaEm = Instant.now();
        this.papel = novoPapel;
    }

    public ParticipanteConversaId getId() { return id; }
    public Long getUsuarioId() { return id.getUsuarioId(); }
    public Conversa getConversa() { return conversa; }
    public PapelParticipante getPapel() { return papel; }
    public void setPapel(PapelParticipante papel) { this.papel = papel; }
    public Instant getEntradaEm() { return entradaEm; }
    public Instant getSaiuEm() { return saiuEm; }
}
