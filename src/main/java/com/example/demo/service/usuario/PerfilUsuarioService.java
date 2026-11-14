
package com.example.demo.service.usuario;

import com.example.demo.common.exception.RecursoNaoEncontradoException;

import com.example.demo.dto.usuario.PerfilAtualizacaoDTO;
import com.example.demo.dto.usuario.PerfilUsuarioDTO;

import com.example.demo.mapper.usuario.PerfilUsuarioMapper;
import com.example.demo.model.usuario.PerfilUsuario;
import com.example.demo.repository.usuario.PerfilUsuarioRepositoryInterface;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PerfilUsuarioService {

    private final PerfilUsuarioRepositoryInterface perfilUsuarioRepository;
    private final PerfilUsuarioMapper perfilUsuarioMapper;

    public PerfilUsuarioService(
            PerfilUsuarioRepositoryInterface perfilUsuarioRepository,
            PerfilUsuarioMapper perfilUsuarioMapper
    ) {
        this.perfilUsuarioRepository = perfilUsuarioRepository;
        this.perfilUsuarioMapper = perfilUsuarioMapper;
    }

    @Transactional(readOnly = true)
    public PerfilUsuarioDTO buscarPorUsuarioId(Long usuarioId) {

        PerfilUsuario perfil = perfilUsuarioRepository.findById(usuarioId)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Perfil não encontrado."
                        )
                );

        return perfilUsuarioMapper.paraDTO(perfil);
    }

    @Transactional
    public PerfilUsuarioDTO atualizar(
            Long usuarioId,
            PerfilAtualizacaoDTO dto
    ) {

        PerfilUsuario perfil = perfilUsuarioRepository.findById(usuarioId)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Perfil não encontrado."
                        )
                );

        perfilUsuarioMapper.atualizarEntidade(dto, perfil);

        PerfilUsuario perfilAtualizado =
                perfilUsuarioRepository.save(perfil);

        return perfilUsuarioMapper.paraDTO(perfilAtualizado);
    }
}
