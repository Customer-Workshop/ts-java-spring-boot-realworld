-- Update seed user passwords (see README for credentials)
-- BCrypt hash for the new password:

UPDATE users SET password = '$2a$10$LUtBa47o7pSr8/JUK2bx7.ZzTPpzcX9C7eDwjIR6IPGtYr4GpewxS'
WHERE id IN ('user-1', 'user-2', 'user-3');
