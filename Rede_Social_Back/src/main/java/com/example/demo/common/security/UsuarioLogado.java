package com.example.demo.common.security;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use em um parâmetro Long de qualquer controller para receber o id do usuário logado:
 *
 *   public ... listar(@UsuarioLogado Long usuarioId) { ... }
 *
 * Se não houver usuário identificado, a resposta é 401 automaticamente.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UsuarioLogado {
}
