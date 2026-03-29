CREATE TABLE IF NOT EXISTS sales_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    area VARCHAR(100) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    total_sales_goal DECIMAL(15,2) NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_sales_transactions_area ON sales_transactions(area);

