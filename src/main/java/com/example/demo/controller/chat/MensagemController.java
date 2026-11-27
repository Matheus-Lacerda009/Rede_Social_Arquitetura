package com.example.demo.controller.chat;

import com.example.demo.dto.chat.MensagemAtualizacaoDTO;
import com.example.demo.dto.chat.MensagemEnvioDTO;
import com.example.demo.dto.chat.MensagemResponseDTO;
import com.example.demo.service.chat.LeituraMensagemService;
import com.example.demo.service.chat.MensagemService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.example.demo.common.security.UsuarioLogado;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class MensagemController {

    private final MensagemService mensagemService;
    private final LeituraMensagemService leituraService;

    public MensagemController(MensagemService mensagemService, LeituraMensagemService leituraService) {
        this.mensagemService = mensagemService;
        this.leituraService = leituraService;
    }

    @PostMapping("/conversas/{conversaId}/mensagens")
    public ResponseEntity<MensagemResponseDTO> enviar(@UsuarioLogado Long usuarioId,
                                                      @PathVariable Long conversaId,
                                                      @Valid @RequestBody MensagemEnvioDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mensagemService.enviar(conversaId, usuarioId, dto));
    }

    @GetMapping("/conversas/{conversaId}/mensagens")
    public Page<MensagemResponseDTO> listar(@UsuarioLogado Long usuarioId,
                                            @PathVariable Long conversaId,
                                            @PageableDefault(size = 30) Pageable pageable) {
        return mensagemService.listar(conversaId, usuarioId, pageable);
    }

    @PutMapping("/mensagens/{mensagemId}")
    public MensagemResponseDTO editar(@UsuarioLogado Long usuarioId,
                                      @PathVariable Long mensagemId,
                                      @Valid @RequestBody MensagemAtualizacaoDTO dto) {
        return mensagemService.editar(mensagemId, usuarioId, dto);
    }

    @DeleteMapping("/mensagens/{mensagemId}")
    public ResponseEntity<Void> excluir(@UsuarioLogado Long usuarioId,
                                        @PathVariable Long mensagemId) {
        mensagemService.excluir(mensagemId, usuarioId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/mensagens/{mensagemId}/leitura")
    public ResponseEntity<Void> marcarMensagemComoLida(@UsuarioLogado Long usuarioId,
                                                       @PathVariable Long mensagemId) {
        leituraService.marcarMensagemComoLida(mensagemId, usuarioId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/conversas/{conversaId}/leitura")
    public Map<String, Integer> marcarConversaComoLida(@UsuarioLogado Long usuarioId,
                                                       @PathVariable Long conversaId) {
        return Map.of("marcadas", leituraService.marcarConversaComoLida(conversaId, usuarioId));
    }
}
