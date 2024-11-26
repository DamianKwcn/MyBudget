CREATE TABLE users (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    mobile_number VARCHAR(255) NOT NULL,
    balance BIGINT,
    created_at date NOT NULL,
    updated_at date DEFAULT NULL
);