
package com.example.demo.controller.usuario;

import com.example.demo.dto.usuario.PerfilAtualizacaoDTO;
import com.example.demo.dto.usuario.PerfilUsuarioDTO;
import com.example.demo.service.usuario.PerfilUsuarioService;
import jakarta.validation.Valid;
import lombok.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios/{usuarioId}/perfil")
public class PerfilUsuarioController {

    private final PerfilUsuarioService perfilUsuarioService;

    public PerfilUsuarioController(
            PerfilUsuarioService perfilUsuarioService
    ) {
        this.perfilUsuarioService = perfilUsuarioService;
    }

    @GetMapping
    public ResponseEntity<@NonNull PerfilUsuarioDTO> buscarPorUsuarioId(
            @PathVariable Long usuarioId
    ) {
        PerfilUsuarioDTO perfil =
                perfilUsuarioService.buscarPorUsuarioId(usuarioId);

        return ResponseEntity.ok(perfil);
    }

    @PutMapping
    public ResponseEntity<@NonNull PerfilUsuarioDTO> atualizar(
            @PathVariable Long usuarioId,
            @Valid @RequestBody PerfilAtualizacaoDTO dto
    ) {
        PerfilUsuarioDTO perfil =
                perfilUsuarioService.atualizar(usuarioId, dto);

        return ResponseEntity.ok(perfil);
    }
}
