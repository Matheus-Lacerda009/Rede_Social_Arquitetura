package com.example.demo.repository.chat;

import com.example.demo.model.chat.Mensagem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MensagemRepositoryInterface extends JpaRepository<Mensagem, Long> {

    Page<Mensagem> findByConversaIdOrderByCriadaEmDescIdDesc(Long conversaId, Pageable pageable);

    Optional<Mensagem> findFirstByConversaIdAndExcluidaEmIsNullOrderByCriadaEmDescIdDesc(Long conversaId);

    /** Mensagens de outros usuários, não excluídas, ainda não lidas por usuarioId. */
    @Query("""
            select m from Mensagem m
            where m.conversa.id = :conversaId
              and (m.remetenteId is null or m.remetenteId <> :usuarioId)
              and m.excluidaEm is null
              and not exists (select 1 from LeituraMensagem l
                              where l.id.mensagemId = m.id and l.id.usuarioId = :usuarioId)
            order by m.id
            """)
    List<Mensagem> listarNaoLidas(@Param("conversaId") Long conversaId, @Param("usuarioId") Long usuarioId);

    @Query("""
            select count(m) from Mensagem m
            where m.conversa.id = :conversaId
              and (m.remetenteId is null or m.remetenteId <> :usuarioId)
              and m.excluidaEm is null
              and not exists (select 1 from LeituraMensagem l
                              where l.id.mensagemId = m.id and l.id.usuarioId = :usuarioId)
            """)
    long contarNaoLidas(@Param("conversaId") Long conversaId, @Param("usuarioId") Long usuarioId);
}
