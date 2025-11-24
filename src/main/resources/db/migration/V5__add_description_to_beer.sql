-- Flyway migration V4: add description column to beer table
-- Following H2-compatible SQL and Flyway conventions

-- Add nullable description column for backward compatibility
ALTER TABLE beer ADD COLUMN IF NOT EXISTS description VARCHAR(255);
