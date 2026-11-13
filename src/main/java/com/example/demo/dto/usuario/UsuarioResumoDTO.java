package com.example.demo.dto.usuario;

public record UsuarioResumoDTO(
        Long id,
        String username,
        String nomeExibicao,
        String fotoPerfil
) {}