# Módulo de Chat (MVC, independente)

Pacote base: `com.example.demo` — camadas `model`, `dto`, `repository`, `service`, `controller`, `mapper`, `enums`,
cada uma com a subpasta `chat/`. Extraia o zip na raiz do projeto (onde ficam `src/` e `pom.xml`).

## Dependências (pom.xml)
- spring-boot-starter-web
- spring-boot-starter-data-jpa
- spring-boot-starter-validation
- spring-boot-starter-websocket (tempo real)
- um banco (PostgreSQL, MySQL, H2...). Para o teste: `com.h2database:h2` com `<scope>test</scope>`
- Java 17+ (usa `record`). Não usa Lombok.

Configuração mínima: `spring.jpa.hibernate.ddl-auto=update` (ou crie as tabelas por migration).

## Independência
- Nenhuma classe do chat importa `Usuario` nem qualquer outro módulo. Usuários são só `usuarioId` (Long), sem FK.
- Tabelas com prefixo `chat_` (`chat_conversas`, `chat_participantes`, `chat_mensagens`, `chat_leituras`).
- Únicas dependências externas ao pacote `chat`, todas em `common/` (compartilhado pelos módulos):
  `common/exception` (RecursoNaoEncontradoException, RegraNegocioException, NaoAutenticadoException, todas com construtor
  `(String)`), `common/handler/GlobalExceptionHandler` e `common/security` (`@UsuarioLogado`, `UsuarioIdResolver`).
  Se o sistema final já tiver algum desses, não copie o duplicado.
- **Identificação do usuário:** os controllers recebem o id com `@UsuarioLogado Long usuarioId`, de `common/security`
  (compartilhado por todos os módulos). Quem decide como o usuário é identificado é a interface `UsuarioIdResolver`:
  hoje a implementação é `HeaderUsuarioIdResolver` (header `X-Usuario-Id` ou `?usuarioId=`, só para desenvolvimento).
  Ao ter login com token, crie outra implementação anotada com `@Component @Primary`. Controllers, services e o
  WebSocket (`ChatWebSocketConfig`) usam o mesmo resolver e não mudam.
- **Validar que o usuário existe:** o chat define a interface `service/chat/ConsultaUsuarioChat` (método `existe(Long)`).
  Se o sistema final tiver um bean que a implemente (numa pasta `integracao/`), o chat passa a recusar ids inexistentes (422).
  Sem esse bean, aceita qualquer id e funciona sozinho.

## Endpoints (todos exigem `X-Usuario-Id`)
| Método | Rota | O que faz |
|---|---|---|
| POST | `/api/chat/conversas` | Cria conversa INDIVIDUAL (1 outro id; reaproveita se já existir) ou GRUPO (título + ids) |
| GET | `/api/chat/conversas?page=0&size=20` | Lista conversas do usuário (mais recentes primeiro) com última mensagem e não lidas |
| GET | `/api/chat/conversas/{id}` | Detalhe da conversa |
| GET | `/api/chat/conversas/{id}/participantes` | Lista participantes ativos |
| POST | `/api/chat/conversas/{id}/participantes/{usuarioId}` | Adiciona ao grupo (só ADMIN) |
| DELETE | `/api/chat/conversas/{id}/participantes/{usuarioId}` | ADMIN remove alguém / qualquer um sai do grupo |
| POST | `/api/chat/conversas/{id}/mensagens` | Envia mensagem `{ "tipo": "TEXTO", "conteudo": "..." }` |
| GET | `/api/chat/conversas/{id}/mensagens?page=0&size=30` | Histórico (mais recentes primeiro) |
| PUT | `/api/chat/mensagens/{id}` | Edita (só autor, só TEXTO) |
| DELETE | `/api/chat/mensagens/{id}` | Exclusão lógica (autor ou ADMIN do grupo) |
| POST | `/api/chat/mensagens/{id}/leitura` | Marca uma mensagem como lida |
| POST | `/api/chat/conversas/{id}/leitura` | Marca toda a conversa como lida |

