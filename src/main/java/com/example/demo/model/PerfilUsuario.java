package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PerfilUsuario {

    @Id
    @Column(name = "user_id")
    private Long usuarioId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private Usuario usuario;

    @Column(name = "display_name", length = 50)
    private String nomeExibicao;

    @Column(length = 500)
    private String bio;

    @Column(name = "avatar_url")
    private String fotoPerfil;
}