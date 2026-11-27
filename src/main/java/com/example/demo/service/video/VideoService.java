package com.example.demo.service.video;

import com.example.demo.common.exception.RegraNegocioException;
import com.example.demo.common.exception.RecursoNaoEncontradoException;
import com.example.demo.dto.video.VideoAtualizacaoDTO;
import com.example.demo.dto.video.VideoCriacaoDTO;
import com.example.demo.dto.video.VideoResponseDTO;
import com.example.demo.enums.usuario.StatusUsuario;
import com.example.demo.enums.video.StatusVideo;
import com.example.demo.enums.video.VisibilidadeVideo;
import com.example.demo.mapper.video.VideoMapper;
import com.example.demo.model.usuario.Usuario;
import com.example.demo.model.video.Video;
import com.example.demo.repository.usuario.UsuarioRepositoryInterface;
import com.example.demo.repository.video.VideoRepositoryInterface;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class VideoService {

    private final VideoRepositoryInterface videoRepository;
    private final UsuarioRepositoryInterface usuarioRepository;
    private final VideoMapper videoMapper;

    public VideoService(
            VideoRepositoryInterface videoRepository,
            UsuarioRepositoryInterface usuarioRepository,
            VideoMapper videoMapper
    ) {
        this.videoRepository = videoRepository;
        this.usuarioRepository = usuarioRepository;
        this.videoMapper = videoMapper;
    }

    @Transactional
    public VideoResponseDTO criar(VideoCriacaoDTO dto) {
        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));

        if (usuario.getStatus() != StatusUsuario.ACTIVE) {
            throw new RegraNegocioException("Somente usuários ativos podem publicar vídeos.");
        }

        Video video = videoMapper.toEntity(dto);
        video.setUsuario(usuario);
        video.setDataPublicacao(LocalDateTime.now());
        video.setStatus(StatusVideo.READY);
        video.setVisibilidade(dto.getVisibilidade() != null
                ? dto.getVisibilidade()
                : VisibilidadeVideo.PUBLIC);

        Video videoSalvo = videoRepository.save(video);
        return videoMapper.toResponseDTO(videoSalvo);
    }

    @Transactional(readOnly = true)
    public List<VideoResponseDTO> listarTodos() {
        return videoRepository
                .findByVisibilidadeAndStatusOrderByDataPublicacaoDesc(
                        VisibilidadeVideo.PUBLIC,
                        StatusVideo.READY
                )
                .stream()
                .map(videoMapper::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public VideoResponseDTO buscarPorId(Long id) {
        Video video = buscarVideoPublico(id);
        return videoMapper.toResponseDTO(video);
    }

    @Transactional
    public VideoResponseDTO atualizar(Long id, VideoAtualizacaoDTO dto) {
        Video video = buscarVideoPorId(id);
        videoMapper.updateEntity(video, dto);
        Video videoAtualizado = videoRepository.save(video);
        return videoMapper.toResponseDTO(videoAtualizado);
    }

    @Transactional
    public void excluir(Long id) {
        Video video = buscarVideoPorId(id);
        videoRepository.delete(video);
    }

    private Video buscarVideoPublico(Long id) {
        return videoRepository.findByIdAndVisibilidadeAndStatus(
                        id,
                        VisibilidadeVideo.PUBLIC,
                        StatusVideo.READY
                )
                .orElseThrow(() -> new RecursoNaoEncontradoException("Vídeo não encontrado."));
    }

    private Video buscarVideoPorId(Long id) {
        return videoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Vídeo não encontrado."));
    }
}
