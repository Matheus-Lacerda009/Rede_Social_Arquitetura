# Demo — módulo de chat

Projeto Spring Boot 3.5.16 (Java 17) com o módulo de **chat** pronto para rodar sozinho ou junto com
outros módulos (usuarios, video), conforme o `CONTRATO-DE-MODULOS.md`.

## Como rodar (standalone)
1. Abra a pasta `demo` no IntelliJ (ou importe o `pom.xml`).
2. Rode `DemoApplication`.
3. API: http://localhost:8082 — identifique-se com o header `X-Usuario-Id: 1`.
4. Página de teste (duas janelas): http://localhost:8082/chat-teste.html
5. H2 console: http://localhost:8082/h2-console (JDBC URL `jdbc:h2:mem:chatdb`, user `sa`, senha vazia).

## Documentos
- `CONTRATO-DE-MODULOS.md` — regras obrigatórias para todos os módulos (use ao criar usuario, video, etc.).
- `LEIA-ME-CHAT.md` — endpoints, regras de negócio, WebSocket e integração com usuários.

## Integração
Não há dependência de compilação com o módulo de usuários. Veja a seção "Integração com o módulo de usuários"
em `LEIA-ME-CHAT.md` e o contrato na raiz.
