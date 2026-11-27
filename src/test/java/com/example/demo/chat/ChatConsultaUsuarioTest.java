package com.example.demo.chat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import com.example.demo.service.chat.ConsultaUsuarioChat;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Mostra o chat recusando usuários inexistentes quando o sistema fornece uma ConsultaUsuarioChat. */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:chat-consulta-test;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ChatConsultaUsuarioTest {

    @TestConfiguration
    static class Config {
        @Bean
        @Primary
        ConsultaUsuarioChat consultaUsuarioChat() {
            return id -> id < 9000;   // ids a partir de 9000 "não existem"
        }
    }

    @Autowired
    MockMvc mvc;

    @Test
    void recusaUsuarioInexistenteEAceitaExistente() throws Exception {
        mvc.perform(post("/api/chat/conversas").header("X-Usuario-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipo\":\"INDIVIDUAL\",\"participantesIds\":[9500]}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensagem").value("Usuário 9500 não encontrado"));

        mvc.perform(post("/api/chat/conversas").header("X-Usuario-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipo\":\"INDIVIDUAL\",\"participantesIds\":[2]}"))
                .andExpect(status().isCreated());

        mvc.perform(post("/api/chat/conversas").header("X-Usuario-Id", 9500)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipo\":\"INDIVIDUAL\",\"participantesIds\":[2]}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensagem").value("Usuário 9500 não encontrado"));
    }
}
