CREATE TABLE transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    keycloak_sub VARCHAR(255) NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    balance_after DECIMAL(19, 2) NOT NULL,
    transaction_type VARCHAR(50) NOT NULL,
    expense_category VARCHAR(50),
    income_category VARCHAR(50),
    description VARCHAR(255),
    CHECK (
        (transaction_type = 'EXPENSE' AND expense_category IS NOT NULL AND income_category IS NULL) OR
        (transaction_type = 'INCOME' AND income_category IS NOT NULL AND expense_category IS NULL)
    )
);