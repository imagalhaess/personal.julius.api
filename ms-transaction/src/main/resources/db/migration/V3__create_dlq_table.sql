CREATE TABLE dlq_messages (
    id BIGSERIAL PRIMARY KEY,
    transaction_id VARCHAR(100),
    original_event TEXT,
    error_message TEXT,
    source_service VARCHAR(50),
    failed_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_dlq_transaction_id ON dlq_messages(transaction_id);
CREATE INDEX idx_dlq_source_service ON dlq_messages(source_service);
