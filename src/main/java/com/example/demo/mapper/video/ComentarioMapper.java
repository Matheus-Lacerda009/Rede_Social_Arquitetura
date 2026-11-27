package com.example.demo.mapper.video;

import com.example.demo.dto.video.ComentarioAtualizacaoDTO;
import com.example.demo.dto.video.ComentarioCriacaoDTO;
import com.example.demo.dto.video.ComentarioResponseDTO;
import com.example.demo.model.video.Comentario;
import com.example.demo.model.video.Video;
import org.springframework.stereotype.Component;

@Component
public class ComentarioMapper {

    public Comentario toEntity(ComentarioCriacaoDTO dto, Video video) {
        Comentario comentario = new Comentario();
        comentario.setTexto(dto.getTexto());
        comentario.setVideo(video);
        return comentario;
    }

    public ComentarioResponseDTO toResponseDTO(Comentario comentario) {
        ComentarioResponseDTO dto = new ComentarioResponseDTO();

        dto.setId(comentario.getId());
        dto.setTexto(comentario.getTexto());
        dto.setDataComentario(comentario.getDataComentario());

        if (comentario.getVideo() != null) {
            dto.setVideoId(comentario.getVideo().getId());
        }

        if (comentario.getUsuario() != null) {
            dto.setUsuarioId(comentario.getUsuario().getId());
        }

        return dto;
    }

    public void updateEntity(Comentario comentario, ComentarioAtualizacaoDTO dto) {
        if (dto.getTexto() != null) {
            comentario.setTexto(dto.getTexto());
        }
    }
}
