package com.example.demo.dto.chat;

import java.util.List;

/**
 * Evento interno publicado pelo service ("isto aconteceu, avise estes usuários").
 * O service não sabe quem escuta nem como a entrega é feita (WebSocket, push, e-mail...).
 */
public record NotificacaoChat(List<Long> destinatarios, MensagemEventoDTO evento) {
}
