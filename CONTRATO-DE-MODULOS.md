# Contrato de encaixe dos módulos

Este documento é obrigatório para **todos** os módulos (usuario, video, chat e os próximos).
Quem seguir estas regras consegue desenvolver a parte dele separadamente e juntar sem conflito.
Coloque este arquivo na **raiz do projeto**, ao lado do `pom.xml`.

---

## 1. Um projeto, vários módulos

- Existe **UM** projeto Maven, **UM** `pom.xml` e **UM** `DemoApplication`.
- Módulo = conjunto de pastas dentro do mesmo projeto, **não** um repositório ou artefato Maven separado.
- Pacote raiz de tudo: `com.example.demo`.
- **Java 17** (mínimo). Spring Boot **3.5.16** — todos os módulos e a base usam a **mesma** versão.
  - O esqueleto `Rede_Social_Arquitetura` deve ser alinhado a essa versão (não misturar Boot 4.x / Java 21
    com módulos escritos em Boot 3.5 / Java 17).
- Dependências do `pom.xml` (união de todos; quem precisar de mais avisa o time):
  - base: `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`
  - chat: `spring-boot-starter-websocket`
  - banco: H2 (dev standalone) e/ou driver MySQL/PostgreSQL (integrado)
  - quando houver login: `spring-boot-starter-security` + biblioteca de JWT
  - opcional: Lombok, se o time concordar em usar em **todos** os módulos

---

## 2. Estrutura de pastas

O nome do módulo é a **subpasta** dentro de cada camada:

```
model/<modulo>/
dto/<modulo>/
repository/<modulo>/
service/<modulo>/
controller/<modulo>/
mapper/<modulo>/
enums/<modulo>/
```

| Módulo   | Prefixo de rota   | Prefixo de tabela | Extras próprios                          |
|----------|-------------------|-------------------|------------------------------------------|
| usuario  | `/api/usuarios`   | `usuario_`        | (auth, se houver)                        |
| video    | `/api/videos`     | `video_`          | —                                        |
| chat     | `/api/chat`       | `chat_`           | `config/chat/`, `websocket/chat/`        |

Pastas **compartilhadas** (não pertencem a nenhum módulo de negócio):

```
common/exception/     → exceções de domínio (404, 422, 401)
common/handler/       → um único GlobalExceptionHandler
common/security/      → UsuarioIdResolver, @UsuarioLogado, resolvers
integracao/           → ÚNICA pasta autorizada a importar classes de dois módulos
```

`common/` é da **plataforma**. Nenhum módulo cria a própria versão de GlobalExceptionHandler ou de
identificação de usuário.

---

## 3. Nomes que não podem colidir

| Item       | Regra |
|------------|--------|
| Rotas      | Cada módulo só cria endpoints no seu prefixo (`/api/usuarios/**`, `/api/videos/**`, `/api/chat/**`) |
| Tabelas    | Prefixo do módulo + `@Table(name = "...")` sempre (`chat_conversas`, `usuario_perfis`, …) |
| Entidades  | Nome de classe único no projeto. Se houver risco, use `@Entity(name = "NomeUnico")` |
| Beans      | Nada de dois `@Component` / `@Service` com o **mesmo nome de classe** em módulos diferentes |

Entidades já usadas pelo **chat**: `Conversa`, `ParticipanteConversa`, `Mensagem`, `LeituraMensagem`.

---

## 4. Identidade do usuário (regra mais importante)

1. O id do usuário é **`Long`** em **todos** os módulos.
2. Módulos que **não** são o de usuários (`chat`, `video`, …) guardam o usuário **só como número**
   (`usuarioId`, `criadoPor`, `donoId`, …).  
   **Proibido:** `@ManyToOne` para entidade `Usuario` e chave estrangeira JPA para tabela de usuários.
3. Quem está logado chega aos controllers de **um jeito só**:

   ```java
   @PostMapping(...)
   public ResponseEntity<?> criar(@UsuarioLogado Long usuarioId, @Valid @RequestBody ...) { ... }
   ```

   Os módulos **nunca** leem header, cookie ou token por conta própria.
4. Quem decide *como* o usuário é identificado é a interface:

   ```java
   // common/security/UsuarioIdResolver.java
   Optional<Long> resolver(HttpServletRequest request);
   ```

   - **Standalone / dev:** `HeaderUsuarioIdResolver` (header `X-Usuario-Id` ou query `usuarioId`).
     Registrado com `@ConditionalOnMissingBean(UsuarioIdResolver.class)` — some sozinho se existir outro.
   - **Com login (módulo usuario):** criar outra implementação (JWT) com `@Component`.
     Opcional: `@Primary`. Nenhum outro módulo precisa mudar.

---

## 5. Como um módulo fala com outro (portas / adapters)

Comunicação **permitida**:

1. Passar ids (`Long`) — o padrão.
2. Chamar o **service** de outro módulo (nunca repository nem entidade).
3. Publicar / escutar eventos Spring (`ApplicationEventPublisher` + `@TransactionalEventListener`),
   como o chat faz com `NotificacaoChat`.

**Proibido:** `import` de `model`, `repository` ou `dto` de outro módulo.

Quando um módulo precisa de algo de outro (ex.: “esse usuário existe?”), ele declara uma **interface própria**
no seu `service/<modulo>/` com o mínimo necessário. A implementação fica em `integracao/`:

