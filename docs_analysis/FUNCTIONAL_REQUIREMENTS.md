# Functional Requirements - Deep Discovery Report

**Repository:** `Cognition-Partner-Workshops/spring-boot-realworld-example-app`
**Analysis Date:** 2026-02-25
**Application Name:** RealWorld Example App (Conduit)

---

## Table of Contents

1. [Architecture Overview](#1-architecture-overview)
2. [Tech Stack Summary](#2-tech-stack-summary)
3. [Component Connection Map](#3-component-connection-map)
4. [Database Schema](#4-database-schema)
5. [Functional Requirements by Module](#5-functional-requirements-by-module)
6. [API Endpoint Inventory](#6-api-endpoint-inventory)
7. [GraphQL API Inventory](#7-graphql-api-inventory)
8. [Validation Logic - Account Creation Module](#8-validation-logic---account-creation-module)
9. [Validation Logic - Money Transfer Module](#9-validation-logic---money-transfer-module)
10. [Authorization & Security Rules](#10-authorization--security-rules)
11. [Seed Data & Default Configuration](#11-seed-data--default-configuration)
12. [Frontend Summary](#12-frontend-summary)

---

## 1. Architecture Overview

This application is a **"RealWorld" (Conduit)** blogging platform -- a Medium.com clone -- implementing a full-stack content management system with user authentication, article publishing, social features (following, favoriting), commenting, and tagging. The backend exposes both a **REST API** and a **GraphQL API**, and includes a **Next.js frontend** SPA.

The architecture follows a **layered / clean architecture** pattern:

```
[Frontend: Next.js SPA]
        |
        v
[API Layer: REST Controllers + GraphQL DGS Data Fetchers]
        |
        v
[Application Layer: Services, Query Services, Command Services, Validators]
        |
        v
[Core/Domain Layer: Entities, Repository Interfaces, Domain Services]
        |
        v
[Infrastructure Layer: MyBatis Mappers, Repository Implementations, JWT Service]
        |
        v
[Database: SQLite via JDBC]
```

---

## 2. Tech Stack Summary

| Component          | Technology                                     |
|--------------------|------------------------------------------------|
| **Language**       | Java 11                                        |
| **Framework**      | Spring Boot 2.6.3                              |
| **Build Tool**     | Gradle                                         |
| **Database**       | SQLite (via `sqlite-jdbc 3.36.0.3`)            |
| **ORM / SQL**      | MyBatis (`mybatis-spring-boot-starter 2.2.2`)  |
| **DB Migrations**  | Flyway (`flywaydb:flyway-core`)                |
| **Authentication** | JWT (`jjwt 0.11.2`) + Spring Security          |
| **Password Hash**  | BCrypt (`BCryptPasswordEncoder`)               |
| **REST API**       | Spring MVC (`spring-boot-starter-web`)         |
| **GraphQL API**    | Netflix DGS Framework (`graphql-dgs 4.9.21`)   |
| **Validation**     | Bean Validation / Hibernate Validator (`spring-boot-starter-validation`) |
| **HATEOAS**        | `spring-boot-starter-hateoas`                  |
| **Date/Time**      | Joda-Time 2.10.13                              |
| **Code Gen**       | Lombok (compile-time annotations)              |
| **Code Format**    | Spotless (Google Java Format)                  |
| **Code Coverage**  | JaCoCo (minimum 80% coverage enforced)         |
| **Testing**        | JUnit 5, Mockito, REST Assured, Spring MockMvc |
| **E2E Testing**    | Selenium 4.15.0, TestNG 7.8.0, WebDriverManager |
| **Frontend**       | Next.js 9.x, React 16.13, TypeScript, SWR, Axios |
| **JSON Handling**  | Jackson (with `UNWRAP_ROOT_VALUE` enabled)     |

---

## 3. Component Connection Map

### REST API Controllers (`io.spring.api`)

| Controller           | Path Prefix                    | Dependencies                                           |
|----------------------|--------------------------------|--------------------------------------------------------|
| `UsersApi`           | `/users`                       | UserRepository, UserQueryService, PasswordEncoder, JwtService, UserService |
| `CurrentUserApi`     | `/user`                        | UserQueryService, UserService                          |
| `ArticlesApi`        | `/articles`                    | ArticleCommandService, ArticleQueryService             |
| `ArticleApi`         | `/articles/{slug}`             | ArticleQueryService, ArticleRepository, ArticleCommandService |
| `CommentsApi`        | `/articles/{slug}/comments`    | ArticleRepository, CommentRepository, CommentQueryService |
| `ArticleFavoriteApi` | `/articles/{slug}/favorite`    | ArticleFavoriteRepository, ArticleRepository, ArticleQueryService |
| `ProfileApi`         | `/profiles/{username}`         | ProfileQueryService, UserRepository                    |
| `TagsApi`            | `/tags`                        | TagsQueryService                                       |

### GraphQL Mutations (`io.spring.graphql`)

| Component            | Operations                                        |
|----------------------|---------------------------------------------------|
| `UserMutation`       | createUser, login, updateUser                     |
| `ArticleMutation`    | createArticle, updateArticle, favoriteArticle, unfavoriteArticle, deleteArticle |
| `CommentMutation`    | addComment, deleteComment                         |
| `RelationMutation`   | followUser, unfollowUser                          |

### GraphQL Queries / Data Fetchers (`io.spring.graphql`)

| Component              | Operations                                     |
|------------------------|------------------------------------------------|
| `ArticleDatafetcher`   | article, articles, feed                        |
| `CommentDatafetcher`   | comments on articles                           |
| `MeDatafetcher`        | me (current user)                              |
| `ProfileDatafetcher`   | profile, articles by profile, favorites, feed  |
| `TagDatafetcher`       | tags                                           |

### Application Services (`io.spring.application`)

| Service                   | Responsibility                                       |
|---------------------------|------------------------------------------------------|
| `UserService`             | User creation (with password encoding), user update  |
| `ArticleCommandService`   | Article creation and update (with validation)        |
| `ArticleQueryService`     | Article querying, feed, filtering, pagination        |
| `CommentQueryService`     | Comment retrieval by article or by ID                |
| `ProfileQueryService`     | Profile lookup with follow status                    |
| `UserQueryService`        | User data retrieval by ID                            |
| `TagsQueryService`        | Retrieve all tags                                    |

### Infrastructure (`io.spring.infrastructure`)

| Component                         | Responsibility                        |
|-----------------------------------|---------------------------------------|
| `MyBatisUserRepository`           | User + Follow persistence via MyBatis |
| `MyBatisArticleRepository`        | Article + Tag persistence via MyBatis |
| `MyBatisCommentRepository`        | Comment persistence via MyBatis       |
| `MyBatisArticleFavoriteRepository`| Article favorite persistence          |
| `DefaultJwtService`               | JWT token generation and validation   |

---

## 4. Database Schema

Defined in `V1__create_tables.sql` (Flyway migration):

| Table               | Columns                                                         | Purpose                         |
|----------------------|-----------------------------------------------------------------|---------------------------------|
| `users`             | id (PK), username (UNIQUE), password, email (UNIQUE), bio, image | User accounts                   |
| `articles`          | id (PK), user_id (FK), slug (UNIQUE), title, description, body, created_at, updated_at | Published articles              |
| `article_favorites` | article_id, user_id (composite PK)                              | User-article favorite relations |
| `follows`           | user_id, follow_id                                              | User-user follow relations      |
| `tags`              | id (PK), name                                                   | Tag definitions                 |
| `article_tags`      | article_id, tag_id                                              | Article-tag associations        |
| `comments`          | id (PK), body, article_id, user_id, created_at, updated_at     | Article comments                |

---

## 5. Functional Requirements by Module

### FR-1: User Registration (Account Creation)

- **Endpoint:** `POST /users`
- **GraphQL:** `mutation createUser`
- **Description:** Allow new users to register with email, username, and password.
- **Business Rules:**
  - Email is required, must be non-blank, and must be a valid email format.
  - Username is required and must be non-blank.
  - Password is required and must be non-blank.
  - Email must be unique across the system (no duplicate emails).
  - Username must be unique across the system (no duplicate usernames).
  - Password is hashed using BCrypt before storage.
  - A default profile image is assigned from configuration (`image.default`).
  - Bio is initialized as an empty string.
  - A JWT token is returned upon successful registration.
  - User ID is generated as a UUID.

### FR-2: User Authentication (Login)

- **Endpoint:** `POST /users/login`
- **GraphQL:** `mutation login`
- **Description:** Authenticate existing users via email and password.
- **Business Rules:**
  - Email is required, must be non-blank, and must be a valid email format.
  - Password is required and must be non-blank.
  - Password is verified against the stored BCrypt hash.
  - Returns a JWT token on successful authentication.
  - Returns 422 (Unprocessable Entity) with error message on invalid credentials.

### FR-3: Get Current User

- **Endpoint:** `GET /user`
- **GraphQL:** `query me`
- **Description:** Retrieve the currently authenticated user's profile.
- **Business Rules:**
  - Requires valid JWT authentication.
  - Returns user data along with the current token.

### FR-4: Update Current User Profile

- **Endpoint:** `PUT /user`
- **GraphQL:** `mutation updateUser`
- **Description:** Update the authenticated user's profile information.
- **Business Rules:**
  - Requires valid JWT authentication.
  - Updatable fields: email, username, password, bio, image.
  - Only non-empty fields are updated (partial update pattern).
  - If email is changed, it must not conflict with another existing user's email.
  - If username is changed, it must not conflict with another existing user's username.
  - Email field (if provided) must be a valid email format.

### FR-5: User Profile Viewing

- **Endpoint:** `GET /profiles/{username}`
- **GraphQL:** `query profile`
- **Description:** View a user's public profile.
- **Business Rules:**
  - Returns username, bio, image, and follow status.
  - If authenticated, the response includes whether the current user is following the profile owner.
  - Returns 404 if the username does not exist.

### FR-6: Follow User

- **Endpoint:** `POST /profiles/{username}/follow`
- **GraphQL:** `mutation followUser`
- **Description:** Follow another user.
- **Business Rules:**
  - Requires valid JWT authentication.
  - Creates a follow relation from the current user to the target user.
  - Returns 404 if the target username does not exist.

### FR-7: Unfollow User

- **Endpoint:** `DELETE /profiles/{username}/follow`
- **GraphQL:** `mutation unfollowUser`
- **Description:** Unfollow a previously followed user.
- **Business Rules:**
  - Requires valid JWT authentication.
  - Removes the follow relation.
  - Returns 404 if the target user or follow relation does not exist.

### FR-8: Create Article

- **Endpoint:** `POST /articles`
- **GraphQL:** `mutation createArticle`
- **Description:** Publish a new article.
- **Business Rules:**
  - Requires valid JWT authentication.
  - Title is required and must be non-blank.
  - Description is required and must be non-blank.
  - Body is required and must be non-blank.
  - Tag list is optional.
  - Article title must be unique (checked via slug uniqueness -- title is converted to a slug).
  - Slug is auto-generated from the title (lowercase, special characters replaced with hyphens).
  - Tags are de-duplicated (stored as a `HashSet`).
  - Article ID is generated as a UUID.
  - `createdAt` and `updatedAt` timestamps are set automatically.

### FR-9: Get Article by Slug

- **Endpoint:** `GET /articles/{slug}`
- **GraphQL:** `query article`
- **Description:** Retrieve a single article by its slug.
- **Business Rules:**
  - Publicly accessible (no authentication required).
  - If authenticated, includes favorited status and author follow status.
  - Returns 404 if the slug does not match any article.

### FR-10: List / Filter Articles

- **Endpoint:** `GET /articles`
- **GraphQL:** `query articles`
- **Description:** List articles with optional filtering.
- **Business Rules:**
  - Publicly accessible.
  - Supports filtering by: tag, author username, favorited-by username.
  - Supports pagination via `offset` (default: 0) and `limit` (default: 20).
  - GraphQL supports cursor-based pagination (first/after/last/before).
  - Returns articles count alongside articles list.

### FR-11: Article Feed

- **Endpoint:** `GET /articles/feed`
- **GraphQL:** `query feed`
- **Description:** Get articles from users the current user follows.
- **Business Rules:**
  - Requires valid JWT authentication.
  - Returns articles authored by followed users only.
  - Supports pagination via `offset`/`limit` (REST) or cursor-based (GraphQL).
  - Returns empty list if the user follows no one.

### FR-12: Update Article

- **Endpoint:** `PUT /articles/{slug}`
- **GraphQL:** `mutation updateArticle`
- **Description:** Update an existing article.
- **Business Rules:**
  - Requires valid JWT authentication.
  - Only the article author can update the article (ownership check).
  - Updatable fields: title, description, body (partial update -- only non-empty fields applied).
  - If title is updated, the slug is regenerated.
  - `updatedAt` timestamp is refreshed on any field change.
  - Returns 403 if the user is not the article author.
  - Returns 404 if the article slug does not exist.

### FR-13: Delete Article

- **Endpoint:** `DELETE /articles/{slug}`
- **GraphQL:** `mutation deleteArticle`
- **Description:** Delete an article.
- **Business Rules:**
  - Requires valid JWT authentication.
  - Only the article author can delete the article (ownership check).
  - Returns 403 if the user is not the article author.
  - Returns 404 if the article does not exist.
  - Returns 204 No Content (REST) or `DeletionStatus { success: true }` (GraphQL) on success.

### FR-14: Favorite Article

- **Endpoint:** `POST /articles/{slug}/favorite`
- **GraphQL:** `mutation favoriteArticle`
- **Description:** Mark an article as a favorite.
- **Business Rules:**
  - Requires valid JWT authentication.
  - Creates a favorite relation between the current user and the article.
  - Returns 404 if the article does not exist.
  - Returns the updated article data (with incremented favorites count).

### FR-15: Unfavorite Article

- **Endpoint:** `DELETE /articles/{slug}/favorite`
- **GraphQL:** `mutation unfavoriteArticle`
- **Description:** Remove an article from favorites.
- **Business Rules:**
  - Requires valid JWT authentication.
  - Removes the favorite relation if it exists.
  - Returns 404 if the article does not exist.
  - Returns the updated article data (with decremented favorites count).

### FR-16: Add Comment to Article

- **Endpoint:** `POST /articles/{slug}/comments`
- **GraphQL:** `mutation addComment`
- **Description:** Add a comment to an article.
- **Business Rules:**
  - Requires valid JWT authentication.
  - Comment body is required and must be non-blank.
  - Comment is associated with both the article and the commenting user.
  - Comment ID is generated as a UUID.
  - `createdAt` timestamp is set automatically.
  - Returns 404 if the article does not exist.

### FR-17: List Comments on Article

- **Endpoint:** `GET /articles/{slug}/comments`
- **GraphQL:** Via `Article.comments` field
- **Description:** Retrieve all comments for a given article.
- **Business Rules:**
  - Accessible without authentication.
  - Returns 404 if the article does not exist.
  - Each comment includes the author's profile data.

### FR-18: Delete Comment

- **Endpoint:** `DELETE /articles/{slug}/comments/{id}`
- **GraphQL:** `mutation deleteComment`
- **Description:** Delete a comment from an article.
- **Business Rules:**
  - Requires valid JWT authentication.
  - Only the comment author OR the article author can delete the comment.
  - Returns 403 if the user lacks authorization.
  - Returns 404 if the article or comment does not exist.

### FR-19: List Tags

- **Endpoint:** `GET /tags`
- **GraphQL:** `query tags`
- **Description:** Retrieve all available tags.
- **Business Rules:**
  - Publicly accessible (no authentication required).
  - Returns a list of all tag names in the system.

---

## 6. API Endpoint Inventory (REST)

| Method   | Path                                | Auth Required | Description                     |
|----------|-------------------------------------|---------------|---------------------------------|
| `POST`   | `/users`                            | No            | Register a new user             |
| `POST`   | `/users/login`                      | No            | Login / authenticate            |
| `GET`    | `/user`                             | Yes           | Get current user                |
| `PUT`    | `/user`                             | Yes           | Update current user profile     |
| `GET`    | `/profiles/{username}`              | No            | Get user profile                |
| `POST`   | `/profiles/{username}/follow`       | Yes           | Follow a user                   |
| `DELETE` | `/profiles/{username}/follow`       | Yes           | Unfollow a user                 |
| `POST`   | `/articles`                         | Yes           | Create an article               |
| `GET`    | `/articles`                         | No            | List/filter articles            |
| `GET`    | `/articles/feed`                    | Yes           | Get user's personalized feed    |
| `GET`    | `/articles/{slug}`                  | No            | Get single article              |
| `PUT`    | `/articles/{slug}`                  | Yes           | Update article                  |
| `DELETE` | `/articles/{slug}`                  | Yes           | Delete article                  |
| `POST`   | `/articles/{slug}/favorite`         | Yes           | Favorite an article             |
| `DELETE` | `/articles/{slug}/favorite`         | Yes           | Unfavorite an article           |
| `POST`   | `/articles/{slug}/comments`         | Yes           | Add comment to article          |
| `GET`    | `/articles/{slug}/comments`         | No            | List comments on article        |
| `DELETE` | `/articles/{slug}/comments/{id}`    | Yes           | Delete a comment                |
| `GET`    | `/tags`                             | No            | List all tags                   |

---

## 7. GraphQL API Inventory

### Queries

| Query                       | Arguments                                           | Auth | Description                 |
|-----------------------------|-----------------------------------------------------|------|-----------------------------|
| `article(slug)`             | `slug: String!`                                     | No   | Get single article          |
| `articles(...)`             | `first, after, last, before, authoredBy, favoritedBy, withTag` | No | List/filter articles |
| `feed(...)`                 | `first, after, last, before`                        | Yes  | User's personalized feed    |
| `me`                        | _(none)_                                            | Yes  | Current authenticated user  |
| `profile(username)`         | `username: String!`                                 | No   | Get user profile            |
| `tags`                      | _(none)_                                            | No   | List all tags               |

### Mutations

| Mutation                     | Arguments                                          | Auth | Description              |
|------------------------------|-----------------------------------------------------|------|--------------------------|
| `createUser(input)`          | `CreateUserInput { email, username, password }`     | No   | Register user            |
| `login(email, password)`     | `email: String!, password: String!`                 | No   | Authenticate user        |
| `updateUser(changes)`        | `UpdateUserInput { email, username, password, image, bio }` | Yes | Update profile    |
| `followUser(username)`       | `username: String!`                                 | Yes  | Follow user              |
| `unfollowUser(username)`     | `username: String!`                                 | Yes  | Unfollow user            |
| `createArticle(input)`       | `CreateArticleInput { body, description, tagList, title }` | Yes | Create article     |
| `updateArticle(slug, changes)` | `slug: String!, UpdateArticleInput { body, description, title }` | Yes | Update article |
| `favoriteArticle(slug)`      | `slug: String!`                                     | Yes  | Favorite article         |
| `unfavoriteArticle(slug)`    | `slug: String!`                                     | Yes  | Unfavorite article       |
| `deleteArticle(slug)`        | `slug: String!`                                     | Yes  | Delete article           |
| `addComment(slug, body)`     | `slug: String!, body: String!`                      | Yes  | Add comment              |
| `deleteComment(slug, id)`    | `slug: String!, id: ID!`                            | Yes  | Delete comment           |

---

## 8. Validation Logic - Account Creation Module

### Registration (`RegisterParam` + `UserService.createUser`)

**Source files:**
- `io.spring.application.user.RegisterParam`
- `io.spring.application.user.UserService`
- `io.spring.application.user.DuplicatedEmailConstraint` / `DuplicatedEmailValidator`
- `io.spring.application.user.DuplicatedUsernameConstraint` / `DuplicatedUsernameValidator`

| Field      | Validation Rule                  | Annotation / Validator                | Error Message             |
|------------|----------------------------------|---------------------------------------|---------------------------|
| `email`    | Must not be blank                | `@NotBlank`                           | `"can't be empty"`        |
| `email`    | Must be valid email format       | `@Email`                              | `"should be an email"`    |
| `email`    | Must not already exist in DB     | `@DuplicatedEmailConstraint` -> `DuplicatedEmailValidator` | `"duplicated email"` |
| `username` | Must not be blank                | `@NotBlank`                           | `"can't be empty"`        |
| `username` | Must not already exist in DB     | `@DuplicatedUsernameConstraint` -> `DuplicatedUsernameValidator` | `"duplicated username"` |
| `password` | Must not be blank                | `@NotBlank`                           | `"can't be empty"`        |

**Validation Flow:**
1. Spring's `@Valid` annotation triggers Bean Validation on `RegisterParam`.
2. `@NotBlank` and `@Email` are standard JSR-380 annotations checked first.
3. `DuplicatedEmailValidator.isValid()` queries `UserRepository.findByEmail()` -- returns false if the email is already taken.
4. `DuplicatedUsernameValidator.isValid()` queries `UserRepository.findByUsername()` -- returns false if the username is already taken.
5. If any validation fails, a `ConstraintViolationException` is thrown, caught by `CustomizeExceptionHandler`, and returned as a 422 response with structured error details.

**Post-Validation Business Logic:**
- Password is encoded via `BCryptPasswordEncoder` before being stored.
- A UUID is assigned as the user ID.
- Default image is loaded from application property `image.default`.
- Bio is set to empty string.
- User is saved via `UserRepository.save()`.

### Profile Update (`UpdateUserCommand` + `UpdateUserParam` + `UserService.updateUser`)

**Source files:**
- `io.spring.application.user.UpdateUserParam`
- `io.spring.application.user.UpdateUserCommand`
- `io.spring.application.user.UserService` (includes `UpdateUserValidator`)

| Field      | Validation Rule                        | Annotation / Validator                  | Error Message              |
|------------|----------------------------------------|-----------------------------------------|----------------------------|
| `email`    | Must be valid email format (if set)    | `@Email`                                | `"should be an email"`     |
| _(command)_| Email must not conflict with other user | `@UpdateUserConstraint` -> `UpdateUserValidator` | `"email already exist"` |
| _(command)_| Username must not conflict with other user | `@UpdateUserConstraint` -> `UpdateUserValidator` | `"username already exist"` |

**Validation Flow:**
1. `UpdateUserCommand` is annotated with `@UpdateUserConstraint`.
2. `UpdateUserValidator.isValid()` checks:
   - If the new email is already used by a *different* user (allows the same user to keep their email).
   - If the new username is already used by a *different* user (allows the same user to keep their username).
3. The `User.update()` method applies only non-empty fields (partial update pattern using `Util.isEmpty()` checks).

---

## 9. Validation Logic - Money Transfer Module

### Finding: No Money Transfer Module Exists

After a thorough scan of the entire codebase, **no "Money Transfer" module was found**. This application is a blogging/content platform (Medium.com clone) and does not contain any banking, financial, or money transfer functionality.

Specifically, the following were checked:
- All Java source files in `src/main/java/` -- no classes related to transfers, payments, accounts (financial), or transactions.
- All database migrations (`V1__create_tables.sql`, `V2__seed_data.sql`) -- no tables for accounts, balances, transfers, or transactions.
- All MyBatis mapper XML files -- the `TransferData.xml` file contains only shared result mappings for `ArticleData`, `CommentData`, `ProfileData`, and `ArticleFavoriteCount` (not related to money transfers).
- All REST endpoints and GraphQL schema -- no financial endpoints.
- The GraphQL schema (`schema.graphqls`) -- only blogging-related types.

**Conclusion:** The repository name `TransferData.xml` is a misnomer -- it refers to data transfer objects (DTOs) / result-set mappings, not financial transfers. The "Money Transfer" requirement does not apply to this codebase.

---

## 10. Authorization & Security Rules

### JWT Authentication

**Source files:**
- `io.spring.infrastructure.service.DefaultJwtService`
- `io.spring.api.security.JwtTokenFilter`
- `io.spring.api.security.WebSecurityConfig`

| Rule                           | Detail                                                        |
|--------------------------------|---------------------------------------------------------------|
| Algorithm                      | HMAC-SHA512 (`HS512`)                                         |
| Token payload                  | Subject = User ID                                             |
| Token expiry                   | Configurable via `jwt.sessionTime` (default: 86400 seconds = 24 hours) |
| Token header format            | `Authorization: Token <jwt>` (space-separated, second part extracted) |
| Session management             | Stateless (no server-side session)                            |

### Endpoint Security (Spring Security)

| Pattern                                       | Access Level      |
|-----------------------------------------------|-------------------|
| `OPTIONS /**`                                 | Permit all (CORS) |
| `GET /articles/**`                            | Permit all        |
| `GET /profiles/**`                            | Permit all        |
| `GET /tags`                                   | Permit all        |
| `POST /users`, `POST /users/login`           | Permit all        |
| `/graphiql`, `/graphql`                       | Permit all        |
| `GET /articles/feed`                          | Authenticated     |
| All other requests                            | Authenticated     |

### Resource Authorization Rules

**Source: `io.spring.core.service.AuthorizationService`**

| Operation         | Rule                                                             |
|-------------------|------------------------------------------------------------------|
| Update article    | Only the article author (`user.id == article.userId`)            |
| Delete article    | Only the article author (`user.id == article.userId`)            |
| Delete comment    | Article author OR comment author (`user.id == article.userId` OR `user.id == comment.userId`) |

### CORS Configuration

- Allowed origins: `*` (all origins)
- Allowed methods: `HEAD, GET, POST, PUT, DELETE, PATCH`
- Allowed headers: `Authorization, Cache-Control, Content-Type`
- Credentials: Disabled

### Error Handling

**Source: `io.spring.api.exception.CustomizeExceptionHandler`**

| Exception                        | HTTP Status | Response Format                           |
|----------------------------------|-------------|-------------------------------------------|
| `InvalidRequestException`        | 422         | Structured field errors                   |
| `InvalidAuthenticationException` | 422         | `{ message: "..." }`                     |
| `MethodArgumentNotValidException`| 422         | Structured field errors                   |
| `ConstraintViolationException`   | 422         | Structured field errors                   |
| `ResourceNotFoundException`      | 404         | _(handled by Spring defaults)_            |
| `NoAuthorizationException`       | 403         | _(handled by Spring defaults)_            |
| Unauthorized (no/invalid token)  | 401         | _(via HttpStatusEntryPoint)_              |

---

## 11. Seed Data & Default Configuration

### Seed Users (V2__seed_data.sql)

| Username   | Email              | Default Password | Bio                                           |
|------------|--------------------|--------------------|-----------------------------------------------|
| `johndoe`  | john@example.com   | `password123`      | Full-stack developer and tech enthusiast       |
| `janedoe`  | jane@example.com   | `password123`      | Software architect passionate about clean code |
| `bobsmith` | bob@example.com    | `password123`      | DevOps engineer and cloud enthusiast           |

### Seed Articles (5 total)

- "Getting Started with Spring Boot" (by johndoe)
- "REST API Best Practices" (by janedoe)
- "Microservices Architecture Guide" (by johndoe)
- "Docker for Java Developers" (by bobsmith)
- "Testing Spring Boot Applications" (by janedoe)

### Seed Tags (7 total)

`java`, `spring-boot`, `web-development`, `tutorial`, `best-practices`, `microservices`, `api-design`

### Seed Follow Relations

- johndoe follows janedoe
- janedoe follows johndoe
- bobsmith follows johndoe and janedoe

### Application Configuration Defaults

| Property              | Value                                                              |
|-----------------------|--------------------------------------------------------------------|
| Database URL          | `jdbc:sqlite:dev.db`                                              |
| Default user image    | `https://static.productionready.io/images/smiley-cyrus.jpg`       |
| JWT session time      | 86400 seconds (24 hours)                                          |
| MyBatis caching       | Enabled                                                            |
| Statement timeout     | 3000ms                                                             |
| Underscore-to-camel   | Enabled                                                            |

---

## 12. Frontend Summary

The frontend is a **Next.js** (v9.x) single-page application located in the `/frontend` directory.

| Component   | Technology                |
|-------------|---------------------------|
| Framework   | Next.js 9.5               |
| UI Library  | React 16.13               |
| Language    | TypeScript                |
| HTTP Client | Axios                     |
| Data Fetch  | SWR (stale-while-revalidate) |
| Markdown    | marked                    |

### Frontend Pages

| Route Path           | Description               |
|----------------------|---------------------------|
| `/`                  | Home page / article feed  |
| `/article/*`         | Article detail pages      |
| `/editor/*`          | Article editor            |
| `/profile/*`         | User profile pages        |
| `/user/*`            | User auth (login/register)|

---

## Appendix: Article Slug Generation Logic

**Source:** `Article.toSlug(String title)`

```java
title.toLowerCase().replaceAll("[\\&|[\\uFE30-\\uFFA0]|\\'|\\"|\\s\\?\\,\\.]+", "-")
```

This converts the title to lowercase and replaces the following characters with hyphens:
- Ampersands (`&`)
- CJK compatibility forms and halfwidth/fullwidth forms (Unicode range `FE30-FFA0`)
- Single quotes, double quotes
- Whitespace, question marks, commas, periods

### Article Duplication Check

**Source:** `DuplicatedArticleValidator`

The article title is converted to a slug via `Article.toSlug()`, and the validator checks if any existing article already has that slug via `ArticleQueryService.findBySlug()`. This prevents articles with titles that would produce identical slugs.
