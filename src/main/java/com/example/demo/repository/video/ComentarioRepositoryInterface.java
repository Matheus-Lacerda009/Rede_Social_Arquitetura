package com.example.demo.repository.video;

import com.example.demo.model.video.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComentarioRepositoryInterface extends JpaRepository<Comentario, Long> {

    List<Comentario> findByVideoId(Long videoId);
}