```java
// service/chat/ConsultaUsuarioChat.java  (definida pelo chat)
@FunctionalInterface
public interface ConsultaUsuarioChat {
    boolean existe(Long usuarioId);
}

// integracao/ChatUsuarioAdapter.java  (criada quando o módulo usuario existir)
@Component
class ChatUsuarioAdapter implements ConsultaUsuarioChat {
    private final UsuarioService usuarios; // service do módulo usuario

    public boolean existe(Long id) {
        return usuarios.existePorId(id);
    }
}
```

No chat, a injeção usa `ObjectProvider<ConsultaUsuarioChat>`:

- **Sem** bean → aceita qualquer id (módulo sobe sozinho).
- **Com** bean → valida existência no módulo de usuários.

Esse padrão (porta no módulo consumidor + adapter em `integracao/`) é o modelo para os próximos módulos.

---

## 6. Padrões comuns

| Tema        | Regra |
|-------------|--------|
| Erros       | `common/exception`: `RecursoNaoEncontradoException` → 404, `RegraNegocioException` → 422, `NaoAutenticadoException` → 401. Um único `GlobalExceptionHandler`. Validação Bean Validation → 400. |
| DTOs        | Preferir `record`. Bean Validation nos de **entrada**. Nunca devolver entidade JPA na API. |
| Datas       | `Instant` (JSON em ISO-8601). |
| Listas      | Paginadas com `Page` / `Pageable`. Tamanho máximo 100. |
| Exclusão    | Lógica (campo `excluidaEm` / flag) quando o registro ainda precisa existir para histórico. |
| Banco       | Uma única conexão. Dev: `ddl-auto=update`. Produção: migrations (Flyway), **um arquivo por módulo**. |
| Config      | Chat standalone usa H2 + porta 8082. Perfil `integrado` não força H2 e usa `SERVER_PORT` / datasource externo. |

---

## 7. O que o módulo de usuários deve entregar para o restante

Para o chat (e outros) encaixarem sem atrito, o módulo **usuario** deve:

1. Expor um service público, por exemplo `UsuarioService.existePorId(Long id)` (e, se útil, `buscarNome(Long id)`).
2. Quando houver autenticação real, fornecer um `UsuarioIdResolver` baseado em token (JWT),
   registrado como `@Component` (o `HeaderUsuarioIdResolver` do common deixa de ser registrado sozinho).
3. **Não** criar entidades ou tabelas com nomes já usados (`Conversa`, `Mensagem`, `chat_*`, etc.).
4. Rotas apenas em `/api/usuarios/**`.
5. Usar as mesmas exceções de `common/exception` e o mesmo `GlobalExceptionHandler`.

Adapter mínimo em `integracao/` (quando os dois módulos estiverem no mesmo projeto):

```java
@Component
class ChatUsuarioAdapter implements ConsultaUsuarioChat {
    private final UsuarioService usuarios;

    ChatUsuarioAdapter(UsuarioService usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    public boolean existe(Long usuarioId) {
        return usuarios.existePorId(usuarioId);
    }
}
```

---

## 8. Checklist ao juntar os módulos

- [ ] Tudo dentro de `com.example.demo` e o projeto sobe com um único `DemoApplication`.
- [ ] Um só `pom.xml` com a união das dependências e **mesmas versões** (Boot 3.5.16, Java 17).
- [ ] Uma só pasta `common/` e um só `GlobalExceptionHandler`.
- [ ] Nenhum `import` entre `model` / `repository` / `dto` de módulos diferentes (só `integracao/` cruza).
- [ ] Nenhuma entidade com nome repetido; todas as tabelas com prefixo do módulo.
- [ ] Ids de usuário são `Long` em todo lugar; sem `@ManyToOne` para `Usuario` fora do módulo usuario.
- [ ] No máximo um bean `UsuarioIdResolver` efetivo (header em dev ou JWT em produção).
- [ ] Cada módulo continua compilando e subindo **sozinho** (teste: apague os outros e verifique).

---

## 9. Texto curto para quem for fazer cada módulo

> Você vai implementar o módulo `<nome>` dentro do projeto Spring Boot **3.5.16** (Java 17) no pacote
> `com.example.demo`, seguindo o arquivo `CONTRATO-DE-MODULOS.md`:
> - pastas por camada com subpasta `<nome>`;
> - rotas só em `/api/<nome plural ou convenção do time>`;
> - tabelas com prefixo `<nome>_`;
> - id de usuário `Long`, **sem** relacionamento JPA com entidade `Usuario`;
> - exceções e handler de `common/`;
> - identificação do logado só via `@UsuarioLogado`;
> - **sem** importar `model` / `repository` / `dto` de outros módulos;
> - se precisar de outro módulo, declare uma interface no seu `service/` e a implementação em `integracao/`.

---

## 10. Referência do que o chat já estabeleceu

| Porta / ponto de extensão              | Onde está                         | Comportamento sem implementação      |
|----------------------------------------|-----------------------------------|--------------------------------------|
| `ConsultaUsuarioChat`                  | `service/chat/`                   | Aceita qualquer id (`id -> true`)    |
| `UsuarioIdResolver`                    | `common/security/`                | Header `X-Usuario-Id` (dev)          |
| Evento `NotificacaoChat`               | `dto/chat/` + WebSocket handler   | REST continua funcionando sozinho    |
| Perfil `integrado`                     | `application-integrado.properties`| Não força H2; usa config externa     |

Use o mesmo estilo de porta + adapter opcional nos próximos módulos.
