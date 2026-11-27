package com.example.demo.dto.chat;

/**
 * O que o cliente recebe pelo WebSocket.
 * tipo: MENSAGEM_NOVA, MENSAGEM_EDITADA ou MENSAGEM_EXCLUIDA.
 */
public record MensagemEventoDTO(String tipo, Long conversaId, MensagemResponseDTO mensagem) {

    public static final String NOVA = "MENSAGEM_NOVA";
    public static final String EDITADA = "MENSAGEM_EDITADA";
    public static final String EXCLUIDA = "MENSAGEM_EXCLUIDA";
}
