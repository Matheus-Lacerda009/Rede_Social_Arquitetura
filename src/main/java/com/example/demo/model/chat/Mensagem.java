package com.example.demo.model.chat;

import com.example.demo.enums.chat.TipoMensagem;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "chat_mensagens",
        indexes = @Index(name = "idx_chat_mensagens_conversa_data", columnList = "conversa_id, criada_em"))
public class Mensagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversa_id", nullable = false)
    private Conversa conversa;

    @Column(name = "remetente_id", nullable = false)
    private Long remetenteId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMensagem tipo;

    @Column(nullable = false, length = 4000)
    private String conteudo;

    @Column(name = "criada_em", nullable = false, updatable = false)
    private Instant criadaEm;

    @Column(name = "editada_em")
    private Instant editadaEm;

    /** Exclusão lógica: null = mensagem visível. */
    @Column(name = "excluida_em")
    private Instant excluidaEm;

    @PrePersist
    void aoCriar() {
        if (criadaEm == null) criadaEm = Instant.now();
    }

    public boolean isExcluida() { return excluidaEm != null; }

    public void editar(String novoConteudo) {
        this.conteudo = novoConteudo;
        this.editadaEm = Instant.now();
    }

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
