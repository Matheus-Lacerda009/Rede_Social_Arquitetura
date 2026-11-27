
package com.example.demo.controller.usuario;

import com.example.demo.dto.usuario.UsuarioResumoDTO;
import com.example.demo.service.usuario.SeguidorService;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios/{usuarioId}/seguidores")
public class SeguidorController {

    private final SeguidorService seguidorService;

    public SeguidorController(SeguidorService seguidorService) {
        this.seguidorService = seguidorService;
    }

    @PostMapping("/{seguidoId}")
    public ResponseEntity<Void> seguir(
            @PathVariable Long usuarioId,
            @PathVariable Long seguidoId
    ) {
        seguidorService.seguir(usuarioId, seguidoId);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{seguidoId}")
    public ResponseEntity<Void> deixarDeSeguir(
            @PathVariable Long usuarioId,
            @PathVariable Long seguidoId
    ) {
        seguidorService.deixarDeSeguir(usuarioId, seguidoId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<@NonNull List<UsuarioResumoDTO>> listarSeguidores(
            @PathVariable Long usuarioId
    ) {
        return ResponseEntity.ok(
                seguidorService.listarSeguidores(usuarioId)
        );
    }

    @GetMapping("/seguindo")
    public ResponseEntity<@NonNull List<UsuarioResumoDTO>> listarSeguindo(
            @PathVariable Long usuarioId
    ) {
        return ResponseEntity.ok(
                seguidorService.listarSeguindo(usuarioId)
        );
    }

    @GetMapping("/contagem")
    public ResponseEntity<@NonNull Long> contarSeguidores(
            @PathVariable Long usuarioId
    ) {
        return ResponseEntity.ok(
                seguidorService.contarSeguidores(usuarioId)
        );
    }

    @GetMapping("/contagem/seguindo")
    public ResponseEntity<@NonNull Long> contarSeguindo(
            @PathVariable Long usuarioId
    ) {
        return ResponseEntity.ok(
                seguidorService.contarSeguindo(usuarioId)
        );
    }
}
