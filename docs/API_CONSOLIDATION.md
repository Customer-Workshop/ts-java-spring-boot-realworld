# API Consolidation Design Document

## Overview

This document describes the API consolidation strategy implemented for the Spring Boot RealWorld Example App. The application provides both REST and GraphQL APIs for a Medium.com-like blogging platform.

## Consolidation Strategy: Unified API Gateway Pattern

After analyzing the existing REST and GraphQL implementations, we chose **Option C: Unified API Gateway Pattern** for the following reasons:

1. **Backward Compatibility**: Maintains existing REST endpoints for clients that depend on them
2. **GraphQL Flexibility**: Preserves GraphQL's powerful query capabilities for complex data fetching
3. **Code Reuse**: Eliminates duplicate business logic by consolidating into a shared service layer
4. **Consistent Error Handling**: Unified exception handling across both API types
5. **Maintainability**: Single source of truth for business logic makes the codebase easier to maintain

## Architecture

### Before Consolidation

```
REST Controllers ──────────────────────────────────> Repositories
                                                          │
GraphQL Resolvers ─────────────────────────────────> Repositories
```

Both REST controllers and GraphQL resolvers contained duplicate business logic, validation, and authorization checks.

### After Consolidation

```
REST Controllers ──────┐
                       │
                       ├──> API Facade Layer ──> Application Services ──> Repositories
                       │
GraphQL Resolvers ─────┘
```

The API Facade Layer provides a unified interface that both REST and GraphQL APIs consume.

## Components

### API Facade Layer

Located in `io.spring.application.facade`, the facade layer consists of:

- **UserApiFacade**: Handles user registration, login, profile management, and follow/unfollow operations
- **ArticleApiFacade**: Manages article CRUD operations, favorites, and article queries
- **CommentApiFacade**: Handles comment creation, retrieval, and deletion
- **AuthenticationFacade**: Provides unified authentication utilities

### Error Handling

The application uses a consistent exception hierarchy:

| Exception | HTTP Status | GraphQL ErrorType | Description |
|-----------|-------------|-------------------|-------------|
| `ResourceNotFoundException` | 404 | NOT_FOUND | Resource does not exist |
| `NoAuthorizationException` | 403 | PERMISSION_DENIED | User lacks permission |
| `InvalidAuthenticationException` | 422 | UNAUTHENTICATED | Invalid credentials |
| `AuthenticationException` | 401 | UNAUTHENTICATED | Authentication required |
| `ConstraintViolationException` | 422 | BAD_REQUEST | Validation errors |

### REST API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/users` | Register a new user |
| POST | `/users/login` | Authenticate user |
| GET | `/user` | Get current user |
| PUT | `/user` | Update current user |
| GET | `/profiles/{username}` | Get user profile |
| POST | `/profiles/{username}/follow` | Follow user |
| DELETE | `/profiles/{username}/follow` | Unfollow user |
| GET | `/articles` | List articles |
| GET | `/articles/feed` | Get user feed |
| POST | `/articles` | Create article |
| GET | `/articles/{slug}` | Get article |
| PUT | `/articles/{slug}` | Update article |
| DELETE | `/articles/{slug}` | Delete article |
| POST | `/articles/{slug}/favorite` | Favorite article |
| DELETE | `/articles/{slug}/favorite` | Unfavorite article |
| GET | `/articles/{slug}/comments` | Get comments |
| POST | `/articles/{slug}/comments` | Create comment |
| DELETE | `/articles/{slug}/comments/{id}` | Delete comment |
| GET | `/tags` | Get all tags |

### GraphQL API

The GraphQL API is available at `/graphql` with the following operations:

**Queries:**
- `article(slug: String!)`: Get single article
- `articles(...)`: List articles with filters and pagination
- `feed(...)`: Get user feed
- `me`: Get current user
- `profile(username: String!)`: Get user profile
- `tags`: Get all tags

**Mutations:**
- `createUser(input: CreateUserInput)`: Register user
- `login(email: String!, password: String!)`: Authenticate
- `updateUser(changes: UpdateUserInput!)`: Update profile
- `followUser(username: String!)`: Follow user
- `unfollowUser(username: String!)`: Unfollow user
- `createArticle(input: CreateArticleInput!)`: Create article
- `updateArticle(slug: String!, changes: UpdateArticleInput!)`: Update article
- `deleteArticle(slug: String!)`: Delete article
- `favoriteArticle(slug: String!)`: Favorite article
- `unfavoriteArticle(slug: String!)`: Unfavorite article
- `addComment(slug: String!, body: String!)`: Add comment
- `deleteComment(slug: String!, id: ID!)`: Delete comment

## Benefits of This Approach

1. **Single Source of Truth**: Business logic is centralized in the facade layer
2. **Easier Testing**: Facade methods can be unit tested independently
3. **Consistent Behavior**: Both APIs behave identically for the same operations
4. **Reduced Code Duplication**: Authorization and validation logic is shared
5. **Flexible Evolution**: New API types (gRPC, WebSocket) can easily consume the facade layer

## Migration Notes

- All existing REST endpoints remain unchanged
- All existing GraphQL operations remain unchanged
- No breaking changes to API contracts
- Internal implementation refactored for better maintainability