Erros: 400 validação · 401 sem header · 404 não encontrado (ou usuário não participa da conversa) · 422 regra de negócio.

## Regras principais
- Só participantes ativos leem/enviam; quem não participa recebe 404.
- Criador de grupo vira ADMIN; se o último ADMIN sai, o participante mais antigo é promovido.
- Quem saiu do grupo pode ser readicionado por um ADMIN.
- Mensagens excluídas continuam na lista com `excluida=true` e `conteudo=null`.
- Para tipos IMAGEM/VIDEO/AUDIO/ARQUIVO, `conteudo` guarda a URL do recurso (upload fica fora do módulo).

## Tempo real (WebSocket)
- Conexão: `ws://servidor/ws/chat?usuarioId=ID` (o cliente só recebe; enviar/editar/excluir continua pela API REST).
- Evento recebido (JSON): `{ "tipo": "MENSAGEM_NOVA" | "MENSAGEM_EDITADA" | "MENSAGEM_EXCLUIDA", "conversaId": 1, "mensagem": { ... } }`.
  O remetente também recebe o próprio evento (útil para atualizar todas as abas dele).
- Como funciona sem acoplar: `MensagemService` apenas publica um evento (`NotificacaoChat`); quem entrega é
  `websocket/chat/ChatWebSocketHandler`, que escuta esse evento depois que a transação é confirmada.
  Se você remover as pastas `websocket/chat` e `config/chat`, o chat continua funcionando só por REST.
- Arquivos novos: `config/chat/ChatWebSocketConfig`, `websocket/chat/ChatWebSocketHandler`,
  `dto/chat/MensagemEventoDTO`, `dto/chat/NotificacaoChat`.
- Segurança: hoje o id vai na URL (só para desenvolvimento). Ao ter login, troque o id por um token validado em
  `UsuarioHandshakeInterceptor` (dentro de `ChatWebSocketConfig`) e restrinja `setAllowedOriginPatterns`.
- Página de teste com duas janelas conversando: `/chat-teste.html` (arquivo `src/main/resources/static/chat-teste.html`).

## Teste
`ChatApiTest` (MockMvc + H2 em memória) cobre a API REST, `ChatWebSocketTest` cobre a entrega em tempo real e
`ChatConsultaUsuarioTest` cobre a validação de usuários via `ConsultaUsuarioChat`.
Escrito para Spring Boot 3.x.

## Integração com o módulo de usuários (sem conflito)

O chat **não** depende do módulo de usuários em tempo de compilação.

1. **Ids apenas** — participantes e autores são `Long`. Não há `@ManyToOne` nem FK para tabela de usuários.
2. **Porta `ConsultaUsuarioChat`** — interface em `service/chat/`. Sem bean no contexto, qualquer id é aceito.
   Com o módulo de usuários no mesmo projeto, crie em `integracao/`:

   ```java
   @Component
   class ChatUsuarioAdapter implements ConsultaUsuarioChat {
       private final UsuarioService usuarios;
       public boolean existe(Long id) { return usuarios.existePorId(id); }
   }
   ```

3. **Quem está logado** — controllers usam só `@UsuarioLogado Long usuarioId`.
   - Dev: `HeaderUsuarioIdResolver` (header `X-Usuario-Id`), registrado só se não houver outro `UsuarioIdResolver`.
   - Produção: o módulo de usuários registra um resolver JWT; o header some sozinho (`@ConditionalOnMissingBean`).

4. **Config** — standalone: H2 + porta 8082. Integrado: `spring.profiles.active=integrado` (não força H2;
   datasource e porta vêm do projeto base / variáveis de ambiente).

5. **Contrato** — regras completas em `CONTRATO-DE-MODULOS.md` (raiz). Use-o como base para vídeo e próximos módulos.

Alinhamento de versões: Spring Boot **3.5.16** e Java **17**. O projeto base (`Rede_Social_Arquitetura`) deve
usar as mesmas versões ao incorporar este módulo.
