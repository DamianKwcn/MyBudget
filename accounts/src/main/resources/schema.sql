CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    keycloak_sub VARCHAR(36) NOT NULL,
    given_name VARCHAR(255),
    family_name VARCHAR(255),
    preferred_username VARCHAR(255),
    email VARCHAR(255),
    PRIMARY KEY (id),
    UNIQUE KEY (keycloak_sub)
);