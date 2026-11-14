package com.example.demo.model.video;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "curtidas_video")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CurtidaVideo {

    @EmbeddedId
    private CurtidaVideoId id;

    private LocalDateTime dataCurtida;

    @ManyToOne
    @MapsId("videoId")
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;
}