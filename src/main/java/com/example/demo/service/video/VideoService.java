package com.example.demo.service.video;

import com.example.demo.common.exception.RecursoNaoEncontradoException;
import com.example.demo.dto.video.VideoAtualizacaoDTO;
import com.example.demo.dto.video.VideoCriacaoDTO;
import com.example.demo.dto.video.VideoResponseDTO;
import com.example.demo.mapper.video.VideoMapper;
import com.example.demo.model.video.Video;
import com.example.demo.repository.video.VideoRepositoryInterface;
import com.example.demo.enums.video.StatusVideo;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class VideoService {

    private final VideoRepositoryInterface videoRepository;
    private final VideoMapper videoMapper;

    public VideoService(
            VideoRepositoryInterface videoRepository,
            VideoMapper videoMapper
    ) {
        this.videoRepository = videoRepository;
        this.videoMapper = videoMapper;
    }

    public VideoResponseDTO criar(VideoCriacaoDTO dto) {

        Video video = videoMapper.toEntity(dto);

        video.setDataPublicacao(LocalDateTime.now());
        video.setStatus(StatusVideo.ATIVO);

        Video videoSalvo = videoRepository.save(video);

        return videoMapper.toResponseDTO(videoSalvo);
    }

    public List<VideoResponseDTO> listarTodos() {

        return videoRepository.findAll()
                .stream()
                .map(videoMapper::toResponseDTO)
                .toList();
    }

    public VideoResponseDTO buscarPorId(Long id) {

        Video video = buscarVideo(id);

        return videoMapper.toResponseDTO(video);
    }

    public VideoResponseDTO atualizar(Long id, VideoAtualizacaoDTO dto) {

        Video video = buscarVideo(id);

        videoMapper.updateEntity(video, dto);

        Video videoAtualizado = videoRepository.save(video);

        return videoMapper.toResponseDTO(videoAtualizado);
    }

    public void excluir(Long id) {

        Video video = buscarVideo(id);

        videoRepository.delete(video);
    }

    private Video buscarVideo(Long id) {

        return videoRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Vídeo não encontrado")
                );
    }
}