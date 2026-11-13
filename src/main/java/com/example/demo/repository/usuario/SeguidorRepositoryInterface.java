package com.example.demo.repository.usuario;

import com.example.demo.model.usuario.Seguidor;
import com.example.demo.model.usuario.SeguidorId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeguidorRepositoryInterface extends JpaRepository<Seguidor, SeguidorId> {

    List<Seguidor> findBySeguidorId(Long usuarioId);

    List<Seguidor> findBySeguidoId(Long usuarioId);

    boolean existsBySeguidorIdAndSeguidoId(
            Long seguidorId,
            Long seguidoId
    );

    long countBySeguidorId(Long usuarioId);

    long countBySeguidoId(Long usuarioId);
}
