CREATE TABLE IF NOT EXISTS transactions
(
    id
    UUID
    PRIMARY
    KEY,
    user_id
    UUID
    NOT
    NULL,
    amount
    DECIMAL
(
    15,
    2
) NOT NULL,
    currency VARCHAR
(
    3
) NOT NULL DEFAULT 'BRL',
    status VARCHAR
(
    20
) NOT NULL DEFAULT 'PENDING',
    category VARCHAR
(
    50
),
    type VARCHAR
(
    10
),
    description TEXT,
    transaction_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_transactions_user_id ON transactions(user_id);
CREATE INDEX IF NOT EXISTS idx_transactions_status ON transactions(status);
CREATE INDEX IF NOT EXISTS idx_transactions_date ON transactions(transaction_date);
CREATE INDEX IF NOT EXISTS idx_transactions_user_date ON transactions(user_id, transaction_date);
CREATE INDEX IF NOT EXISTS idx_transactions_type ON transactions(type);
CREATE INDEX IF NOT EXISTS idx_transactions_category ON transactions(category);
