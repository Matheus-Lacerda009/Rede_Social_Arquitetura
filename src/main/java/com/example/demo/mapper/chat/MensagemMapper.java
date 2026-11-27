package com.example.demo.mapper.chat;

import com.example.demo.dto.chat.MensagemResponseDTO;
import com.example.demo.model.chat.Mensagem;
import org.springframework.stereotype.Component;

@Component
public class MensagemMapper {

    public MensagemResponseDTO toResponse(Mensagem m) {
        if (m == null) return null;
        boolean excluida = m.isExcluida();
        return new MensagemResponseDTO(
                m.getId(),
                m.getConversa().getId(),
                m.getRemetenteId(),
                m.getTipo(),
                excluida ? null : m.getConteudo(),
                m.getCriadaEm(),
                m.getEditadaEm(),
                excluida);
    }
}
