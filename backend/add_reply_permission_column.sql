-- Add reply_permission column to threads table
ALTER TABLE threads ADD COLUMN IF NOT EXISTS reply_permission VARCHAR(20) DEFAULT 'ANYONE';

-- Update existing threads to have ANYONE permission
UPDATE threads SET reply_permission = 'ANYONE' WHERE reply_permission IS NULL;

-- Add constraint to ensure valid values
ALTER TABLE threads ADD CONSTRAINT chk_reply_permission 
CHECK (reply_permission IN ('ANYONE', 'FOLLOWERS', 'FOLLOWING', 'MENTIONED_ONLY'));
