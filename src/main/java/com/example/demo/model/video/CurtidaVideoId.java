package com.example.demo.model.video;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CurtidaVideoId implements Serializable {

    @Column(name = "user_id")
    private Long usuarioId;

    @Column(name = "video_id")
    private Long videoId;
}
