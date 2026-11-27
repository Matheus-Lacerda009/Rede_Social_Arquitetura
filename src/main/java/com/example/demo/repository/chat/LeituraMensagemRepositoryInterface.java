package com.example.demo.repository.chat;

import com.example.demo.model.chat.LeituraMensagem;
import com.example.demo.model.chat.LeituraMensagemId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeituraMensagemRepositoryInterface extends JpaRepository<LeituraMensagem, LeituraMensagemId> {
}
