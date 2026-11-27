package com.example.demo.dto.chat;

import com.example.demo.enums.chat.TipoConversa;

import java.time.Instant;
import java.util.List;

public record ConversaResponseDTO(
        Long id,
        TipoConversa tipo,
        String titulo,
        Long criadoPor,
        Instant criadoEm,
        Instant ultimaAtividadeEm,
        List<ParticipanteResponseDTO> participantes,
        MensagemResponseDTO ultimaMensagem,
        long naoLidas
) {
}
