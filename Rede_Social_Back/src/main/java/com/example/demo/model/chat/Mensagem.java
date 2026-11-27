package com.example.demo.model.chat;

import com.example.demo.enums.chat.TipoMensagem;
import com.example.demo.enums.chat.TipoMensagemConverter;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "messages",
        indexes = @Index(name = "idx_message_conversation", columnList = "conversation_id, created_at"))
public class Mensagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversa conversa;

    /** Pode ficar nulo quando a conta do remetente é excluída (ON DELETE SET NULL). */
    @Column(name = "sender_id")
    private Long remetenteId;

    @Convert(converter = TipoMensagemConverter.class)
    @Column(name = "message_type", nullable = false, length = 20)
    private TipoMensagem tipo;

    @Column(name = "content", columnDefinition = "TEXT")
    private String conteudo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant criadaEm;

    @Column(name = "edited_at")
    private Instant editadaEm;

    @Column(name = "deleted_at")
    private Instant excluidaEm;

    @PrePersist
    void aoCriar() {
        if (criadaEm == null) criadaEm = Instant.now();
    }

    public boolean isExcluida() { return excluidaEm != null; }
    public void editar(String novoConteudo) { this.conteudo = novoConteudo; this.editadaEm = Instant.now(); }
    public void excluir() { this.excluidaEm = Instant.now(); }

    public Long getId() { return id; }
    public Conversa getConversa() { return conversa; }
    public void setConversa(Conversa conversa) { this.conversa = conversa; }
    public Long getRemetenteId() { return remetenteId; }
    public void setRemetenteId(Long remetenteId) { this.remetenteId = remetenteId; }
    public TipoMensagem getTipo() { return tipo; }
    public void setTipo(TipoMensagem tipo) { this.tipo = tipo; }
    public String getConteudo() { return conteudo; }
    public void setConteudo(String conteudo) { this.conteudo = conteudo; }
    public Instant getCriadaEm() { return criadaEm; }
    public Instant getEditadaEm() { return editadaEm; }
    public Instant getExcluidaEm() { return excluidaEm; }
}
