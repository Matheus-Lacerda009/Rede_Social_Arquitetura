package com.example.demo.service.video;

import com.example.demo.common.exception.RecursoNaoEncontradoException;
import com.example.demo.dto.video.ComentarioAtualizacaoDTO;
import com.example.demo.dto.video.ComentarioCriacaoDTO;
import com.example.demo.dto.video.ComentarioResponseDTO;
import com.example.demo.mapper.video.ComentarioMapper;
import com.example.demo.model.video.Comentario;
import com.example.demo.model.video.Video;
import com.example.demo.repository.video.ComentarioRepositoryInterface;
import com.example.demo.repository.video.VideoRepositoryInterface;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ComentarioService {

    private final ComentarioRepositoryInterface comentarioRepository;
    private final VideoRepositoryInterface videoRepository;
    private final ComentarioMapper comentarioMapper;

    public ComentarioService(
            ComentarioRepositoryInterface comentarioRepository,
            VideoRepositoryInterface videoRepository,
            ComentarioMapper comentarioMapper
    ) {
        this.comentarioRepository = comentarioRepository;
        this.videoRepository = videoRepository;
        this.comentarioMapper = comentarioMapper;
    }

    public ComentarioResponseDTO criar(ComentarioCriacaoDTO dto) {

        Video video = buscarVideo(dto.getVideoId());

        Comentario comentario = comentarioMapper.toEntity(dto, video);

        comentario.setDataComentario(LocalDateTime.now());

        Comentario comentarioSalvo = comentarioRepository.save(comentario);

        return comentarioMapper.toResponseDTO(comentarioSalvo);
    }

    public List<ComentarioResponseDTO> listarPorVideo(Long videoId) {

        buscarVideo(videoId);

        return comentarioRepository.findByVideoId(videoId)
                .stream()
                .map(comentarioMapper::toResponseDTO)
                .toList();
    }

    public ComentarioResponseDTO buscarPorId(Long id) {

        Comentario comentario = buscarComentario(id);

        return comentarioMapper.toResponseDTO(comentario);
    }

    public ComentarioResponseDTO atualizar(
            Long id,
            ComentarioAtualizacaoDTO dto
    ) {

        Comentario comentario = buscarComentario(id);

        comentarioMapper.updateEntity(comentario, dto);

        Comentario comentarioAtualizado =
                comentarioRepository.save(comentario);

        return comentarioMapper.toResponseDTO(comentarioAtualizado);
    }

    public void excluir(Long id) {

        Comentario comentario = buscarComentario(id);

        comentarioRepository.delete(comentario);
    }

    private Comentario buscarComentario(Long id) {

        return comentarioRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Comentário não encontrado"
                        )
                );
    }

    private Video buscarVideo(Long id) {

        return videoRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Vídeo não encontrado"
                        )
                );
    }
}