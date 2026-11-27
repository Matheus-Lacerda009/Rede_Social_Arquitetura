package com.example.demo.repository.chat;

import com.example.demo.model.chat.Conversa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ConversaRepositoryInterface extends JpaRepository<Conversa, Long> {

    @Query(value = """
            select c from Conversa c
            where exists (select 1 from ParticipanteConversa p
                          where p.conversa = c and p.id.usuarioId = :usuarioId and p.saiuEm is null)
            order by c.ultimaAtividadeEm desc, c.id desc
            """,
            countQuery = """
            select count(c) from Conversa c
            where exists (select 1 from ParticipanteConversa p
                          where p.conversa = c and p.id.usuarioId = :usuarioId and p.saiuEm is null)
            """)
    Page<Conversa> listarDoUsuario(@Param("usuarioId") Long usuarioId, Pageable pageable);

    /** Conversas INDIVIDUAIS que têm exatamente os dois usuários como participantes. */
    @Query("""
            select c from Conversa c
            where c.tipo = com.example.demo.enums.chat.TipoConversa.INDIVIDUAL
              and exists (select 1 from ParticipanteConversa p1 where p1.conversa = c and p1.id.usuarioId = :a)
              and exists (select 1 from ParticipanteConversa p2 where p2.conversa = c and p2.id.usuarioId = :b)
            order by c.id
            """)
    List<Conversa> buscarIndividualEntre(@Param("a") Long a, @Param("b") Long b);
}
