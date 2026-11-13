package com.example.demo.dto.usuario;

import com.example.demo.model.usuario.StatusUsuario;

public record UsuarioResponseDTO(
        Long id,
        String username,
        String email,
        StatusUsuario status
) {}
