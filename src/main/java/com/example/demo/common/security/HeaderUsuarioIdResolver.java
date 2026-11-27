package com.example.demo.common.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Implementação de DESENVOLVIMENTO: confia no header X-Usuario-Id (ou no parâmetro ?usuarioId=,
 * usado pelo WebSocket, que no navegador não envia headers).
 *
 * Qualquer um pode se passar por outro usuário: não use em produção.
 *
 * Só é registrado se NÃO existir outro UsuarioIdResolver no contexto
 * ({@code @ConditionalOnMissingBean}). Assim o módulo de usuários pode fornecer
 * uma implementação baseada em JWT com {@code @Component} (e opcionalmente {@code @Primary})
 * sem conflito de beans.
 */
@Component
@ConditionalOnMissingBean(UsuarioIdResolver.class)
public class HeaderUsuarioIdResolver implements UsuarioIdResolver {

    public static final String HEADER = "X-Usuario-Id";
    public static final String PARAMETRO = "usuarioId";

    @Override
    public Optional<Long> resolver(HttpServletRequest requisicao) {
        String valor = requisicao.getHeader(HEADER);
        if (valor == null || valor.isBlank()) {
            valor = requisicao.getParameter(PARAMETRO);
        }
        if (valor == null || valor.isBlank()) {
            return Optional.empty();
        }
        try {
            long id = Long.parseLong(valor.trim());
            return id > 0 ? Optional.of(id) : Optional.empty();
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
