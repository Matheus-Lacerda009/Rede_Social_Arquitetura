package com.example.demo.controller.video;

import com.example.demo.service.video.CurtidaVideoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/videos")
public class CurtidaVideoController {

    private final CurtidaVideoService curtidaVideoService;

    public CurtidaVideoController(CurtidaVideoService curtidaVideoService) {
        this.curtidaVideoService = curtidaVideoService;
    }

    @PostMapping("/{videoId}/curtidas/{usuarioId}")
    public ResponseEntity<Void> curtir(
            @PathVariable Long videoId,
            @PathVariable Long usuarioId
    ) {
        curtidaVideoService.curtir(usuarioId, videoId);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{videoId}/curtidas/{usuarioId}")
    public ResponseEntity<Void> descurtir(
            @PathVariable Long videoId,
            @PathVariable Long usuarioId
    ) {
        curtidaVideoService.descurtir(usuarioId, videoId);

        return ResponseEntity.noContent().build();
    }
}