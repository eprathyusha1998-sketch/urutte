-- Fix thread hierarchy for existing data
-- This script corrects the thread_path values for existing threads

-- First, let's see the current state
SELECT id, content, parent_thread_id, root_thread_id, thread_level, thread_path 
FROM threads 
ORDER BY id;

-- Update thread paths for existing threads
-- We need to rebuild the thread paths based on the parent relationships

-- Step 1: Update threads that have no parent (main threads)
UPDATE threads 
SET thread_path = NULL 
WHERE parent_thread_id IS NULL;

-- Step 2: Update first-level replies (thread_level = 1)
UPDATE threads 
SET thread_path = CAST(parent_thread_id AS VARCHAR)
WHERE thread_level = 1 AND parent_thread_id IS NOT NULL;

-- Step 3: Update second-level replies (thread_level = 2)
UPDATE threads 
SET thread_path = (
    SELECT CAST(t2.parent_thread_id AS VARCHAR) || '.' || CAST(t2.id AS VARCHAR)
    FROM threads t2 
    WHERE t2.id = threads.parent_thread_id
)
WHERE thread_level = 2 AND parent_thread_id IS NOT NULL;

-- Step 4: Update third-level replies (thread_level = 3)
UPDATE threads 
SET thread_path = (
    SELECT t3.thread_path || '.' || CAST(t3.id AS VARCHAR)
    FROM threads t3 
    WHERE t3.id = threads.parent_thread_id
)
WHERE thread_level = 3 AND parent_thread_id IS NOT NULL;

-- Step 5: Update fourth-level replies (thread_level = 4)
UPDATE threads 
SET thread_path = (
    SELECT t4.thread_path || '.' || CAST(t4.id AS VARCHAR)
    FROM threads t4 
    WHERE t4.id = threads.parent_thread_id
)
WHERE thread_level = 4 AND parent_thread_id IS NOT NULL;

-- Step 6: Update fifth-level replies (thread_level = 5)
UPDATE threads 
SET thread_path = (
    SELECT t5.thread_path || '.' || CAST(t5.id AS VARCHAR)
    FROM threads t5 
    WHERE t5.id = threads.parent_thread_id
)
WHERE thread_level = 5 AND parent_thread_id IS NOT NULL;

-- Show the corrected state
SELECT id, content, parent_thread_id, root_thread_id, thread_level, thread_path 
FROM threads 
ORDER BY id;
