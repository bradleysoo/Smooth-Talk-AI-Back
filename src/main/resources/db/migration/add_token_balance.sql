-- Migration script to add token_balance column to users table
-- Run this if Hibernate auto-update doesn't work

-- Add the column if it doesn't exist (PostgreSQL syntax)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'users' AND column_name = 'token_balance'
    ) THEN
        ALTER TABLE users ADD COLUMN token_balance BIGINT DEFAULT 0;
    END IF;
END $$;

-- Update existing users with NULL token_balance to have 5 tokens
UPDATE users 
SET token_balance = 5 
WHERE token_balance IS NULL OR token_balance = 0;

-- Remove old subscription columns (optional - only if you want to clean up)
-- ALTER TABLE users DROP COLUMN IF EXISTS subscription_plan;
-- ALTER TABLE users DROP COLUMN IF EXISTS subscription_expires_at;
-- ALTER TABLE users DROP COLUMN IF EXISTS subscription_status;
