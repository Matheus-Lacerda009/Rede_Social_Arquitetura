package com.example.demo.dto.video;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class ComentarioCriacaoDTO {

    @NotBlank(message = "O texto do comentário é obrigatório.")
    @Size(max = 5000, message = "O comentário deve ter no máximo 5000 caracteres.")
    private String texto;

    @NotNull(message = "O ID do vídeo é obrigatório.")
    @Positive(message = "O ID do vídeo deve ser positivo.")
    private Long videoId;

    @NotNull(message = "O ID do usuário é obrigatório.")
    @Positive(message = "O ID do usuário deve ser positivo.")
    private Long usuarioId;
}
