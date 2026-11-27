package com.example.demo.model.usuario;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_follows")
@Getter
@Setter
@NoArgsConstructor
public class Seguidor {

    @EmbeddedId
    private SeguidorId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("seguidorId")
    @JoinColumn(name = "follower_id")
    private Usuario seguidor;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("seguidoId")
    @JoinColumn(name = "followed_id")
    private Usuario seguido;

    public Seguidor(Usuario seguidor, Usuario seguido) {
        this.seguidor = seguidor;
        this.seguido = seguido;

        this.id = new SeguidorId(
                seguidor.getId(),
                seguido.getId()
        );
    }
}