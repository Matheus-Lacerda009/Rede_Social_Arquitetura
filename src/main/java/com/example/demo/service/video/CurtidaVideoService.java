package com.example.demo.service.video;

import com.example.demo.common.exception.RecursoNaoEncontradoException;
import com.example.demo.model.usuario.Usuario;
import com.example.demo.model.video.CurtidaVideo;
import com.example.demo.model.video.CurtidaVideoId;
import com.example.demo.model.video.Video;
import com.example.demo.repository.usuario.UsuarioRepositoryInterface;
import com.example.demo.repository.video.CurtidaVideoRepositoryInterface;
import com.example.demo.repository.video.VideoRepositoryInterface;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CurtidaVideoService {

    private final CurtidaVideoRepositoryInterface curtidaRepository;
    private final UsuarioRepositoryInterface usuarioRepository;
    private final VideoRepositoryInterface videoRepository;

    public CurtidaVideoService(
            CurtidaVideoRepositoryInterface curtidaRepository,
            UsuarioRepositoryInterface usuarioRepository,
            VideoRepositoryInterface videoRepository
    ) {
        this.curtidaRepository = curtidaRepository;
        this.usuarioRepository = usuarioRepository;
        this.videoRepository = videoRepository;
    }

    public void curtir(Long usuarioId, Long videoId) {

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Usuário não encontrado"
                        )
                );

        Video video = videoRepository.findById(videoId)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Vídeo não encontrado"
                        )
                );

        CurtidaVideoId id = new CurtidaVideoId(usuarioId, videoId);

        if (curtidaRepository.existsById(id)) {
            return;
        }

        CurtidaVideo curtida = new CurtidaVideo();

        curtida.setId(id);
        curtida.setUsuario(usuario);
        curtida.setVideo(video);
        curtida.setDataCurtida(LocalDateTime.now());

        curtidaRepository.save(curtida);
    }

    public void descurtir(Long usuarioId, Long videoId) {

        CurtidaVideoId id = new CurtidaVideoId(usuarioId, videoId);

        if (!curtidaRepository.existsById(id)) {
            return;
        }

        curtidaRepository.deleteById(id);
    }
}