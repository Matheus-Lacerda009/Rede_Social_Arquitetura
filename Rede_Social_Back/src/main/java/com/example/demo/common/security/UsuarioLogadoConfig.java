package com.example.demo.common.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/** Registra o @UsuarioLogado no Spring MVC. Não precisa ser alterado. */
@Configuration
public class UsuarioLogadoConfig implements WebMvcConfigurer {

    private final UsuarioIdResolver resolver;

    public UsuarioLogadoConfig(UsuarioIdResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new UsuarioLogadoArgumentResolver(resolver));
    }
}
