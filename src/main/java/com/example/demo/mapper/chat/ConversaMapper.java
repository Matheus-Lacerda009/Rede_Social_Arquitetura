package com.example.demo.mapper.chat;

import com.example.demo.dto.chat.ConversaResponseDTO;
import com.example.demo.dto.chat.MensagemResponseDTO;
import com.example.demo.dto.chat.ParticipanteResponseDTO;
import com.example.demo.model.chat.Conversa;
import com.example.demo.model.chat.ParticipanteConversa;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ConversaMapper {

    public ParticipanteResponseDTO toParticipanteResponse(ParticipanteConversa p) {
        return new ParticipanteResponseDTO(p.getUsuarioId(), p.getPapel(), p.getEntradaEm());
    }

    public List<ParticipanteResponseDTO> toParticipantesResponse(List<ParticipanteConversa> participantes) {
        return participantes.stream().map(this::toParticipanteResponse).toList();
    }

    public ConversaResponseDTO toResponse(Conversa c,
                                          List<ParticipanteConversa> participantes,
                                          MensagemResponseDTO ultimaMensagem,
                                          long naoLidas) {
        return new ConversaResponseDTO(
                c.getId(),
                c.getTipo(),
                c.getTitulo(),
                c.getCriadoPor(),
                c.getCriadoEm(),
                c.getUltimaAtividadeEm(),
                toParticipantesResponse(participantes),
                ultimaMensagem,
                naoLidas);
    }
}
