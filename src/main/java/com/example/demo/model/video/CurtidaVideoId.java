package com.example.demo.model.video;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CurtidaVideoId implements Serializable {

    private Long usuarioId;

    private Long videoId;
}