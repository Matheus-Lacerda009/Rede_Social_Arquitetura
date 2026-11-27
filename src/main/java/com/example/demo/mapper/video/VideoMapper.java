package com.example.demo.mapper.video;

import com.example.demo.dto.video.VideoAtualizacaoDTO;
import com.example.demo.dto.video.VideoCriacaoDTO;
import com.example.demo.dto.video.VideoResponseDTO;
import com.example.demo.model.video.Video;
import org.springframework.stereotype.Component;

@Component
public class VideoMapper {

    public Video toEntity(VideoCriacaoDTO dto) {
        Video video = new Video();

        video.setTitulo(dto.getTitulo());
        video.setDescricao(dto.getDescricao());
        video.setUrl(dto.getUrl());
        video.setVisibilidade(dto.getVisibilidade());

        return video;
    }

    public VideoResponseDTO toResponseDTO(Video video) {
        VideoResponseDTO dto = new VideoResponseDTO();

        dto.setId(video.getId());
        dto.setTitulo(video.getTitulo());
        dto.setDescricao(video.getDescricao());
        dto.setUrl(video.getUrl());
        dto.setDataPublicacao(video.getDataPublicacao());
        dto.setStatus(video.getStatus());
        dto.setVisibilidade(video.getVisibilidade());

        if (video.getUsuario() != null) {
            dto.setUsuarioId(video.getUsuario().getId());
        }

        return dto;
    }

    public void updateEntity(
            Video video,
            VideoAtualizacaoDTO dto
    ) {
        video.setTitulo(dto.getTitulo());
        video.setDescricao(dto.getDescricao());
        video.setUrl(dto.getUrl());
        video.setVisibilidade(dto.getVisibilidade());
    }
}