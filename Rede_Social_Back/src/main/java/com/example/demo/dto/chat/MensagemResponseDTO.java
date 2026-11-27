package com.example.demo.dto.chat;

import com.example.demo.enums.chat.TipoMensagem;

import java.time.Instant;

/** Mensagens excluídas retornam excluida=true e conteudo=null. */
public record MensagemResponseDTO(
        Long id,
        Long conversaId,
        Long remetenteId,
        TipoMensagem tipo,
        String conteudo,
        Instant criadaEm,
        Instant editadaEm,
        boolean excluida
) {
}
