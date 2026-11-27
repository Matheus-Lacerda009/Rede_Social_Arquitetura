package com.example.demo.enums.chat;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Traduz os nomes públicos do Java para os valores já definidos no MySQL. */
@Converter
public class TipoConversaConverter implements AttributeConverter<TipoConversa, String> {

    @Override
    public String convertToDatabaseColumn(TipoConversa tipo) {
        if (tipo == null) return null;
        return switch (tipo) {
            case INDIVIDUAL -> "DIRECT";
            case GRUPO -> "GROUP";
        };
    }

    @Override
    public TipoConversa convertToEntityAttribute(String valor) {
        if (valor == null) return null;
        return switch (valor) {
            case "DIRECT" -> TipoConversa.INDIVIDUAL;
            case "GROUP" -> TipoConversa.GRUPO;
            default -> throw new IllegalArgumentException("Tipo de conversa desconhecido: " + valor);
        };
    }
}
