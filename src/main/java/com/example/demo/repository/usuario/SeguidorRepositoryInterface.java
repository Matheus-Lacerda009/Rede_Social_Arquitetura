package com.example.demo.repository.usuario;

import com.example.demo.model.usuario.Seguidor;
import com.example.demo.model.usuario.SeguidorId;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeguidorRepositoryInterface
        extends JpaRepository<Seguidor, SeguidorId> {

    @EntityGraph(attributePaths = "seguidor.perfilUsuario")
    List<Seguidor> findBySeguido_Id(Long usuarioId);

    @EntityGraph(attributePaths = "seguido.perfilUsuario")
    List<Seguidor> findBySeguidor_Id(Long usuarioId);

    boolean existsBySeguidor_IdAndSeguido_Id(
            Long seguidorId,
            Long seguidoId
    );

    long countBySeguidor_Id(Long usuarioId);

    long countBySeguido_Id(Long usuarioId);
}
