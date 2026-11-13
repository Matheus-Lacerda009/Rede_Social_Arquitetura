package com.example.demo.model.usuario;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String username;

    @Column(nullable = false, unique = true, length = 254)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusUsuario status = StatusUsuario.ACTIVE;

    @Column(name = "last_login_at")
    private LocalDateTime ultimoLogin;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime atualizadoEm;

    @PrePersist
    protected void aoCriar() {
        LocalDateTime agora = LocalDateTime.now();
        criadoEm = agora;
        atualizadoEm = agora;
    }

    @PreUpdate
    protected void aoAtualizar() {
        atualizadoEm = LocalDateTime.now();
    }
}