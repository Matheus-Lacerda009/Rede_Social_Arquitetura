package com.example.demo.dto.video;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ComentarioCriacaoDTO {

    private String texto;

    private Long videoId;
}