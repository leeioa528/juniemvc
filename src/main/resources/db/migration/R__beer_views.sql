-- Repeatable migration: sample view for reporting
-- This is H2-compatible and safe to re-run by Flyway when contents change.

CREATE VIEW IF NOT EXISTS vw_beer_counts_by_style AS
SELECT beer_style, COUNT(*) AS beer_count
FROM beer
GROUP BY beer_style;