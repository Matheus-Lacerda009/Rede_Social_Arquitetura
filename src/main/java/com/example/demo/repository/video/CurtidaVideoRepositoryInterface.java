package com.example.demo.repository.video;

import com.example.demo.model.video.CurtidaVideo;
import com.example.demo.model.video.CurtidaVideoId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CurtidaVideoRepositoryInterface
        extends JpaRepository<CurtidaVideo, CurtidaVideoId> {
}