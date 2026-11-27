package com.example.demo.model.video;

import com.example.demo.enums.video.StatusVideo;
import com.example.demo.enums.video.VisibilidadeVideo;
import com.example.demo.model.usuario.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "videos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 150)
    private String titulo;

    @Column(name = "description", columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "video_url", nullable = false, length = 2048)
    private String url;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime dataPublicacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusVideo status;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false)
    private VisibilidadeVideo visibilidade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Usuario usuario;
}
