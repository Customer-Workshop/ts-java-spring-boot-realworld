-- Insert sample users
-- Password for all users is: password123
-- BCrypt hash: $2a$10$AbglDchyhkogGBIxNoHdN.pBDK86VNXtF.Vh6N72G9s1rjw7z2b4u

MERGE INTO users (id, username, email, password, bio, image) KEY(id) VALUES
('user-1', 'johndoe', 'john@example.com', '$2a$10$AbglDchyhkogGBIxNoHdN.pBDK86VNXtF.Vh6N72G9s1rjw7z2b4u', 'Full-stack developer and tech enthusiast', 'https://api.dicebear.com/7.x/avataaars/svg?seed=John'),
('user-2', 'janedoe', 'jane@example.com', '$2a$10$AbglDchyhkogGBIxNoHdN.pBDK86VNXtF.Vh6N72G9s1rjw7z2b4u', 'Software architect passionate about clean code', 'https://api.dicebear.com/7.x/avataaars/svg?seed=Jane'),
('user-3', 'bobsmith', 'bob@example.com', '$2a$10$AbglDchyhkogGBIxNoHdN.pBDK86VNXtF.Vh6N72G9s1rjw7z2b4u', 'DevOps engineer and cloud enthusiast', 'https://api.dicebear.com/7.x/avataaars/svg?seed=Bob');

-- Insert sample tags
MERGE INTO tags (id, name) KEY(id) VALUES
('tag-1', 'java'),
('tag-2', 'spring-boot'),
('tag-3', 'web-development'),
('tag-4', 'tutorial'),
('tag-5', 'best-practices'),
('tag-6', 'microservices'),
('tag-7', 'api-design');

-- Insert sample articles
MERGE INTO articles (id, user_id, slug, title, description, body, created_at, updated_at) KEY(id) VALUES
('article-1', 'user-1', 'getting-started-with-spring-boot', 'Getting Started with Spring Boot', 'A comprehensive guide to building your first Spring Boot application', 'Spring Boot makes it easy to create stand-alone, production-grade Spring based Applications.', DATEADD('DAY', -7, CURRENT_TIMESTAMP), DATEADD('DAY', -7, CURRENT_TIMESTAMP)),
('article-2', 'user-2', 'rest-api-best-practices', 'REST API Best Practices', 'Learn the essential principles for designing robust REST APIs', 'Building a great REST API requires more than just exposing endpoints.', DATEADD('DAY', -5, CURRENT_TIMESTAMP), DATEADD('DAY', -5, CURRENT_TIMESTAMP)),
('article-3', 'user-1', 'microservices-architecture-guide', 'Microservices Architecture Guide', 'Understanding microservices patterns and when to use them', 'Microservices architecture has become increasingly popular.', DATEADD('DAY', -3, CURRENT_TIMESTAMP), DATEADD('DAY', -3, CURRENT_TIMESTAMP)),
('article-4', 'user-3', 'docker-for-java-developers', 'Docker for Java Developers', 'Containerize your Java applications with Docker', 'Docker has revolutionized how we deploy applications.', DATEADD('DAY', -2, CURRENT_TIMESTAMP), DATEADD('DAY', -2, CURRENT_TIMESTAMP)),
('article-5', 'user-2', 'testing-spring-boot-applications', 'Testing Spring Boot Applications', 'A complete guide to testing strategies in Spring Boot', 'Testing is crucial for maintaining code quality.', DATEADD('DAY', -1, CURRENT_TIMESTAMP), DATEADD('DAY', -1, CURRENT_TIMESTAMP));

-- Link articles to tags
MERGE INTO article_tags (article_id, tag_id) KEY(article_id, tag_id) VALUES
('article-1', 'tag-1'),
('article-1', 'tag-2'),
('article-1', 'tag-4'),
('article-2', 'tag-3'),
('article-2', 'tag-5'),
('article-2', 'tag-7'),
('article-3', 'tag-2'),
('article-3', 'tag-6'),
('article-3', 'tag-5'),
('article-4', 'tag-1'),
('article-4', 'tag-2'),
('article-4', 'tag-4'),
('article-5', 'tag-1'),
('article-5', 'tag-2'),
('article-5', 'tag-5');

-- Add some favorites
MERGE INTO article_favorites (article_id, user_id) KEY(article_id, user_id) VALUES
('article-1', 'user-2'),
('article-1', 'user-3'),
('article-2', 'user-1'),
('article-3', 'user-2'),
('article-4', 'user-1'),
('article-5', 'user-3');

-- Add some follows
MERGE INTO follows (user_id, follow_id) KEY(user_id, follow_id) VALUES
('user-1', 'user-2'),
('user-2', 'user-1'),
('user-3', 'user-1'),
('user-3', 'user-2');

-- Add some comments
MERGE INTO comments (id, body, article_id, user_id, created_at, updated_at) KEY(id) VALUES
('comment-1', 'Great article! This really helped me understand Spring Boot basics.', 'article-1', 'user-2', DATEADD('DAY', -6, CURRENT_TIMESTAMP), DATEADD('DAY', -6, CURRENT_TIMESTAMP)),
('comment-2', 'Thanks for sharing. The code examples are very clear.', 'article-1', 'user-3', DATEADD('DAY', -6, CURRENT_TIMESTAMP), DATEADD('DAY', -6, CURRENT_TIMESTAMP)),
('comment-3', 'Excellent best practices guide.', 'article-2', 'user-1', DATEADD('DAY', -4, CURRENT_TIMESTAMP), DATEADD('DAY', -4, CURRENT_TIMESTAMP)),
('comment-4', 'Very comprehensive overview of microservices. Well written!', 'article-3', 'user-2', DATEADD('DAY', -2, CURRENT_TIMESTAMP), DATEADD('DAY', -2, CURRENT_TIMESTAMP)),
('comment-5', 'Docker tutorial was exactly what I needed. Thanks!', 'article-4', 'user-1', DATEADD('DAY', -1, CURRENT_TIMESTAMP), DATEADD('DAY', -1, CURRENT_TIMESTAMP));
