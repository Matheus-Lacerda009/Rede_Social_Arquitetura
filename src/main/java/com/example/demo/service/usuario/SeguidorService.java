
package com.example.demo.service.usuario;

import com.example.demo.common.exception.RegraNegocioException;
import com.example.demo.common.exception.RecursoNaoEncontradoException;

import com.example.demo.dto.usuario.UsuarioResumoDTO;

import com.example.demo.mapper.usuario.SeguidorMapper;
import com.example.demo.model.usuario.Seguidor;
import com.example.demo.model.usuario.SeguidorId;
import com.example.demo.model.usuario.Usuario;

import com.example.demo.repository.usuario.SeguidorRepositoryInterface;
import com.example.demo.repository.usuario.UsuarioRepositoryInterface;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SeguidorService {

    private final SeguidorRepositoryInterface seguidorRepository;
    private final UsuarioRepositoryInterface usuarioRepository;
    private final SeguidorMapper seguidorMapper;

    public SeguidorService(
            SeguidorRepositoryInterface seguidorRepository,
            UsuarioRepositoryInterface usuarioRepository,
            SeguidorMapper seguidorMapper
    ) {
        this.seguidorRepository = seguidorRepository;
        this.usuarioRepository = usuarioRepository;
        this.seguidorMapper = seguidorMapper;
    }

    @Transactional
    public void seguir(Long seguidorId, Long seguidoId) {

        if (seguidorId.equals(seguidoId)) {
            throw new RegraNegocioException(
                    "Um usuário não pode seguir a si mesmo."
            );
        }

        Usuario seguidor = usuarioRepository.findById(seguidorId)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Usuário que deseja seguir não encontrado."
                        )
                );

        Usuario seguido = usuarioRepository.findById(seguidoId)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Usuário a ser seguido não encontrado."
                        )
                );

        if (seguidorRepository.existsBySeguidor_IdAndSeguido_Id(
                seguidorId, seguidoId
        )) {
            throw new RegraNegocioException(
                    "Você já segue este usuário."
            );
        }

        Seguidor relacao = new Seguidor(seguidor, seguido);

        seguidorRepository.save(relacao);
    }

    @Transactional
    public void deixarDeSeguir(Long seguidorId, Long seguidoId) {

        SeguidorId id = new SeguidorId(seguidorId, seguidoId);

        if (!seguidorRepository.existsById(id)) {
            throw new RecursoNaoEncontradoException(
                    "Relação de seguimento não encontrada."
            );
        }

        seguidorRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<UsuarioResumoDTO> listarSeguidores(Long usuarioId) {

        verificarUsuario(usuarioId);

        return seguidorRepository.findBySeguido_Id(usuarioId)
                .stream()
                .map(seguidorMapper::paraSeguidorDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UsuarioResumoDTO> listarSeguindo(Long usuarioId) {

        verificarUsuario(usuarioId);

        return seguidorRepository.findBySeguidor_Id(usuarioId)
                .stream()
                .map(seguidorMapper::paraSeguidoDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public long contarSeguidores(Long usuarioId) {

        verificarUsuario(usuarioId);

        return seguidorRepository.countBySeguido_Id(usuarioId);
    }

    @Transactional(readOnly = true)
    public long contarSeguindo(Long usuarioId) {

        verificarUsuario(usuarioId);

        return seguidorRepository.countBySeguidor_Id(usuarioId);
    }

    private void verificarUsuario(Long usuarioId) {

        if (!usuarioRepository.existsById(usuarioId)) {
            throw new RecursoNaoEncontradoException(
                    "Usuário não encontrado."
            );
        }
    }
}
