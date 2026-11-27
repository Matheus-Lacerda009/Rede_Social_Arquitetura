package com.example.demo.model.chat;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ParticipanteConversaId implements Serializable {

    @Column(name = "conversa_id")
    private Long conversaId;

    @Column(name = "usuario_id")
    private Long usuarioId;

    protected ParticipanteConversaId() {
    }

    public ParticipanteConversaId(Long conversaId, Long usuarioId) {
        this.conversaId = conversaId;
        this.usuarioId = usuarioId;
    }

    public Long getConversaId() { return conversaId; }
    public Long getUsuarioId() { return usuarioId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParticipanteConversaId that)) return false;
        return Objects.equals(conversaId, that.conversaId) && Objects.equals(usuarioId, that.usuarioId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(conversaId, usuarioId);
    }
}
