package com.example.demo.repository.video;

import com.example.demo.model.video.Video;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoRepositoryInterface extends JpaRepository<Video, Long> {
}