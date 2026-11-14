package com.example.demo.mapper.usuario;

import com.example.demo.dto.usuario.UsuarioCadastroDTO;
import com.example.demo.dto.usuario.UsuarioResponseDTO;
import com.example.demo.dto.usuario.UsuarioResumoDTO;
import com.example.demo.model.usuario.Usuario;
import com.example.demo.model.usuario.PerfilUsuario;

import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    public Usuario paraEntidade(UsuarioCadastroDTO dto) {
        Usuario usuario = new Usuario();

        usuario.setUsername(dto.username());
        usuario.setEmail(dto.email());
        usuario.setSenha(dto.senha());

        return usuario;
    }

    public UsuarioResponseDTO paraResponseDTO(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getEmail(),
                usuario.getStatus()
        );
    }

    public UsuarioResumoDTO paraResumoDTO(
            Usuario usuario,
            PerfilUsuario perfil
    ) {
        return new UsuarioResumoDTO(
                usuario.getId(),
                usuario.getUsername(),
                perfil != null ? perfil.getNomeExibicao() : null,
                perfil != null ? perfil.getFotoPerfil() : null
        );
    }
}
