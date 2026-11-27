package com.example.demo.dto.usuario;

public record PerfilUsuarioDTO(
        Long usuarioId,
        String username,
        String nomeExibicao,
        String bio,
        String fotoPerfil
) {}
