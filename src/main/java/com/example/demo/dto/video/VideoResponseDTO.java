package com.example.demo.dto.video;

import com.example.demo.enums.video.StatusVideo;
import com.example.demo.enums.video.VisibilidadeVideo;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VideoResponseDTO {

    private Long id;

    private String titulo;

    private String descricao;

    private String url;

    private LocalDateTime dataPublicacao;

    private StatusVideo status;

    private VisibilidadeVideo visibilidade;

    private Long usuarioId;
}