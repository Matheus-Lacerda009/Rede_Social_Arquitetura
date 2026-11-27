
package com.example.demo.common.handler;

import com.example.demo.common.exception.RegraNegocioException;
import com.example.demo.common.exception.RecursoNaoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.jspecify.annotations.NonNull;

import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<@NonNull String> tratarRegraNegocio(
            RegraNegocioException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(exception.getMessage());
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<@NonNull String> tratarRecursoNaoEncontrado(
            RecursoNaoEncontradoException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<@NonNull String> tratarErroInterno(
            Exception exception
    ) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Ocorreu um erro interno no servidor.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<@NonNull Map<String, String>> tratarErrosValidacao(
            MethodArgumentNotValidException exception
    ) {
        Map<String, String> erros = new LinkedHashMap<>();

        exception.getBindingResult().getFieldErrors().forEach(erro -> {
            String mensagem = erro.getDefaultMessage() != null
                    ? erro.getDefaultMessage()
                    : "Valor inválido.";

            erros.put(erro.getField(), mensagem);
        });

        return ResponseEntity.badRequest().body(erros);
    }

}
