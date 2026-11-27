package com.example.demo.service.chat;

import com.example.demo.common.exception.RecursoNaoEncontradoException;
import com.example.demo.common.exception.RegraNegocioException;
import com.example.demo.dto.chat.MensagemAtualizacaoDTO;
import com.example.demo.dto.chat.MensagemEnvioDTO;
import com.example.demo.dto.chat.MensagemEventoDTO;
import com.example.demo.dto.chat.MensagemResponseDTO;
import com.example.demo.dto.chat.NotificacaoChat;
import com.example.demo.enums.chat.PapelParticipante;
import com.example.demo.enums.chat.TipoMensagem;
import com.example.demo.mapper.chat.MensagemMapper;
import com.example.demo.model.chat.Conversa;
import com.example.demo.model.chat.Mensagem;
import com.example.demo.model.chat.ParticipanteConversa;
import com.example.demo.repository.chat.MensagemRepositoryInterface;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional
public class MensagemService {

    private static final int TAMANHO_MAXIMO_PAGINA = 100;

    private final MensagemRepositoryInterface mensagens;
    private final ParticipanteConversaService participanteService;
    private final MensagemMapper mapper;
    private final ApplicationEventPublisher eventos;

    public MensagemService(MensagemRepositoryInterface mensagens,
                           ParticipanteConversaService participanteService,
                           MensagemMapper mapper,
                           ApplicationEventPublisher eventos) {
        this.mensagens = mensagens;
        this.participanteService = participanteService;
        this.mapper = mapper;
        this.eventos = eventos;
    }

    public MensagemResponseDTO enviar(Long conversaId, Long remetenteId, MensagemEnvioDTO dto) {
        ParticipanteConversa participante = participanteService.exigirParticipanteAtivo(conversaId, remetenteId);
        Conversa conversa = participante.getConversa();

        Mensagem mensagem = new Mensagem();
        mensagem.setConversa(conversa);
        mensagem.setRemetenteId(remetenteId);
        TipoMensagem tipo = dto.tipo() == null ? TipoMensagem.TEXTO : dto.tipo();
        if (tipo == TipoMensagem.SISTEMA) {
            throw new RegraNegocioException("Mensagens de sistema não podem ser enviadas por usuários");
        }
        mensagem.setTipo(tipo);
        mensagem.setConteudo(dto.conteudo().strip());
        mensagens.save(mensagem);

        conversa.setUltimaAtividadeEm(mensagem.getCriadaEm());
        MensagemResponseDTO resposta = mapper.toResponse(mensagem);
        notificar(MensagemEventoDTO.NOVA, conversaId, resposta);
        return resposta;
    }

    /** Mais recentes primeiro. Ordenação enviada pelo cliente é ignorada. */
    @Transactional(readOnly = true)
    public Page<MensagemResponseDTO> listar(Long conversaId, Long usuarioId, Pageable pageable) {
        participanteService.exigirParticipanteAtivo(conversaId, usuarioId);
        Pageable semOrdenacao = PageRequest.of(pageable.getPageNumber(),
                Math.max(1, Math.min(pageable.getPageSize(), TAMANHO_MAXIMO_PAGINA)));
        return mensagens.findByConversaIdOrderByCriadaEmDescIdDesc(conversaId, semOrdenacao)
                .map(mapper::toResponse);
    }

    public MensagemResponseDTO editar(Long mensagemId, Long usuarioId, MensagemAtualizacaoDTO dto) {
        Mensagem mensagem = buscarEntidade(mensagemId);
        participanteService.exigirParticipanteAtivo(mensagem.getConversa().getId(), usuarioId);

        if (!Objects.equals(mensagem.getRemetenteId(), usuarioId)) {
            throw new RegraNegocioException("Apenas o autor pode editar a mensagem");
        }
        if (mensagem.isExcluida()) {
            throw new RegraNegocioException("Mensagem excluída não pode ser editada");
        }
        if (mensagem.getTipo() != TipoMensagem.TEXTO) {
            throw new RegraNegocioException("Apenas mensagens de texto podem ser editadas");
        }
        mensagem.editar(dto.conteudo().strip());
        MensagemResponseDTO resposta = mapper.toResponse(mensagem);
        notificar(MensagemEventoDTO.EDITADA, mensagem.getConversa().getId(), resposta);
        return resposta;
    }

    /** Exclusão lógica. O autor ou um ADMIN do grupo pode excluir. Idempotente. */
    public void excluir(Long mensagemId, Long usuarioId) {
        Mensagem mensagem = buscarEntidade(mensagemId);
        ParticipanteConversa participante =
                participanteService.exigirParticipanteAtivo(mensagem.getConversa().getId(), usuarioId);

        boolean autor = Objects.equals(mensagem.getRemetenteId(), usuarioId);
        if (!autor && participante.getPapel() != PapelParticipante.ADMIN) {
            throw new RegraNegocioException("Sem permissão para excluir esta mensagem");
        }
        if (!mensagem.isExcluida()) {
            mensagem.excluir();
            notificar(MensagemEventoDTO.EXCLUIDA, mensagem.getConversa().getId(), mapper.toResponse(mensagem));
        }
    }

    /** Uso interno (sem checagem de acesso): última mensagem visível da conversa, ou null. */
    @Transactional(readOnly = true)
    public MensagemResponseDTO buscarUltima(Long conversaId) {
        return mensagens.findFirstByConversaIdAndExcluidaEmIsNullOrderByCriadaEmDescIdDesc(conversaId)
                .map(mapper::toResponse)
                .orElse(null);
    }

    /** Publica o evento; a entrega em tempo real acontece fora do service (ver websocket/chat). */
    private void notificar(String tipo, Long conversaId, MensagemResponseDTO mensagem) {
        eventos.publishEvent(new NotificacaoChat(
                participanteService.idsAtivos(conversaId),
                new MensagemEventoDTO(tipo, conversaId, mensagem)));
    }

    private Mensagem buscarEntidade(Long mensagemId) {
        return mensagens.findById(mensagemId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Mensagem não encontrada"));
    }
}
