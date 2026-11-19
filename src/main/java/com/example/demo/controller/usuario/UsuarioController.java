
package com.example.demo.controller.usuario;

import com.example.demo.dto.usuario.UsuarioCadastroDTO;
import com.example.demo.dto.usuario.UsuarioLoginDTO;
import com.example.demo.dto.usuario.UsuarioResponseDTO;
import com.example.demo.service.usuario.UsuarioService;
import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public ResponseEntity<@NonNull UsuarioResponseDTO> cadastrar(
            @RequestBody UsuarioCadastroDTO dto
    ) {
        UsuarioResponseDTO usuario = usuarioService.cadastrar(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(usuario);
    }

    @PostMapping("/login")
    public ResponseEntity<@NonNull UsuarioResponseDTO> login(
            @RequestBody UsuarioLoginDTO dto
    ) {
        UsuarioResponseDTO usuario = usuarioService.login(dto);

        return ResponseEntity.ok(usuario);
    }

    @GetMapping("/{id}")
    public ResponseEntity<@NonNull UsuarioResponseDTO> buscarPorId(
            @PathVariable Long id
    ) {
        UsuarioResponseDTO usuario = usuarioService.buscarPorId(id);

        return ResponseEntity.ok(usuario);
    }
}
