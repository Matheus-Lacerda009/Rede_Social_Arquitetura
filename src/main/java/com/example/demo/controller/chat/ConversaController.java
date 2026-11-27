package com.example.demo.controller.chat;

import com.example.demo.dto.chat.ConversaCriacaoDTO;
import com.example.demo.dto.chat.ConversaResponseDTO;
import com.example.demo.service.chat.ConversaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.example.demo.common.security.UsuarioLogado;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat/conversas")
public class ConversaController {

    private final ConversaService conversaService;

    public ConversaController(ConversaService conversaService) {
        this.conversaService = conversaService;
    }

    @PostMapping
    public ResponseEntity<ConversaResponseDTO> criar(@UsuarioLogado Long usuarioId,
                                                     @Valid @RequestBody ConversaCriacaoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(conversaService.criar(usuarioId, dto));
    }

    @GetMapping
    public Page<ConversaResponseDTO> listar(@UsuarioLogado Long usuarioId,
                                            @PageableDefault(size = 20) Pageable pageable) {
        return conversaService.listar(usuarioId, pageable);
    }

    @GetMapping("/{conversaId}")
    public ConversaResponseDTO buscar(@UsuarioLogado Long usuarioId,
                                      @PathVariable Long conversaId) {
        return conversaService.buscar(conversaId, usuarioId);
    }
}
