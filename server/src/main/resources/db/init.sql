-- ============================================================
-- Fenerbahçe Sözlük - PostgreSQL Database Init Script
-- ============================================================
-- Bu script'i çalıştırmadan önce:
-- 1. Database ve user oluşturulmuş olmalı
-- 2. psql -U fenerbahce_user -d fenerbahcesozluk -f init.sql
-- ============================================================

-- UUID extension (gerekli)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================
-- USERS TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    version BIGINT DEFAULT 0,
    created_time TIMESTAMP,
    updated_time TIMESTAMP,
    
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'USER',
    is_active BOOLEAN DEFAULT TRUE,
    banned_until TIMESTAMP,
    ban_reason VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- ============================================================
-- TOPICS TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS topics (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    version BIGINT DEFAULT 0,
    created_time TIMESTAMP,
    updated_time TIMESTAMP,
    
    title VARCHAR(50) NOT NULL,
    author_id UUID NOT NULL REFERENCES users(id),
    entry_count INTEGER DEFAULT 0,
    view_count BIGINT DEFAULT 0,
    is_locked BOOLEAN DEFAULT FALSE,
    is_pinned BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    delete_reason VARCHAR(255),
    kunye_image_url VARCHAR(500),
    kunye_data TEXT
);

CREATE INDEX IF NOT EXISTS idx_topic_active_entrycount ON topics(is_active, entry_count);
CREATE INDEX IF NOT EXISTS idx_topic_active_created ON topics(is_active, created_time);
CREATE INDEX IF NOT EXISTS idx_topic_title ON topics(title);
CREATE INDEX IF NOT EXISTS idx_topic_author ON topics(author_id);

-- ============================================================
-- ENTRIES TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS entries (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    version BIGINT DEFAULT 0,
    created_time TIMESTAMP,
    updated_time TIMESTAMP,
    
    content TEXT NOT NULL,
    topic_id UUID NOT NULL REFERENCES topics(id) ON DELETE CASCADE,
    author_id UUID NOT NULL REFERENCES users(id),
    like_count INTEGER DEFAULT 0,
    dislike_count INTEGER DEFAULT 0,
    favorite_count INTEGER DEFAULT 0,
    is_edited BOOLEAN DEFAULT FALSE,
    is_hidden BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    delete_reason VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_entry_topic_active_created ON entries(topic_id, is_active, created_time);
CREATE INDEX IF NOT EXISTS idx_entry_author_active ON entries(author_id, is_active);
CREATE INDEX IF NOT EXISTS idx_entry_active_likes ON entries(is_active, like_count);
CREATE INDEX IF NOT EXISTS idx_entry_created ON entries(created_time);

-- ============================================================
-- VOTES TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS votes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    version BIGINT DEFAULT 0,
    created_time TIMESTAMP,
    updated_time TIMESTAMP,
    
    entry_id UUID NOT NULL REFERENCES entries(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id),
    vote_type VARCHAR(50) NOT NULL,
    
    CONSTRAINT uk_vote_entry_user UNIQUE (entry_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_vote_entry_user ON votes(entry_id, user_id);

-- ============================================================
-- MESSAGES TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS messages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    version BIGINT DEFAULT 0,
    created_time TIMESTAMP,
    updated_time TIMESTAMP,
    
    sender_id UUID NOT NULL REFERENCES users(id),
    receiver_id UUID NOT NULL REFERENCES users(id),
    content VARCHAR(2000) NOT NULL,
    read_at TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE,
    deleted_by_sender BOOLEAN DEFAULT FALSE,
    deleted_by_receiver BOOLEAN DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_message_sender ON messages(sender_id);
CREATE INDEX IF NOT EXISTS idx_message_receiver ON messages(receiver_id);
CREATE INDEX IF NOT EXISTS idx_message_read_at ON messages(read_at);

-- ============================================================
-- NEWS TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS news (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    version BIGINT DEFAULT 0,
    created_time TIMESTAMP,
    updated_time TIMESTAMP,
    
    title VARCHAR(255) NOT NULL,
    link VARCHAR(1000) NOT NULL,
    description VARCHAR(2000),
    image_url VARCHAR(1000),
    source VARCHAR(255) NOT NULL,
    pub_date TIMESTAMP NOT NULL,
    guid VARCHAR(255) UNIQUE
);

CREATE INDEX IF NOT EXISTS idx_news_pub_date ON news(pub_date);
CREATE UNIQUE INDEX IF NOT EXISTS idx_news_guid ON news(guid);

-- ============================================================
-- PASSWORD RESET TOKENS TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    version BIGINT DEFAULT 0,
    created_time TIMESTAMP,
    updated_time TIMESTAMP,
    
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id UUID NOT NULL REFERENCES users(id),
    expiry_date TIMESTAMP NOT NULL,
    used BOOLEAN DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_password_reset_token ON password_reset_tokens(token);
CREATE INDEX IF NOT EXISTS idx_password_reset_user ON password_reset_tokens(user_id);

-- ============================================================
-- İLK ADMIN KULLANICISI (Opsiyonel)
-- ============================================================
-- Şifre: Admin123! (BCrypt hash)
-- Bu satırı kullanmak isterseniz aşağıdaki satırı uncomment edin:
--
-- INSERT INTO users (id, version, created_time, updated_time, username, email, password, role, is_active)
-- VALUES (
--     uuid_generate_v4(),
--     0,
--     NOW(),
--     NOW(),
--     'admin',
--     'admin@fenerbahcesozluk.net',
--     '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqHuDn6Xk.7vqFgVVk7PHh.XT7emi',
--     'ADMIN',
--     TRUE
-- );

-- ============================================================
-- Tamamlandı
-- ============================================================
SELECT 'Database tables created successfully!' AS status;
