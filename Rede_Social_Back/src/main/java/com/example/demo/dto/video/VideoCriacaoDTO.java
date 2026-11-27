package com.example.demo.dto.video;

import com.example.demo.enums.video.VisibilidadeVideo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VideoCriacaoDTO {

    @NotBlank(message = "O título é obrigatório.")
    @Size(max = 150, message = "O título deve ter no máximo 150 caracteres.")
    private String titulo;

    @Size(max = 5000, message = "A descrição deve ter no máximo 5000 caracteres.")
    private String descricao;

    @NotBlank(message = "A URL do vídeo é obrigatória.")
    @Size(max = 2048, message = "A URL deve ter no máximo 2048 caracteres.")
    @Pattern(regexp = "^https?://\\S+$", message = "A URL deve começar com http:// ou https://.")
    private String url;

    private VisibilidadeVideo visibilidade;

    @NotNull(message = "O ID do usuário é obrigatório.")
    @Positive(message = "O ID do usuário deve ser positivo.")
    private Long usuarioId;
}
