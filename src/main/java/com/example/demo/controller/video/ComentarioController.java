package com.example.demo.controller.video;

import com.example.demo.dto.video.ComentarioAtualizacaoDTO;
import com.example.demo.dto.video.ComentarioCriacaoDTO;
import com.example.demo.dto.video.ComentarioResponseDTO;
import com.example.demo.service.video.ComentarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comentarios")
public class ComentarioController {

    private final ComentarioService comentarioService;

    public ComentarioController(ComentarioService comentarioService) {
        this.comentarioService = comentarioService;
    }

    @PostMapping
    public ResponseEntity<ComentarioResponseDTO> criar(
            @RequestBody ComentarioCriacaoDTO dto
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(comentarioService.criar(dto));
    }

    @GetMapping("/video/{videoId}")
    public ResponseEntity<List<ComentarioResponseDTO>> listarPorVideo(
            @PathVariable Long videoId
    ) {
        return ResponseEntity.ok(
                comentarioService.listarPorVideo(videoId)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComentarioResponseDTO> buscarPorId(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                comentarioService.buscarPorId(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ComentarioResponseDTO> atualizar(
            @PathVariable Long id,
            @RequestBody ComentarioAtualizacaoDTO dto
    ) {
        return ResponseEntity.ok(
                comentarioService.atualizar(id, dto)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @PathVariable Long id
    ) {
        comentarioService.excluir(id);

        return ResponseEntity.noContent().build();
    }
}