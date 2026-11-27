package com.example.demo.common.handler;

import com.example.demo.common.exception.NaoAutenticadoException;
import com.example.demo.common.exception.RecursoNaoEncontradoException;
import com.example.demo.common.exception.RegraNegocioException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    public record ErroResponse(int status, String mensagem, List<String> detalhes, Instant timestamp) {
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> naoEncontrado(RecursoNaoEncontradoException ex) {
        return resposta(HttpStatus.NOT_FOUND, ex.getMessage(), List.of());
    }

    @ExceptionHandler(NaoAutenticadoException.class)
    public ResponseEntity<ErroResponse> naoAutenticado(NaoAutenticadoException ex) {
        return resposta(HttpStatus.UNAUTHORIZED, ex.getMessage(), List.of());
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponse> regraNegocio(RegraNegocioException ex) {
        return resposta(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> validacao(MethodArgumentNotValidException ex) {
        List<String> detalhes = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .toList();
        return resposta(HttpStatus.BAD_REQUEST, "Dados inválidos", detalhes);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErroResponse> headerAusente(MissingRequestHeaderException ex) {
        return resposta(HttpStatus.UNAUTHORIZED, "Header obrigatório ausente: " + ex.getHeaderName(), List.of());
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErroResponse> requisicaoInvalida(Exception ex) {
        return resposta(HttpStatus.BAD_REQUEST, "Requisição inválida", List.of());
    }

    private ResponseEntity<ErroResponse> resposta(HttpStatus status, String mensagem, List<String> detalhes) {
        return ResponseEntity.status(status).body(new ErroResponse(status.value(), mensagem, detalhes, Instant.now()));
    }
}
