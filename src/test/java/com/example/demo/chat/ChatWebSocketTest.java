package com.example.demo.chat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:chat-ws-test;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ChatWebSocketTest {

    @TestConfiguration
    static class ConfiguracaoTeste {
        @Bean
        @Primary
        com.example.demo.service.chat.ConsultaUsuarioChat consultaUsuarioChat() {
            return id -> true;
        }
    }


    @LocalServerPort
    int porta;

    @Autowired
    TestRestTemplate rest;

    private HttpEntity<Map<String, Object>> req(long usuario, Map<String, Object> corpo) {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.add("X-Usuario-Id", String.valueOf(usuario));
        return new HttpEntity<>(corpo, h);
    }

    private WebSocketSession conectar(long usuario, BlockingQueue<String> recebidas) throws Exception {
        return new StandardWebSocketClient().execute(new TextWebSocketHandler() {
            @Override
            protected void handleTextMessage(WebSocketSession s, TextMessage m) {
                recebidas.add(m.getPayload());
            }
        }, "ws://localhost:" + porta + "/ws/chat?usuarioId=" + usuario).get(5, TimeUnit.SECONDS);
    }

    @Test
    void destinatarioRecebeMensagemEmTempoReal() throws Exception {
        Map<?, ?> conversa = rest.exchange("/api/chat/conversas", HttpMethod.POST,
                req(5001, Map.of("tipo", "INDIVIDUAL", "participantesIds", List.of(5002))), Map.class).getBody();
        long conversaId = ((Number) conversa.get("id")).longValue();

        BlockingQueue<String> recebidasPelaB = new LinkedBlockingQueue<>();
        WebSocketSession b = conectar(5002, recebidasPelaB);

        Map<?, ?> mensagem = rest.exchange("/api/chat/conversas/" + conversaId + "/mensagens", HttpMethod.POST,
                req(5001, Map.of("conteudo", "ao vivo")), Map.class).getBody();
        long mensagemId = ((Number) mensagem.get("id")).longValue();

        String jsonNova = recebidasPelaB.poll(5, TimeUnit.SECONDS);
        assertNotNull(jsonNova, "O usuário B deveria ter recebido a mensagem pelo WebSocket");
        assertTrue(jsonNova.contains("MENSAGEM_NOVA"));
        assertTrue(jsonNova.contains("ao vivo"));

        rest.exchange("/api/chat/mensagens/" + mensagemId, HttpMethod.PUT,
                req(5001, Map.of("conteudo", "mensagem editada")), Map.class);
        String jsonEditada = recebidasPelaB.poll(5, TimeUnit.SECONDS);
        assertNotNull(jsonEditada, "O usuário B deveria receber o evento de edição");
        assertTrue(jsonEditada.contains("MENSAGEM_EDITADA"));
        assertTrue(jsonEditada.contains("mensagem editada"));

        rest.exchange("/api/chat/mensagens/" + mensagemId, HttpMethod.DELETE,
                req(5001, Map.of()), Map.class);
        String jsonExcluida = recebidasPelaB.poll(5, TimeUnit.SECONDS);
        assertNotNull(jsonExcluida, "O usuário B deveria receber o evento de exclusão");
        assertTrue(jsonExcluida.contains("MENSAGEM_EXCLUIDA"));
        b.close();
    }

    @Test
    void quemNaoParticipaNaoRecebeNada() throws Exception {
        Map<?, ?> conversa = rest.exchange("/api/chat/conversas", HttpMethod.POST,
                req(5101, Map.of("tipo", "INDIVIDUAL", "participantesIds", List.of(5102))), Map.class).getBody();
        long conversaId = ((Number) conversa.get("id")).longValue();

        BlockingQueue<String> recebidasPeloEstranho = new LinkedBlockingQueue<>();
        WebSocketSession estranho = conectar(5199, recebidasPeloEstranho);

        rest.exchange("/api/chat/conversas/" + conversaId + "/mensagens", HttpMethod.POST,
                req(5101, Map.of("conteudo", "segredo")), Map.class);

        assertNull(recebidasPeloEstranho.poll(2, TimeUnit.SECONDS));
        estranho.close();
    }

    @Test
    void conexaoSemUsuarioERecusada() {
        assertThrows(ExecutionException.class, () ->
                new StandardWebSocketClient().execute(new TextWebSocketHandler(),
                        "ws://localhost:" + porta + "/ws/chat").get(5, TimeUnit.SECONDS));
    }
}
