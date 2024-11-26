CREATE TABLE expenses (
    expense_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    mobile_number VARCHAR(15) NOT NULL,
    title VARCHAR(20) NOT NULL,
    description varchar(100),
    amount_used DECIMAL(10, 2) NOT NULL,
    created_at date NOT NULL,
    updated_at date DEFAULT NULL
);