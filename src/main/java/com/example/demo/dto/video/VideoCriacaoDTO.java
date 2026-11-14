package com.example.demo.dto.video;

import com.example.demo.enums.video.VisibilidadeVideo;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VideoCriacaoDTO {

    private String titulo;

    private String descricao;

    private String url;

    private VisibilidadeVideo visibilidade;
}