package com.example.demo.dto.usuario;

import jakarta.validation.constraints.NotBlank;

public record UsuarioLoginDTO(

        @NotBlank
        String identificador,

        @NotBlank
        String senha

) {}
