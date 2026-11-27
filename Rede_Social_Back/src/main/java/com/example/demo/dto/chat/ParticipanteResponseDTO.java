package com.example.demo.dto.chat;

import com.example.demo.enums.chat.PapelParticipante;

import java.time.Instant;

public record ParticipanteResponseDTO(Long usuarioId, PapelParticipante papel, Instant entradaEm) {
}
