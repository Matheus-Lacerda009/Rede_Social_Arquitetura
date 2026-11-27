package com.example.demo.integracao;

import com.example.demo.service.chat.ConsultaUsuarioChat;
import com.example.demo.service.usuario.UsuarioService;
import org.springframework.stereotype.Component;

@Component
public class ChatUsuarioAdapter implements ConsultaUsuarioChat {

    private final UsuarioService usuarios;

    public ChatUsuarioAdapter(UsuarioService usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    public boolean existe(Long usuarioId) {
        return usuarios.existeAtivoPorId(usuarioId);
    }
}
