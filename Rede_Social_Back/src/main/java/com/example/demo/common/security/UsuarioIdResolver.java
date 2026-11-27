package com.example.demo.common.security;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;

/**
 * CONTRATO: "dada uma requisição, qual é o id do usuário logado?".
 * É o único ponto em que o sistema decide como o usuário é identificado.
 * Todos os módulos (chat, vídeo, ...) usam @UsuarioLogado e nunca leem header/token por conta própria.
 *
 * Implementação padrão (desenvolvimento): HeaderUsuarioIdResolver.
 * Para usar login com token (JWT): crie uma classe que implemente esta interface, anote com
 * @Component e @Primary, e pronto. Nenhum módulo precisa mudar.
 */
public interface UsuarioIdResolver {

    Optional<Long> resolver(HttpServletRequest requisicao);
}
