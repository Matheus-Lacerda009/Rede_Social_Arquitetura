
CREATE DATABASE IF NOT EXISTS rede_social
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

USE rede_social;

-- =============================================
-- 1. USUÁRIOS
-- =============================================

CREATE TABLE users (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,

    username VARCHAR(30) NOT NULL UNIQUE,
    email VARCHAR(254) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,

    status ENUM('ACTIVE', 'SUSPENDED', 'DELETED')
        NOT NULL DEFAULT 'ACTIVE',

    last_login_at DATETIME(6),

    created_at TIMESTAMP(6)
        NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    updated_at TIMESTAMP(6)
        NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6)
);

-- =============================================
-- 2. PERFIS
-- =============================================

CREATE TABLE user_profiles (
    user_id BIGINT UNSIGNED PRIMARY KEY,

    display_name VARCHAR(60) NOT NULL,
    bio VARCHAR(500),
    avatar_url VARCHAR(2048),

    CONSTRAINT fk_profile_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

-- =============================================
-- 3. SEGUIDORES
-- =============================================

CREATE TABLE user_follows (
    follower_id BIGINT UNSIGNED NOT NULL,
    followed_id BIGINT UNSIGNED NOT NULL,

    created_at TIMESTAMP(6)
        NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    PRIMARY KEY (follower_id, followed_id),

    CONSTRAINT fk_follow_follower
        FOREIGN KEY (follower_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_follow_followed
        FOREIGN KEY (followed_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_no_self_follow
        CHECK (follower_id <> followed_id),

    INDEX idx_followed_id (followed_id)
);

-- =============================================
-- 4. VÍDEOS
-- =============================================

CREATE TABLE videos (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,

    user_id BIGINT UNSIGNED NOT NULL,

    title VARCHAR(150) NOT NULL,
    description TEXT,

    video_url VARCHAR(2048) NOT NULL,
    thumbnail_url VARCHAR(2048),

    duration_seconds INT UNSIGNED,

    visibility ENUM('PUBLIC', 'FOLLOWERS', 'PRIVATE')
        NOT NULL DEFAULT 'PUBLIC',

    status ENUM('PROCESSING', 'READY', 'FAILED')
        NOT NULL DEFAULT 'PROCESSING',

    created_at TIMESTAMP(6)
        NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    updated_at TIMESTAMP(6)
        NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT fk_video_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    INDEX idx_video_user (user_id),
    INDEX idx_video_created (created_at),
    INDEX idx_video_visibility_status (visibility, status)
);

-- =============================================
-- 5. CURTIDAS DOS VÍDEOS
-- =============================================

CREATE TABLE video_likes (
    user_id BIGINT UNSIGNED NOT NULL,
    video_id BIGINT UNSIGNED NOT NULL,

    created_at TIMESTAMP(6)
        NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    PRIMARY KEY (user_id, video_id),

    CONSTRAINT fk_like_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_like_video
        FOREIGN KEY (video_id)
        REFERENCES videos(id)
        ON DELETE CASCADE,

    INDEX idx_like_video (video_id)
);

-- =============================================
-- 6. COMENTÁRIOS DOS VÍDEOS
-- =============================================

CREATE TABLE video_comments (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,

    video_id BIGINT UNSIGNED NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL,

    parent_comment_id BIGINT UNSIGNED,

    content TEXT NOT NULL,

    created_at TIMESTAMP(6)
        NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    updated_at TIMESTAMP(6)
        NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT fk_comment_video
        FOREIGN KEY (video_id)
        REFERENCES videos(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_comment_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_comment_parent
        FOREIGN KEY (parent_comment_id)
        REFERENCES video_comments(id)
        ON DELETE CASCADE,

    INDEX idx_comment_video (video_id),
    INDEX idx_comment_user (user_id),
    INDEX idx_comment_parent (parent_comment_id)
);

-- =============================================
-- 7. CONVERSAS
-- =============================================

CREATE TABLE conversations (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,

    created_by BIGINT UNSIGNED NOT NULL,

    type ENUM('DIRECT', 'GROUP')
        NOT NULL DEFAULT 'DIRECT',

    title VARCHAR(100),

    created_at TIMESTAMP(6)
        NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    updated_at TIMESTAMP(6)
        NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT fk_conversation_creator
        FOREIGN KEY (created_by)
        REFERENCES users(id)
        ON DELETE CASCADE,

    INDEX idx_conversation_creator (created_by)
);

-- =============================================
-- 8. PARTICIPANTES DAS CONVERSAS
-- =============================================

CREATE TABLE conversation_participants (
    conversation_id BIGINT UNSIGNED NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL,

    role ENUM('MEMBER', 'ADMIN')
        NOT NULL DEFAULT 'MEMBER',

    joined_at TIMESTAMP(6)
        NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    PRIMARY KEY (conversation_id, user_id),

    CONSTRAINT fk_participant_conversation
        FOREIGN KEY (conversation_id)
        REFERENCES conversations(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_participant_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    INDEX idx_participant_user (user_id)
);

-- =============================================
-- 9. MENSAGENS
-- =============================================

CREATE TABLE messages (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,

    conversation_id BIGINT UNSIGNED NOT NULL,
    sender_id BIGINT UNSIGNED,

    content TEXT,

    message_type ENUM('TEXT', 'IMAGE', 'VIDEO', 'FILE', 'SYSTEM')
        NOT NULL DEFAULT 'TEXT',

    media_url VARCHAR(2048),
    media_mime_type VARCHAR(100),

    reply_to_message_id BIGINT UNSIGNED,

    edited_at DATETIME(6),
    deleted_at DATETIME(6),

    created_at TIMESTAMP(6)
        NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT fk_message_conversation
        FOREIGN KEY (conversation_id)
        REFERENCES conversations(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_message_sender
        FOREIGN KEY (sender_id)
        REFERENCES users(id)
        ON DELETE SET NULL,

    CONSTRAINT fk_message_reply
        FOREIGN KEY (reply_to_message_id)
        REFERENCES messages(id)
        ON DELETE SET NULL,

    INDEX idx_message_conversation (conversation_id, created_at),
    INDEX idx_message_sender (sender_id),
    INDEX idx_message_reply (reply_to_message_id)
);

-- =============================================
-- 10. CONFIRMAÇÃO DE LEITURA
-- =============================================

CREATE TABLE message_reads (
    message_id BIGINT UNSIGNED NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL,

    read_at TIMESTAMP(6)
        NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    PRIMARY KEY (message_id, user_id),

    CONSTRAINT fk_read_message
        FOREIGN KEY (message_id)
        REFERENCES messages(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_read_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    INDEX idx_read_user (user_id)
);
