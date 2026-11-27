package com.example.demo.dto.video;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ComentarioResponseDTO {

    private Long id;

    private String texto;

    private LocalDateTime dataComentario;

    private Long videoId;

    private Long usuarioId;
}