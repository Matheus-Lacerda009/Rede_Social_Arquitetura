/**
 * Única pasta autorizada a "conhecer" dois módulos ao mesmo tempo.
 *
 * Exemplo de adapter (criado quando o módulo de usuários existir):
 *
 * <pre>
 * {@literal @}Component
 * class ChatUsuarioAdapter implements ConsultaUsuarioChat {
 *     private final UsuarioService usuarios; // do módulo usuario
 *     public boolean existe(Long id) { return usuarios.existePorId(id); }
 * }
 * </pre>
 *
 * Sem um bean que implemente {@code ConsultaUsuarioChat}, o chat aceita qualquer id
 * (comportamento standalone). Com o adapter, passa a validar existência no módulo de usuários.
 */
package com.example.demo.integracao;
