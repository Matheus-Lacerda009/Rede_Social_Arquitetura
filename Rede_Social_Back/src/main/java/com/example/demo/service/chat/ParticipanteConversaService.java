package com.example.demo.service.chat;

import com.example.demo.common.exception.RecursoNaoEncontradoException;
import com.example.demo.common.exception.RegraNegocioException;
import com.example.demo.dto.chat.ParticipanteResponseDTO;
import com.example.demo.enums.chat.PapelParticipante;
import com.example.demo.enums.chat.TipoConversa;
import com.example.demo.mapper.chat.ConversaMapper;
import com.example.demo.model.chat.Conversa;
import com.example.demo.model.chat.ParticipanteConversa;
import com.example.demo.model.chat.ParticipanteConversaId;
import com.example.demo.repository.chat.ParticipanteConversaRepositoryInterface;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ParticipanteConversaService {

    private final ParticipanteConversaRepositoryInterface participantes;
    private final ConversaMapper mapper;
    private final ConsultaUsuarioChat consultaUsuario;

    public ParticipanteConversaService(ParticipanteConversaRepositoryInterface participantes,
                                       ConversaMapper mapper,
                                       ObjectProvider<ConsultaUsuarioChat> consultaUsuario) {
        this.participantes = participantes;
        this.mapper = mapper;
        this.consultaUsuario = consultaUsuario.getIfAvailable(() -> id -> true);
    }

    @Transactional(readOnly = true)
    public List<ParticipanteResponseDTO> listar(Long conversaId, Long solicitanteId) {
        exigirParticipanteAtivo(conversaId, solicitanteId);
        return mapper.toParticipantesResponse(participantes.listarAtivos(conversaId));
    }

    /** Ids dos participantes ativos (usado para saber quem deve ser notificado). */
    @Transactional(readOnly = true)
    public List<Long> idsAtivos(Long conversaId) {
        return participantes.listarAtivos(conversaId).stream().map(ParticipanteConversa::getUsuarioId).toList();
    }

    /**
     * Ponto único de controle de acesso do módulo: garante que o usuário participa da conversa.
     * Se não participa, responde como "não encontrada" para não revelar a existência da conversa.
     */
    @Transactional(readOnly = true)
    public ParticipanteConversa exigirParticipanteAtivo(Long conversaId, Long usuarioId) {
        return participantes.findById(new ParticipanteConversaId(conversaId, usuarioId))
                .filter(ParticipanteConversa::isAtivo)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conversa não encontrada"));
    }

    public ParticipanteResponseDTO adicionar(Long conversaId, Long solicitanteId, Long usuarioId) {
        ParticipanteConversa solicitante = exigirParticipanteAtivo(conversaId, solicitanteId);
        Conversa conversa = solicitante.getConversa();
        exigirGrupo(conversa);
        exigirAdmin(solicitante);
        if (!consultaUsuario.existe(usuarioId)) {
            throw new RegraNegocioException("Usuário " + usuarioId + " não encontrado");
        }

        Optional<ParticipanteConversa> existente = participantes.findById(new ParticipanteConversaId(conversaId, usuarioId));
        ParticipanteConversa novo;
        if (existente.isPresent()) {
            novo = existente.get();
            if (novo.isAtivo()) {
                throw new RegraNegocioException("Usuário já participa da conversa");
            }
            novo.reativar(PapelParticipante.MEMBRO);
        } else {
            novo = participantes.save(new ParticipanteConversa(conversa, usuarioId, PapelParticipante.MEMBRO));
        }
        return mapper.toParticipanteResponse(novo);
    }

    /** Admin remove qualquer participante; qualquer participante pode remover a si mesmo (sair do grupo). */
    public void remover(Long conversaId, Long solicitanteId, Long alvoId) {
        ParticipanteConversa solicitante = exigirParticipanteAtivo(conversaId, solicitanteId);
        exigirGrupo(solicitante.getConversa());

        boolean sairSozinho = solicitanteId.equals(alvoId);
        if (!sairSozinho) {
            exigirAdmin(solicitante);
        }
        ParticipanteConversa alvo = sairSozinho
                ? solicitante
                : participantes.findById(new ParticipanteConversaId(conversaId, alvoId))
                        .filter(ParticipanteConversa::isAtivo)
                        .orElseThrow(() -> new RecursoNaoEncontradoException("Participante não encontrado"));
        List<ParticipanteConversa> restantes = participantes.listarAtivos(conversaId).stream()
                .filter(p -> !p.getUsuarioId().equals(alvoId))
                .toList();
        if (restantes.isEmpty()) {
            throw new RegraNegocioException("O grupo precisa manter ao menos um participante ativo");
        }

        alvo.sair();

        // Se o grupo ficou sem admin, promove o participante mais antigo.
        boolean semAdmin = restantes.stream().noneMatch(p -> p.getPapel() == PapelParticipante.ADMIN);
        if (!restantes.isEmpty() && semAdmin) {
            restantes.get(0).setPapel(PapelParticipante.ADMIN);
        }
    }

    private void exigirGrupo(Conversa conversa) {
        if (conversa.getTipo() != TipoConversa.GRUPO) {
            throw new RegraNegocioException("Operação disponível apenas para conversas em grupo");
        }
    }

    private void exigirAdmin(ParticipanteConversa participante) {
        if (participante.getPapel() != PapelParticipante.ADMIN) {
            throw new RegraNegocioException("Apenas administradores do grupo podem realizar esta operação");
        }
    }
}
