package com.example.demo.service.usuario;

import com.example.demo.dto.usuario.UsuarioCadastroDTO;
import com.example.demo.dto.usuario.UsuarioLoginDTO;
import com.example.demo.dto.usuario.UsuarioResponseDTO;

import com.example.demo.mapper.usuario.UsuarioMapper;
import com.example.demo.model.usuario.PerfilUsuario;
import com.example.demo.model.usuario.Usuario;
import com.example.demo.repository.usuario.UsuarioRepositoryInterface;

import com.example.demo.common.exception.RegraNegocioException;
import com.example.demo.common.exception.RecursoNaoEncontradoException;
import com.example.demo.enums.usuario.StatusUsuario;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import com.example.demo.repository.usuario.PerfilUsuarioRepositoryInterface;

@Service
public class UsuarioService {

    private final UsuarioRepositoryInterface usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;
    private final PerfilUsuarioRepositoryInterface perfilUsuarioRepository;

    public UsuarioService(
            UsuarioRepositoryInterface usuarioRepository,
            UsuarioMapper usuarioMapper,
            PasswordEncoder passwordEncoder,
            PerfilUsuarioRepositoryInterface perfilUsuarioRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioMapper = usuarioMapper;
        this.passwordEncoder = passwordEncoder;
        this.perfilUsuarioRepository = perfilUsuarioRepository;
    }

    @Transactional
    public UsuarioResponseDTO cadastrar(UsuarioCadastroDTO dto) {

        if (usuarioRepository.existsByUsername(dto.username())) {
            throw new RegraNegocioException("Username já cadastrado.");
        }

        if (usuarioRepository.existsByEmail(dto.email())) {
            throw new RegraNegocioException("Email já cadastrado.");
        }

        Usuario usuario = usuarioMapper.paraEntidade(dto);

        usuario.setSenha(passwordEncoder.encode(dto.senha()));

        usuario = usuarioRepository.save(usuario);

        PerfilUsuario perfil = new PerfilUsuario();
        perfil.setUsuario(usuario);
        perfil.setNomeExibicao(usuario.getUsername());

        usuario.setPerfilUsuario(perfil);
        perfilUsuarioRepository.save(perfil);

        return usuarioMapper.paraResponseDTO(usuario);
    }

    @Transactional
    public UsuarioResponseDTO login(UsuarioLoginDTO dto) {

        Usuario usuario = usuarioRepository
                .findByUsername(dto.identificador())
                .or(() -> usuarioRepository.findByEmail(dto.identificador()))
                .orElseThrow(() ->
                        new RegraNegocioException("Credenciais inválidas.")
                );

        if (!passwordEncoder.matches(dto.senha(), usuario.getSenha())) {
            throw new RegraNegocioException("Credenciais inválidas.");
        }

        if (usuario.getStatus() != StatusUsuario.ACTIVE) {
            throw new RegraNegocioException("Esta conta não está ativa.");
        }

        usuario.setUltimoLogin(LocalDateTime.now());
        usuarioRepository.save(usuario);

        return usuarioMapper.paraResponseDTO(usuario);
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorId(Long id) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Usuário não encontrado.")
                );

        return usuarioMapper.paraResponseDTO(usuario);
    }
}
