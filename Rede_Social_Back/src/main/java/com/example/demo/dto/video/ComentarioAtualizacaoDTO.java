package com.example.demo.dto.video;

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
public class ComentarioAtualizacaoDTO {

    @Size(min = 1, max = 5000, message = "O comentário deve ter entre 1 e 5000 caracteres.")
    @Pattern(regexp = ".*\\S.*", message = "O comentário não pode conter apenas espaços.")
    private String texto;
}
