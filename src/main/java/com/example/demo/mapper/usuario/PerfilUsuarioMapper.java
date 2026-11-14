
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
        perfil.setNomeExibicao(dto.nomeExibicao());
        perfil.setBio(dto.bio());
        perfil.setFotoPerfil(dto.fotoPerfil());
    }
}
