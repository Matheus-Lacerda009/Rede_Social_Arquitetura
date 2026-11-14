package com.example.demo.service.usuario;

import com.example.demo.dto.usuario.UsuarioCadastroDTO;
import com.example.demo.dto.usuario.UsuarioLoginDTO;
import com.example.demo.dto.usuario.UsuarioResponseDTO;

import com.example.demo.mapper.usuario.UsuarioMapper;
import com.example.demo.model.usuario.Usuario;
import com.example.demo.repository.usuario.UsuarioRepositoryInterface;

import com.example.demo.common.exception.RegraNegocioException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepositoryInterface usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepositoryInterface usuarioRepository,
            UsuarioMapper usuarioMapper,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioMapper = usuarioMapper;
        this.passwordEncoder = passwordEncoder;
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

        Usuario usuarioSalvo = usuarioRepository.save(usuario);

        return usuarioMapper.paraResponseDTO(usuarioSalvo);
    }

    @Transactional(readOnly = true)
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

        return usuarioMapper.paraResponseDTO(usuario);
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorId(Long id) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new RegraNegocioException("Usuário não encontrado.")
                );

        return usuarioMapper.paraResponseDTO(usuario);
    }
}
