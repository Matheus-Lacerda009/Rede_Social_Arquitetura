package com.example.demo.dto.usuario;

import com.example.demo.enums.usuario.StatusUsuario;

public record UsuarioResponseDTO(
        Long id,
        String username,
        String email,
        StatusUsuario status
) {}
