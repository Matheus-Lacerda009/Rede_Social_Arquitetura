package com.example.demo.enums.chat;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class PapelParticipanteConverter implements AttributeConverter<PapelParticipante, String> {

    @Override
    public String convertToDatabaseColumn(PapelParticipante papel) {
        if (papel == null) return null;
        return switch (papel) {
            case MEMBRO -> "MEMBER";
            case ADMIN -> "ADMIN";
        };
    }

    @Override
    public PapelParticipante convertToEntityAttribute(String valor) {
        if (valor == null) return null;
        return switch (valor) {
            case "MEMBER" -> PapelParticipante.MEMBRO;
            case "ADMIN" -> PapelParticipante.ADMIN;
            default -> throw new IllegalArgumentException("Papel de participante desconhecido: " + valor);
        };
    }
}
