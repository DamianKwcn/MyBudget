CREATE DATABASE IF NOT EXISTS accountdb;
USE accountdb;
CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    keycloak_sub VARCHAR(36) NOT NULL,
    preferred_username VARCHAR(255),
    email VARCHAR(255),
    balance DECIMAL(19,4),
    PRIMARY KEY (id),
    UNIQUE KEY (keycloak_sub)
);
