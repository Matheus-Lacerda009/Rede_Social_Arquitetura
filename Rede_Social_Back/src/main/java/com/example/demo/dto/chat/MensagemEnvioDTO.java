package com.example.demo.dto.chat;

import com.example.demo.enums.chat.TipoMensagem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** tipo é opcional (padrão TEXTO). */
public record MensagemEnvioDTO(
        TipoMensagem tipo,
        @NotBlank @Size(max = 4000) String conteudo
) {
}
