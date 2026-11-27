package com.example.demo.model.video;

import com.example.demo.model.usuario.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "curtidas_video")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CurtidaVideo {

    @EmbeddedId
    private CurtidaVideoId id;

    @ManyToOne
    @MapsId("usuarioId")
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @MapsId("videoId")
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;

    private LocalDateTime dataCurtida;
}