package com.example.demo.dto.usuario;

import jakarta.validation.constraints.Size;

public record PerfilAtualizacaoDTO(

        @Size(max = 50)
        String nomeExibicao,

        @Size(max = 500)
        String bio,

        @Size(max = 2048)
        String fotoPerfil

) {}
