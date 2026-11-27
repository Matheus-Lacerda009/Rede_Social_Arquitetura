-- Execute uma única vez em uma base que já tenha as tabelas do chat criadas.
-- Em uma base nova, estas alterações já estão incorporadas em script.sql.

ALTER TABLE conversation_participants
    ADD COLUMN left_at DATETIME(6) NULL;

ALTER TABLE messages
    MODIFY message_type ENUM('TEXT', 'IMAGE', 'VIDEO', 'AUDIO', 'FILE', 'SYSTEM')
        NOT NULL DEFAULT 'TEXT';
