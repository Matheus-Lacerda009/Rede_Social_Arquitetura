package com.example.demo.chat;

import com.jayway.jsonpath.JsonPath;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:chat-test;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ChatApiTest {

    @TestConfiguration
    static class ConfiguracaoTeste {
        @Bean
        @Primary
        com.example.demo.service.chat.ConsultaUsuarioChat consultaUsuarioChat() {
            return id -> true;
        }
    }


    @Autowired
    MockMvc mvc;

    private ResultActions chamar(MockHttpServletRequestBuilder req, Long usuario, String json) throws Exception {
        if (usuario != null) req.header("X-Usuario-Id", usuario);
        if (json != null) req.contentType(MediaType.APPLICATION_JSON).content(json);
        return mvc.perform(req);
    }

    private long id(ResultActions r) throws Exception {
        return ((Number) JsonPath.read(r.andReturn().getResponse().getContentAsString(), "$.id")).longValue();
    }

    @Test
    void fluxoConversaIndividualComMensagensELeitura() throws Exception {
        // Usuários 101 e 102 (ids soltos, sem módulo de usuários)
        long conversaId = id(chamar(post("/api/chat/conversas"), 101L,
                "{\"tipo\":\"INDIVIDUAL\",\"participantesIds\":[102]}").andExpect(status().isCreated()));

        // idempotente: mesma conversa na direção oposta
        long mesma = id(chamar(post("/api/chat/conversas"), 102L,
                "{\"tipo\":\"INDIVIDUAL\",\"participantesIds\":[101]}"));
        org.junit.jupiter.api.Assertions.assertEquals(conversaId, mesma);

        long m1 = id(chamar(post("/api/chat/conversas/" + conversaId + "/mensagens"), 101L,
                "{\"conteudo\":\"  Oi!  \"}").andExpect(status().isCreated())
                .andExpect(jsonPath("$.conteudo").value("Oi!"))
                .andExpect(jsonPath("$.tipo").value("TEXTO")));
        chamar(post("/api/chat/conversas/" + conversaId + "/mensagens"), 101L, "{\"conteudo\":\"Tudo bem?\"}");

        // 102 tem 2 não lidas; 101 tem 0
        chamar(get("/api/chat/conversas/" + conversaId), 102L, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.naoLidas").value(2))
                .andExpect(jsonPath("$.ultimaMensagem.conteudo").value("Tudo bem?"))
                .andExpect(jsonPath("$.participantes.length()").value(2));
        chamar(get("/api/chat/conversas/" + conversaId), 101L, null)
                .andExpect(jsonPath("$.naoLidas").value(0));

        // marca uma como lida (idempotente), depois a conversa inteira
        chamar(post("/api/chat/mensagens/" + m1 + "/leitura"), 102L, null).andExpect(status().isNoContent());
        chamar(post("/api/chat/mensagens/" + m1 + "/leitura"), 102L, null).andExpect(status().isNoContent());
        chamar(post("/api/chat/conversas/" + conversaId + "/leitura"), 102L, null)
                .andExpect(jsonPath("$.marcadas").value(1));
        chamar(get("/api/chat/conversas/" + conversaId), 102L, null).andExpect(jsonPath("$.naoLidas").value(0));

        // edição: só o autor
        chamar(put("/api/chat/mensagens/" + m1), 101L, "{\"conteudo\":\"Olá!\"}")
                .andExpect(status().isOk()).andExpect(jsonPath("$.conteudo").value("Olá!"))
                .andExpect(jsonPath("$.editadaEm").exists());
        chamar(put("/api/chat/mensagens/" + m1), 102L, "{\"conteudo\":\"hack\"}")
                .andExpect(status().isUnprocessableEntity());

        // exclusão lógica
        chamar(delete("/api/chat/mensagens/" + m1), 101L, null).andExpect(status().isNoContent());
        chamar(get("/api/chat/conversas/" + conversaId + "/mensagens"), 102L, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[1].excluida").value(true))
                .andExpect(jsonPath("$.content[1].conteudo").value(org.hamcrest.Matchers.nullValue()));

        // listagem de conversas do usuário
        chamar(get("/api/chat/conversas"), 101L, null)
                .andExpect(status().isOk()).andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void estranhoNaoAcessaConversa() throws Exception {
        long conversaId = id(chamar(post("/api/chat/conversas"), 201L,
                "{\"tipo\":\"INDIVIDUAL\",\"participantesIds\":[202]}"));
        chamar(get("/api/chat/conversas/" + conversaId), 999L, null).andExpect(status().isNotFound());
        chamar(post("/api/chat/conversas/" + conversaId + "/mensagens"), 999L, "{\"conteudo\":\"x\"}")
                .andExpect(status().isNotFound());
        chamar(get("/api/chat/conversas/" + conversaId + "/mensagens"), 999L, null).andExpect(status().isNotFound());
    }

    @Test
    void grupoComPapeisEPromocaoAutomatica() throws Exception {
        long g = id(chamar(post("/api/chat/conversas"), 301L,
                "{\"tipo\":\"GRUPO\",\"titulo\":\"Time\",\"participantesIds\":[302,303,301]}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo").value("Time"))
                .andExpect(jsonPath("$.participantes.length()").value(3)));
        String base = "/api/chat/conversas/" + g + "/participantes";

        // membro não pode adicionar/remover outros
        chamar(post(base + "/304"), 302L, null).andExpect(status().isUnprocessableEntity());
        chamar(delete(base + "/303"), 302L, null).andExpect(status().isUnprocessableEntity());

        // admin adiciona; duplicado é rejeitado
        chamar(post(base + "/304"), 301L, null).andExpect(status().isCreated())
                .andExpect(jsonPath("$.papel").value("MEMBRO"));
        chamar(post(base + "/304"), 301L, null).andExpect(status().isUnprocessableEntity());

        // admin sai -> outro vira ADMIN
        chamar(delete(base + "/301"), 301L, null).andExpect(status().isNoContent());
        chamar(get(base), 302L, null).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].usuarioId").value(302))
                .andExpect(jsonPath("$[0].papel").value("ADMIN"));

        // quem saiu perde o acesso; admin pode readicionar
        chamar(get("/api/chat/conversas/" + g), 301L, null).andExpect(status().isNotFound());
        chamar(post(base + "/301"), 302L, null).andExpect(status().isCreated());
        chamar(get("/api/chat/conversas/" + g), 301L, null).andExpect(status().isOk());

        // admin pode excluir mensagem de outro
        long m = id(chamar(post("/api/chat/conversas/" + g + "/mensagens"), 303L, "{\"conteudo\":\"spam\"}"));
        chamar(delete("/api/chat/mensagens/" + m), 301L, null).andExpect(status().isUnprocessableEntity()); // 301 voltou como MEMBRO
        chamar(delete("/api/chat/mensagens/" + m), 302L, null).andExpect(status().isNoContent());
    }

    @Test
    void grupoNaoPodeFicarSemParticipantesAtivos() throws Exception {
        long grupoId = id(chamar(post("/api/chat/conversas"), 601L,
                "{\"tipo\":\"GRUPO\",\"titulo\":\"Grupo com dois\",\"participantesIds\":[602]}"));
        String base = "/api/chat/conversas/" + grupoId + "/participantes";

        // O criador sai e o único outro participante é promovido a ADMIN.
        chamar(delete(base + "/601"), 601L, null).andExpect(status().isNoContent());
        chamar(get(base), 602L, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].papel").value("ADMIN"));

        // O último participante não pode deixar o grupo sem ninguém que consiga administrá-lo.
        chamar(delete(base + "/602"), 602L, null).andExpect(status().isUnprocessableEntity());
        chamar(get("/api/chat/conversas/" + grupoId), 602L, null).andExpect(status().isOk());
    }

    @Test
    void usuarioDeSistemaNaoPodeSerEnviadoPeloCliente() throws Exception {
        long conversaId = id(chamar(post("/api/chat/conversas"), 701L,
                "{\"tipo\":\"INDIVIDUAL\",\"participantesIds\":[702]}"));
        chamar(post("/api/chat/conversas/" + conversaId + "/mensagens"), 701L,
                "{\"tipo\":\"SISTEMA\",\"conteudo\":\"mensagem interna\"}")
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void validacoes() throws Exception {
        chamar(post("/api/chat/conversas"), 1L, "{\"tipo\":\"GRUPO\",\"participantesIds\":[2]}")
                .andExpect(status().isUnprocessableEntity());                      // grupo sem título
        chamar(post("/api/chat/conversas"), 1L, "{\"tipo\":\"INDIVIDUAL\",\"participantesIds\":[2,3]}")
                .andExpect(status().isUnprocessableEntity());                      // individual com 2 outros
        chamar(post("/api/chat/conversas"), 1L, "{\"tipo\":\"INDIVIDUAL\",\"participantesIds\":[1]}")
                .andExpect(status().isUnprocessableEntity());                      // consigo mesmo
        chamar(post("/api/chat/conversas"), 1L, "{\"participantesIds\":[]}")
                .andExpect(status().isBadRequest());                               // bean validation
        chamar(post("/api/chat/conversas"), null, "{\"tipo\":\"INDIVIDUAL\",\"participantesIds\":[2]}")
                .andExpect(status().isUnauthorized());                             // sem header
        long c = id(chamar(post("/api/chat/conversas"), 1L, "{\"tipo\":\"INDIVIDUAL\",\"participantesIds\":[2]}"));
        chamar(post("/api/chat/conversas/" + c + "/mensagens"), 1L, "{\"conteudo\":\"   \"}")
                .andExpect(status().isBadRequest());                               // mensagem vazia
        chamar(post("/api/chat/conversas/" + c + "/participantes/5"), 1L, null)
                .andExpect(status().isUnprocessableEntity());                      // individual não aceita add
    }
}
