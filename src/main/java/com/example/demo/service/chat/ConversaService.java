package com.example.demo.service.chat;

import com.example.demo.common.exception.RegraNegocioException;
import com.example.demo.dto.chat.ConversaCriacaoDTO;
import com.example.demo.dto.chat.ConversaResponseDTO;
import com.example.demo.enums.chat.PapelParticipante;
import com.example.demo.enums.chat.TipoConversa;
import com.example.demo.mapper.chat.ConversaMapper;
import com.example.demo.model.chat.Conversa;
import com.example.demo.model.chat.ParticipanteConversa;
import com.example.demo.repository.chat.ConversaRepositoryInterface;
import com.example.demo.repository.chat.ParticipanteConversaRepositoryInterface;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class ConversaService {

    private static final int TAMANHO_MAXIMO_PAGINA = 100;

    private final ConversaRepositoryInterface conversas;
    private final ParticipanteConversaRepositoryInterface participantes;
    private final ParticipanteConversaService participanteService;
    private final MensagemService mensagemService;
    private final LeituraMensagemService leituraService;
    private final ConversaMapper mapper;
    private final ConsultaUsuarioChat consultaUsuario;

    public ConversaService(ConversaRepositoryInterface conversas,
                           ParticipanteConversaRepositoryInterface participantes,
                           ParticipanteConversaService participanteService,
                           MensagemService mensagemService,
                           LeituraMensagemService leituraService,
                           ConversaMapper mapper,
                           ObjectProvider<ConsultaUsuarioChat> consultaUsuario) {
        this.conversas = conversas;
        this.participantes = participantes;
        this.participanteService = participanteService;
        this.mensagemService = mensagemService;
        this.leituraService = leituraService;
        this.mapper = mapper;
        this.consultaUsuario = consultaUsuario.getIfAvailable(() -> id -> true);
    }

    /**
     * Cria uma conversa. Para INDIVIDUAL, se já existir conversa entre os dois usuários, ela é reutilizada.
     */
    public ConversaResponseDTO criar(Long criadorId, ConversaCriacaoDTO dto) {
        Set<Long> outros = new LinkedHashSet<>(dto.participantesIds());
        outros.remove(criadorId);
        if (outros.isEmpty()) {
            throw new RegraNegocioException("Informe ao menos um participante além de você");
        }

        for (Long id : outros) {
            if (!consultaUsuario.existe(id)) {
                throw new RegraNegocioException("Usuário " + id + " não encontrado");
            }
        }

        if (dto.tipo() == TipoConversa.INDIVIDUAL) {
            if (outros.size() != 1) {
                throw new RegraNegocioException("Conversa individual deve ter exatamente um outro participante");
            }
            List<Conversa> existentes = conversas.buscarIndividualEntre(criadorId, outros.iterator().next());
            if (!existentes.isEmpty()) {
                return montarResposta(existentes.get(0), criadorId);
            }
        } else if (dto.titulo() == null || dto.titulo().isBlank()) {
            throw new RegraNegocioException("Título é obrigatório para conversas em grupo");
        }

        Conversa conversa = new Conversa();
        conversa.setTipo(dto.tipo());
        conversa.setTitulo(dto.tipo() == TipoConversa.GRUPO ? dto.titulo().strip() : null);
        conversa.setCriadoPor(criadorId);
        conversas.save(conversa);

        List<ParticipanteConversa> novos = new ArrayList<>();
        novos.add(new ParticipanteConversa(conversa, criadorId,
                dto.tipo() == TipoConversa.GRUPO ? PapelParticipante.ADMIN : PapelParticipante.MEMBRO));
        outros.forEach(id -> novos.add(new ParticipanteConversa(conversa, id, PapelParticipante.MEMBRO)));
        participantes.saveAll(novos);

        return montarResposta(conversa, criadorId);
    }

    @Transactional(readOnly = true)
    public ConversaResponseDTO buscar(Long conversaId, Long usuarioId) {
        ParticipanteConversa participante = participanteService.exigirParticipanteAtivo(conversaId, usuarioId);
        return montarResposta(participante.getConversa(), usuarioId);
    }

    /** Conversas do usuário, da mais recente para a mais antiga. Ordenação enviada pelo cliente é ignorada. */
    @Transactional(readOnly = true)
    public Page<ConversaResponseDTO> listar(Long usuarioId, Pageable pageable) {
        Pageable semOrdenacao = PageRequest.of(pageable.getPageNumber(),
                Math.min(pageable.getPageSize(), TAMANHO_MAXIMO_PAGINA));
        return conversas.listarDoUsuario(usuarioId, semOrdenacao)
                .map(c -> montarResposta(c, usuarioId));
    }

    private ConversaResponseDTO montarResposta(Conversa conversa, Long usuarioId) {
        return mapper.toResponse(
                conversa,
                participantes.listarAtivos(conversa.getId()),
                mensagemService.buscarUltima(conversa.getId()),
                leituraService.contarNaoLidas(conversa.getId(), usuarioId));
    }
}
