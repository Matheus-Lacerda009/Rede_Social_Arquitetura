package com.example.demo.model.chat;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class LeituraMensagemId implements Serializable {

    @Column(name = "mensagem_id")
    private Long mensagemId;

    @Column(name = "usuario_id")
    private Long usuarioId;

    protected LeituraMensagemId() {
    }

    public LeituraMensagemId(Long mensagemId, Long usuarioId) {
        this.mensagemId = mensagemId;
        this.usuarioId = usuarioId;
    }

    public Long getMensagemId() { return mensagemId; }
    public Long getUsuarioId() { return usuarioId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LeituraMensagemId that)) return false;
        return Objects.equals(mensagemId, that.mensagemId) && Objects.equals(usuarioId, that.usuarioId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mensagemId, usuarioId);
    }
}
