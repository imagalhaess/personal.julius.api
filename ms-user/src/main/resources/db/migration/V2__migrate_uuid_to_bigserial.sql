-- Migration: Change User ID from UUID to BIGSERIAL (Long)
-- Also adjust CPF column to support formatted CPF (000.000.000-00)

-- Create sequence for user IDs
CREATE SEQUENCE IF NOT EXISTS user_sequence START WITH 1 INCREMENT BY 50;

-- Create new users table with Long ID
CREATE TABLE users_new (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    cpf VARCHAR(14) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    active BOOLEAN DEFAULT TRUE
);

-- Copy data from old table (existing users will get new sequential IDs)
INSERT INTO users_new (name, email, cpf, password, active)
SELECT name, email, cpf, password, active FROM users;

-- Drop old table
DROP TABLE users;

-- Rename new table
ALTER TABLE users_new RENAME TO users;

-- Recreate indexes
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_cpf ON users(cpf);
CREATE INDEX IF NOT EXISTS idx_users_active ON users(active);
