package com.example.demo.model.chat;

import com.example.demo.enums.chat.PapelParticipante;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "chat_participantes")
public class ParticipanteConversa {

    @EmbeddedId
    private ParticipanteConversaId id;

    @MapsId("conversaId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversa_id")
    private Conversa conversa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PapelParticipante papel;

    @Column(name = "entrada_em", nullable = false)
    private Instant entradaEm;

    /** Preenchido quando o participante sai ou é removido; null = participante ativo. */
    @Column(name = "saiu_em")
    private Instant saiuEm;

    protected ParticipanteConversa() {
    }

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
