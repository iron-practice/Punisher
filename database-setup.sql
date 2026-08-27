CREATE DATABASE IF NOT EXISTS ironpunisher CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE ironpunisher;

CREATE TABLE IF NOT EXISTS punishments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    uuid CHAR(36) NOT NULL,
    player_name VARCHAR(16) NOT NULL,
    reason VARCHAR(255) NOT NULL,
    punishment_type VARCHAR(32) NOT NULL DEFAULT 'BAN',
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    expires_at TIMESTAMP(3) NULL,
    created_by VARCHAR(64) NOT NULL,
    server_id VARCHAR(64) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    INDEX idx_uuid_active (uuid, active),
    INDEX idx_active_created (active, created_at),
    INDEX idx_expires_at (expires_at),
    INDEX idx_server_created (server_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
