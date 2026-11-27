package com.example.demo.dto.chat;

import com.example.demo.enums.chat.TipoConversa;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

/** INDIVIDUAL: exatamente um outro id. GRUPO: título e ao menos um outro id. */
public record ConversaCriacaoDTO(
        @NotNull TipoConversa tipo,
        @Size(max = 100) String titulo,
        @NotEmpty List<@NotNull @Positive Long> participantesIds
) { }
