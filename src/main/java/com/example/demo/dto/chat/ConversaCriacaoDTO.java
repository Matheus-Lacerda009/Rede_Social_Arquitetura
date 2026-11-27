package com.example.demo.dto.chat;

import com.example.demo.enums.chat.TipoConversa;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * INDIVIDUAL: informe exatamente 1 id em participantesIds (o outro usuário).
 * GRUPO: titulo obrigatório e ao menos 1 id em participantesIds (o criador é adicionado automaticamente como ADMIN).
 */
public record ConversaCriacaoDTO(
        @NotNull TipoConversa tipo,
        @Size(max = 150) String titulo,
        @NotEmpty List<@NotNull @Positive Long> participantesIds
) {
}
