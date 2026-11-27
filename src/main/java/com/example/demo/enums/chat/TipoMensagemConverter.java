package com.example.demo.enums.chat;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class TipoMensagemConverter implements AttributeConverter<TipoMensagem, String> {

    @Override
    public String convertToDatabaseColumn(TipoMensagem tipo) {
        if (tipo == null) return null;
        return switch (tipo) {
            case TEXTO -> "TEXT";
            case IMAGEM -> "IMAGE";
            case VIDEO -> "VIDEO";
            case AUDIO -> "AUDIO";
            case ARQUIVO -> "FILE";
            case SISTEMA -> "SYSTEM";
        };
    }

    @Override
    public TipoMensagem convertToEntityAttribute(String valor) {
        if (valor == null) return null;
        return switch (valor) {
            case "TEXT" -> TipoMensagem.TEXTO;
            case "IMAGE" -> TipoMensagem.IMAGEM;
            case "VIDEO" -> TipoMensagem.VIDEO;
            case "AUDIO" -> TipoMensagem.AUDIO;
            case "FILE" -> TipoMensagem.ARQUIVO;
            case "SYSTEM" -> TipoMensagem.SISTEMA;
            default -> throw new IllegalArgumentException("Tipo de mensagem desconhecido: " + valor);
        };
    }
}
