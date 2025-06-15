CREATE DATABASE IF NOT EXISTS transactiondb;
USE transactiondb;

CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    keycloak_sub VARCHAR(255) NOT NULL,
    category_name VARCHAR(255) COLLATE utf8mb4_unicode_ci NOT NULL,
    preferred_username VARCHAR(255) DEFAULT NULL,
    category_balance DECIMAL(19,2) NOT NULL DEFAULT 0,
    transaction_type VARCHAR(50) NOT NULL,
    CONSTRAINT uq_category UNIQUE (keycloak_sub, category_name)
);

CREATE TABLE transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    keycloak_sub VARCHAR(255) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    balance_after DECIMAL(19,2),
    transaction_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    category_id BIGINT NOT NULL,
    description VARCHAR(255),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_category
        FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
);

CREATE INDEX idx_tx_user ON transactions(keycloak_sub);
CREATE INDEX idx_tx_user_type ON transactions(keycloak_sub, transaction_type);
CREATE INDEX idx_tx_category_user ON transactions(category_id, keycloak_sub);
