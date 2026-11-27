package com.example.demo.common.security;

import com.example.demo.common.exception.NaoAutenticadoException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/** Preenche os parâmetros marcados com @UsuarioLogado. */
public class UsuarioLogadoArgumentResolver implements HandlerMethodArgumentResolver {

    private final UsuarioIdResolver resolver;

    public UsuarioLogadoArgumentResolver(UsuarioIdResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public boolean supportsParameter(MethodParameter parametro) {
        return parametro.hasParameterAnnotation(UsuarioLogado.class)
                && Long.class.equals(parametro.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parametro, ModelAndViewContainer container,
                                  NativeWebRequest requisicao, WebDataBinderFactory fabrica) {
        HttpServletRequest http = requisicao.getNativeRequest(HttpServletRequest.class);
        if (http == null) {
            throw new NaoAutenticadoException("Usuário não identificado");
        }
        return resolver.resolver(http)
                .orElseThrow(() -> new NaoAutenticadoException("Usuário não identificado"));
    }
}
