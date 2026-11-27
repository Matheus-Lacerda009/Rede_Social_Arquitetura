package com.example.demo.websocket.chat;

import com.example.demo.dto.chat.NotificacaoChat;
import tools.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Guarda as conexões abertas (usuarioId -> conexões) e entrega os eventos do chat.
 * O cliente só RECEBE por aqui; para enviar/editar/excluir ele usa a API REST normal.
 * Um mesmo usuário pode ter várias conexões (várias abas/aparelhos).
 */
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    public static final String ATRIBUTO_USUARIO = "usuarioId";

    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketHandler.class);

    private final ConcurrentHashMap<Long, Set<WebSocketSession>> conexoes = new ConcurrentHashMap<>();
    private final ObjectMapper mapper;

    public ChatWebSocketHandler(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession sessao) {
        Long usuarioId = usuarioDe(sessao);
        if (usuarioId != null) {
            conexoes.computeIfAbsent(usuarioId, id -> ConcurrentHashMap.newKeySet()).add(sessao);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession sessao, CloseStatus status) {
        Long usuarioId = usuarioDe(sessao);
        if (usuarioId != null) {
            Set<WebSocketSession> abertas = conexoes.get(usuarioId);
            if (abertas != null) {
                abertas.remove(sessao);
                if (abertas.isEmpty()) {
                    conexoes.remove(usuarioId);
                }
            }
        }
    }

    /**
     * Chamado quando o service publica um evento. Só dispara DEPOIS que a transação foi confirmada,
     * então o destinatário nunca recebe aviso de algo que não foi salvo.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void aoNotificar(NotificacaoChat notificacao) {
        String json;
        try {
            json = mapper.writeValueAsString(notificacao.evento());
        } catch (Exception e) {
            log.warn("Não foi possível converter o evento do chat para JSON", e);
            return;
        }
        TextMessage texto = new TextMessage(json);
        for (Long destinatario : notificacao.destinatarios()) {
            Set<WebSocketSession> abertas = conexoes.get(destinatario);
            if (abertas == null) continue;
            for (WebSocketSession sessao : abertas) {
                enviar(sessao, texto);
            }
        }
    }

    private void enviar(WebSocketSession sessao, TextMessage texto) {
        try {
            if (sessao.isOpen()) {
                synchronized (sessao) {   // a sessão não aceita dois envios ao mesmo tempo
                    sessao.sendMessage(texto);
                }
            }
        } catch (Exception e) {
            log.warn("Falha ao enviar para uma conexão do chat; ela será ignorada", e);
        }
    }

    private Long usuarioDe(WebSocketSession sessao) {
        Object valor = sessao.getAttributes().get(ATRIBUTO_USUARIO);
        return valor instanceof Long id ? id : null;
    }
}
