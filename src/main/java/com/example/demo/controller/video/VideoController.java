package com.example.demo.controller.video;

import com.example.demo.dto.video.VideoAtualizacaoDTO;
import com.example.demo.dto.video.VideoCriacaoDTO;
import com.example.demo.dto.video.VideoResponseDTO;
import com.example.demo.service.video.VideoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/videos")
public class VideoController {

    private final VideoService videoService;

    public VideoController(VideoService videoService) {
        this.videoService = videoService;
    }

    @PostMapping
    public ResponseEntity<VideoResponseDTO> criar(
            @RequestBody VideoCriacaoDTO dto
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(videoService.criar(dto));
    }

    @GetMapping
    public ResponseEntity<List<VideoResponseDTO>> listarTodos() {
        return ResponseEntity.ok(videoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VideoResponseDTO> buscarPorId(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(videoService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VideoResponseDTO> atualizar(
            @PathVariable Long id,
            @RequestBody VideoAtualizacaoDTO dto
    ) {
        return ResponseEntity.ok(videoService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @PathVariable Long id
    ) {
        videoService.excluir(id);

        return ResponseEntity.noContent().build();
    }
}