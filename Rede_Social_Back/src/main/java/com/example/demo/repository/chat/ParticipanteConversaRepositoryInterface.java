package com.example.demo.repository.chat;

import com.example.demo.model.chat.ParticipanteConversa;
import com.example.demo.model.chat.ParticipanteConversaId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ParticipanteConversaRepositoryInterface extends JpaRepository<ParticipanteConversa, ParticipanteConversaId> {

    @Query("""
            select p from ParticipanteConversa p
            where p.id.conversaId = :conversaId and p.saiuEm is null
            order by p.entradaEm, p.id.usuarioId
            """)
    List<ParticipanteConversa> listarAtivos(@Param("conversaId") Long conversaId);
}
