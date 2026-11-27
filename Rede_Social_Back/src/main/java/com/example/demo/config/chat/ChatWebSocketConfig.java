package com.example.demo.config.chat;

import com.example.demo.common.security.UsuarioIdResolver;
import com.example.demo.websocket.chat.ChatWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;
import java.util.Optional;

/**
 * Abre o endereço ws://servidor/ws/chat
 *
 * Quem é o usuário é decidido pelo UsuarioIdResolver do sistema (o mesmo usado pelo REST). Na versão de
 * desenvolvimento ele lê ?usuarioId=ID; quando o sistema tiver login com token, basta trocar o resolver
 * (ex.: ws/chat?token=...). Este arquivo não precisa mudar.
 */
@Configuration
@EnableWebSocket
public class ChatWebSocketConfig implements WebSocketConfigurer {

    private final ChatWebSocketHandler handler;
    private final UsuarioIdResolver resolver;

    public ChatWebSocketConfig(ChatWebSocketHandler handler, UsuarioIdResolver resolver) {
        this.handler = handler;
        this.resolver = resolver;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/ws/chat")
                .addInterceptors(new UsuarioHandshakeInterceptor(resolver))
                .setAllowedOriginPatterns("*");   // em produção, restrinja aos domínios do seu front-end
    }

    static class UsuarioHandshakeInterceptor implements HandshakeInterceptor {

        private final UsuarioIdResolver resolver;

        UsuarioHandshakeInterceptor(UsuarioIdResolver resolver) {
            this.resolver = resolver;
        }

        @Override
        public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                       WebSocketHandler wsHandler, Map<String, Object> atributos) {
            if (request instanceof ServletServerHttpRequest servlet) {
                Optional<Long> id = resolver.resolver(servlet.getServletRequest());
                if (id.isPresent()) {
                    atributos.put(ChatWebSocketHandler.ATRIBUTO_USUARIO, id.get());
                    return true;
                }
            }
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        @Override
        public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Exception exception) {
        }
    }
}
