CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(255) NOT NULL,
    transaction_type VARCHAR(50) NOT NULL,
    keycloak_sub VARCHAR(255) DEFAULT NULL,
    built_in BOOLEAN NOT NULL
);

INSERT INTO categories (category_name, transaction_type, keycloak_sub, built_in) VALUES
('Salary', 'INCOME', NULL, true),
('Gift', 'INCOME', NULL, true);

INSERT INTO categories (category_name, transaction_type, keycloak_sub, built_in) VALUES
('Car', 'EXPENSE', NULL, true),
('Home', 'EXPENSE', NULL, true),
('Health', 'EXPENSE', NULL, true)

CREATE TABLE transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    keycloak_sub VARCHAR(255) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    balance_after DECIMAL(19,2) NOT NULL,
    transaction_type VARCHAR(50) NOT NULL,
    category_id BIGINT NOT NULL,
    description VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_category FOREIGN KEY (category_id) REFERENCES categories(id)
);