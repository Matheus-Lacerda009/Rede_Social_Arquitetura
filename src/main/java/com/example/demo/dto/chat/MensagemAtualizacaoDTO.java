package com.example.demo.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MensagemAtualizacaoDTO(@NotBlank @Size(max = 4000) String conteudo) {
}
