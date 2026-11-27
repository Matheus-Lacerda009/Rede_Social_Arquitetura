package com.example.demo.dto.video;

import com.example.demo.enums.video.VisibilidadeVideo;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VideoAtualizacaoDTO {

    @Size(min = 1, max = 150, message = "O título deve ter entre 1 e 150 caracteres.")
    @Pattern(regexp = ".*\\S.*", message = "O título não pode conter apenas espaços.")
    private String titulo;

    @Size(max = 5000, message = "A descrição deve ter no máximo 5000 caracteres.")
    private String descricao;

    @Size(max = 2048, message = "A URL deve ter no máximo 2048 caracteres.")
    @Pattern(regexp = "^https?://\\S+$", message = "A URL deve começar com http:// ou https://.")
    private String url;

    private VisibilidadeVideo visibilidade;
}
