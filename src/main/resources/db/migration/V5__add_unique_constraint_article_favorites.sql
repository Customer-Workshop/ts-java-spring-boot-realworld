-- Add unique index on article_favorites table for (article_id, user_id)
-- Note: The table already has a composite primary key on these columns which enforces uniqueness,
-- but this explicit unique index provides additional safety and clarity for the constraint.
CREATE UNIQUE INDEX IF NOT EXISTS idx_article_favorites_unique 
ON article_favorites(article_id, user_id);
