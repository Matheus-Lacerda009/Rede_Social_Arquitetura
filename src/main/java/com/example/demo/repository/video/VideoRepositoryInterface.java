package com.example.demo.repository.video;

import com.example.demo.enums.video.StatusVideo;
import com.example.demo.enums.video.VisibilidadeVideo;
import com.example.demo.model.video.Video;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VideoRepositoryInterface extends JpaRepository<Video, Long> {

    List<Video> findByVisibilidadeAndStatusOrderByDataPublicacaoDesc(
            VisibilidadeVideo visibilidade,
            StatusVideo status
    );

    Optional<Video> findByIdAndVisibilidadeAndStatus(
            Long id,
            VisibilidadeVideo visibilidade,
            StatusVideo status
    );
}
