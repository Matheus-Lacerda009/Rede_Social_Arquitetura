
package com.example.demo.mapper.usuario;

import com.example.demo.dto.usuario.PerfilAtualizacaoDTO;
import com.example.demo.dto.usuario.PerfilUsuarioDTO;
import com.example.demo.model.usuario.PerfilUsuario;

import org.springframework.stereotype.Component;

@Component
public class PerfilUsuarioMapper {

    public PerfilUsuarioDTO paraDTO(PerfilUsuario perfil) {
        return new PerfilUsuarioDTO(
                perfil.getUsuario().getId(),
                perfil.getUsuario().getUsername(),
                perfil.getNomeExibicao(),
                perfil.getBio(),
                perfil.getFotoPerfil()
        );
    }

    public void atualizarEntidade(
            PerfilAtualizacaoDTO dto,
            PerfilUsuario perfil
    ) {
        if (dto.nomeExibicao() != null) {
            perfil.setNomeExibicao(dto.nomeExibicao());
        }

        if (dto.bio() != null) {
            perfil.setBio(dto.bio());
        }

        if (dto.fotoPerfil() != null) {
            perfil.setFotoPerfil(dto.fotoPerfil());
        }
    }
}
