package com.example.demo.service.chat;

/**
 * O que o chat precisa saber sobre usuários, definido pelo próprio chat.
 *
 * Se o sistema final tiver um bean que implemente esta interface (em integracao/), o chat passa a recusar
 * ids de usuários que não existem. Se NÃO houver nenhum, o chat aceita qualquer id e funciona sozinho.
 *
 * Exemplo (fora do chat):
 *   @Component class ChatUsuarioAdapter implements ConsultaUsuarioChat {
 *       public boolean existe(Long id) { return usuarioService.existePorId(id); }
 *   }
 */
@FunctionalInterface
public interface ConsultaUsuarioChat {
    boolean existe(Long usuarioId);
}
