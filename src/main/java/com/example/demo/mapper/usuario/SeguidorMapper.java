package com.example.demo.mapper.usuario;

import com.example.demo.dto.usuario.UsuarioResumoDTO;
import com.example.demo.mapper.usuario.UsuarioMapper;
import com.example.demo.model.usuario.Seguidor;

import org.springframework.stereotype.Component;

@Component
public class SeguidorMapper {

    private final UsuarioMapper usuarioMapper;

    public SeguidorMapper(UsuarioMapper usuarioMapper) {
        this.usuarioMapper = usuarioMapper;
    }

    public UsuarioResumoDTO paraDTO(Seguidor seguidor) {
        return usuarioMapper.paraResumoDTO(
                seguidor.getSeguidor(),
                null
        );
    }
}