package com.example.demo.service.video;

import com.example.demo.common.exception.RegraNegocioException;
import com.example.demo.common.exception.RecursoNaoEncontradoException;
import com.example.demo.enums.usuario.StatusUsuario;
import com.example.demo.enums.video.StatusVideo;
import com.example.demo.enums.video.VisibilidadeVideo;
import com.example.demo.model.usuario.Usuario;
import com.example.demo.model.video.CurtidaVideo;
import com.example.demo.model.video.CurtidaVideoId;
import com.example.demo.model.video.Video;
import com.example.demo.repository.usuario.UsuarioRepositoryInterface;
import com.example.demo.repository.video.CurtidaVideoRepositoryInterface;
import com.example.demo.repository.video.VideoRepositoryInterface;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public void curtir(Long usuarioId, Long videoId) {
        Usuario usuario = buscarUsuario(usuarioId);
        Video video = buscarVideoPublico(videoId);

        if (usuario.getStatus() != StatusUsuario.ACTIVE) {
            throw new RegraNegocioException("Somente usuários ativos podem curtir vídeos.");
        }

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

    @Transactional
    public void descurtir(Long usuarioId, Long videoId) {
        buscarUsuario(usuarioId);
        videoRepository.findById(videoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Vídeo não encontrado."));

        CurtidaVideoId id = new CurtidaVideoId(usuarioId, videoId);
        if (curtidaRepository.existsById(id)) {
            curtidaRepository.deleteById(id);
        }
    }

    private Usuario buscarUsuario(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));
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
