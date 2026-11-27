package com.example.demo.model.video;

import com.example.demo.model.usuario.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "video_likes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CurtidaVideo {

    @EmbeddedId
    private CurtidaVideoId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("usuarioId")
    @JoinColumn(name = "user_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("videoId")
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime dataCurtida;
}
