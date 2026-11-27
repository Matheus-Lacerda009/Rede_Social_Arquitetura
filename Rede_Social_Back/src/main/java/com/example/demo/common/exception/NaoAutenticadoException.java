package com.example.demo.common.exception;

/** Requisição sem identificação válida do usuário (vira HTTP 401). */
public class NaoAutenticadoException extends RuntimeException {
    public NaoAutenticadoException(String mensagem) {
        super(mensagem);
    }
}
