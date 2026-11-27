package com.example.demo.service.chat;

import com.example.demo.common.exception.RecursoNaoEncontradoException;
import com.example.demo.model.chat.LeituraMensagem;
import com.example.demo.model.chat.LeituraMensagemId;
import com.example.demo.model.chat.Mensagem;
import com.example.demo.repository.chat.LeituraMensagemRepositoryInterface;
import com.example.demo.repository.chat.MensagemRepositoryInterface;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class LeituraMensagemService {

    private final LeituraMensagemRepositoryInterface leituras;
    private final MensagemRepositoryInterface mensagens;
    private final ParticipanteConversaService participanteService;

    public LeituraMensagemService(LeituraMensagemRepositoryInterface leituras,
                                  MensagemRepositoryInterface mensagens,
                                  ParticipanteConversaService participanteService) {
        this.leituras = leituras;
        this.mensagens = mensagens;
        this.participanteService = participanteService;
    }

    /** Idempotente. Mensagens próprias ou excluídas são ignoradas. */
    public void marcarMensagemComoLida(Long mensagemId, Long usuarioId) {
        Mensagem mensagem = mensagens.findById(mensagemId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Mensagem não encontrada"));
        participanteService.exigirParticipanteAtivo(mensagem.getConversa().getId(), usuarioId);

        if (mensagem.isExcluida() || Objects.equals(mensagem.getRemetenteId(), usuarioId)) {
            return;
        }
        if (!leituras.existsById(new LeituraMensagemId(mensagemId, usuarioId))) {
            leituras.save(new LeituraMensagem(mensagem, usuarioId));
        }
    }

    /** Marca todas as mensagens pendentes da conversa como lidas e retorna quantas foram marcadas. */
    public int marcarConversaComoLida(Long conversaId, Long usuarioId) {
        participanteService.exigirParticipanteAtivo(conversaId, usuarioId);
        List<Mensagem> pendentes = mensagens.listarNaoLidas(conversaId, usuarioId);
        leituras.saveAll(pendentes.stream().map(m -> new LeituraMensagem(m, usuarioId)).toList());
        return pendentes.size();
    }

    @Transactional(readOnly = true)
    public long contarNaoLidas(Long conversaId, Long usuarioId) {
        participanteService.exigirParticipanteAtivo(conversaId, usuarioId);
        return mensagens.contarNaoLidas(conversaId, usuarioId);
    }
}
