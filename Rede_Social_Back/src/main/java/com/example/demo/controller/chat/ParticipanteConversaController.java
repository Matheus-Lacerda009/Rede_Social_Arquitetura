package com.example.demo.controller.chat;

import com.example.demo.dto.chat.ParticipanteResponseDTO;
import com.example.demo.service.chat.ParticipanteConversaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.example.demo.common.security.UsuarioLogado;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat/conversas/{conversaId}/participantes")
public class ParticipanteConversaController {

    private final ParticipanteConversaService participanteService;

    public ParticipanteConversaController(ParticipanteConversaService participanteService) {
        this.participanteService = participanteService;
    }

    @GetMapping
    public List<ParticipanteResponseDTO> listar(@UsuarioLogado Long usuarioId,
                                                @PathVariable Long conversaId) {
        return participanteService.listar(conversaId, usuarioId);
    }

    /** Adiciona um usuário ao grupo (somente ADMIN). */
    @PostMapping("/{novoUsuarioId}")
    public ResponseEntity<ParticipanteResponseDTO> adicionar(@UsuarioLogado Long usuarioId,
                                                             @PathVariable Long conversaId,
                                                             @PathVariable Long novoUsuarioId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(participanteService.adicionar(conversaId, usuarioId, novoUsuarioId));
    }

    /** ADMIN remove qualquer um; um participante pode remover a si mesmo (sair do grupo). */
    @DeleteMapping("/{alvoUsuarioId}")
    public ResponseEntity<Void> remover(@UsuarioLogado Long usuarioId,
                                        @PathVariable Long conversaId,
                                        @PathVariable Long alvoUsuarioId) {
        participanteService.remover(conversaId, usuarioId, alvoUsuarioId);
        return ResponseEntity.noContent().build();
    }
}
