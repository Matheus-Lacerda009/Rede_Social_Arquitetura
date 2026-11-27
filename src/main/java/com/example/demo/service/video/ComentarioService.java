package com.example.demo.service.video;

import com.example.demo.common.exception.RegraNegocioException;
import com.example.demo.common.exception.RecursoNaoEncontradoException;
import com.example.demo.dto.video.ComentarioAtualizacaoDTO;
import com.example.demo.dto.video.ComentarioCriacaoDTO;
import com.example.demo.dto.video.ComentarioResponseDTO;
import com.example.demo.enums.usuario.StatusUsuario;
import com.example.demo.enums.video.StatusVideo;
import com.example.demo.enums.video.VisibilidadeVideo;
import com.example.demo.mapper.video.ComentarioMapper;
import com.example.demo.model.usuario.Usuario;
import com.example.demo.model.video.Comentario;
import com.example.demo.model.video.Video;
import com.example.demo.repository.usuario.UsuarioRepositoryInterface;
import com.example.demo.repository.video.ComentarioRepositoryInterface;
import com.example.demo.repository.video.VideoRepositoryInterface;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ComentarioService {

    private final ComentarioRepositoryInterface comentarioRepository;
    private final VideoRepositoryInterface videoRepository;
    private final UsuarioRepositoryInterface usuarioRepository;
    private final ComentarioMapper comentarioMapper;

    public ComentarioService(
            ComentarioRepositoryInterface comentarioRepository,
            VideoRepositoryInterface videoRepository,
            UsuarioRepositoryInterface usuarioRepository,
            ComentarioMapper comentarioMapper
    ) {
        this.comentarioRepository = comentarioRepository;
        this.videoRepository = videoRepository;
        this.usuarioRepository = usuarioRepository;
        this.comentarioMapper = comentarioMapper;
    }

    @Transactional
    public ComentarioResponseDTO criar(ComentarioCriacaoDTO dto) {
        Video video = buscarVideoPublico(dto.getVideoId());

        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));

        if (usuario.getStatus() != StatusUsuario.ACTIVE) {
            throw new RegraNegocioException("Somente usuários ativos podem comentar.");
        }

        Comentario comentario = comentarioMapper.toEntity(dto, video);
        comentario.setUsuario(usuario);
        comentario.setDataComentario(LocalDateTime.now());

        Comentario comentarioSalvo = comentarioRepository.save(comentario);
        return comentarioMapper.toResponseDTO(comentarioSalvo);
    }

    @Transactional(readOnly = true)
    public List<ComentarioResponseDTO> listarPorVideo(Long videoId) {
        buscarVideoPublico(videoId);

        return comentarioRepository.findByVideo_IdOrderByDataComentarioAsc(videoId)
                .stream()
                .map(comentarioMapper::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public ComentarioResponseDTO buscarPorId(Long id) {
        Comentario comentario = buscarComentario(id);
        buscarVideoPublico(comentario.getVideo().getId());
        return comentarioMapper.toResponseDTO(comentario);
    }

    @Transactional
    public ComentarioResponseDTO atualizar(Long id, ComentarioAtualizacaoDTO dto) {
        Comentario comentario = buscarComentario(id);
        buscarVideoPublico(comentario.getVideo().getId());

        comentarioMapper.updateEntity(comentario, dto);
        Comentario comentarioAtualizado = comentarioRepository.save(comentario);
        return comentarioMapper.toResponseDTO(comentarioAtualizado);
    }

    @Transactional
    public void excluir(Long id) {
        Comentario comentario = buscarComentario(id);
        buscarVideoPublico(comentario.getVideo().getId());
        comentarioRepository.delete(comentario);
    }

    private Comentario buscarComentario(Long id) {
        return comentarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Comentário não encontrado."));
    }

    private Video buscarVideoPublico(Long id) {
        return videoRepository.findByIdAndVisibilidadeAndStatus(
                        id,
                        VisibilidadeVideo.PUBLIC,
                        StatusVideo.READY
                )
                .orElseThrow(() -> new RecursoNaoEncontradoException("Vídeo não encontrado."));
    }
}
