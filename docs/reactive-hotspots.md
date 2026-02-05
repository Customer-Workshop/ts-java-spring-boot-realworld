# Reactive Hotspots Analysis

## Executive Summary

This document identifies all blocking code patterns in the spring-boot-realworld-example-app codebase that need to be addressed for the reactive migration. The application currently uses a traditional blocking servlet-based architecture with Spring MVC and MyBatis/JDBC.

**Current Architecture:** Blocking (Servlet-based)
**Target Architecture:** Reactive (Spring WebFlux with Reactor)

---

## 1. Web Layer - Blocking Controllers

### Current State
All REST controllers use Spring MVC with blocking `ResponseEntity` return types.

| File | Class | Issue | Recommended Refactor |
|------|-------|-------|---------------------|
| `src/main/java/io/spring/api/ArticlesApi.java` | ArticlesApi | Returns `ResponseEntity` (blocking) | Convert to `Mono<ResponseEntity>` |
| `src/main/java/io/spring/api/ArticleApi.java` | ArticleApi | Returns `ResponseEntity` (blocking) | Convert to `Mono<ResponseEntity>` |
| `src/main/java/io/spring/api/UsersApi.java` | UsersApi | Returns `ResponseEntity` (blocking) | Convert to `Mono<ResponseEntity>` |
| `src/main/java/io/spring/api/CurrentUserApi.java` | CurrentUserApi | Returns `ResponseEntity` (blocking) | Convert to `Mono<ResponseEntity>` |
| `src/main/java/io/spring/api/ProfileApi.java` | ProfileApi | Returns `ResponseEntity` (blocking) | Convert to `Mono<ResponseEntity>` |
| `src/main/java/io/spring/api/CommentsApi.java` | CommentsApi | Returns `ResponseEntity` (blocking) | Convert to `Mono<ResponseEntity>` |
| `src/main/java/io/spring/api/TagsApi.java` | TagsApi | Returns `ResponseEntity` (blocking) | Convert to `Mono<ResponseEntity>` |
| `src/main/java/io/spring/api/ArticleFavoriteApi.java` | ArticleFavoriteApi | Returns `ResponseEntity` (blocking) | Convert to `Mono<ResponseEntity>` |

### Refactor Approach
1. Replace `@RestController` with WebFlux-compatible handlers
2. Change return types from `ResponseEntity<T>` to `Mono<ResponseEntity<T>>`
3. Update `@AuthenticationPrincipal User` to work with reactive security context

---

## 2. Data Access Layer - Blocking JDBC/MyBatis

### Current State
The application uses MyBatis with SQLite JDBC driver, which is inherently blocking.

| File | Class | Issue | Recommended Refactor |
|------|-------|-------|---------------------|
| `src/main/java/io/spring/infrastructure/repository/MyBatisArticleRepository.java` | MyBatisArticleRepository | Blocking JDBC calls via MyBatis | Option A: R2DBC with H2, Option B: Isolate with `Schedulers.boundedElastic()` |
| `src/main/java/io/spring/infrastructure/repository/MyBatisUserRepository.java` | MyBatisUserRepository | Blocking JDBC calls via MyBatis | Same as above |
| `src/main/java/io/spring/infrastructure/repository/MyBatisCommentRepository.java` | MyBatisCommentRepository | Blocking JDBC calls via MyBatis | Same as above |
| `src/main/java/io/spring/infrastructure/repository/MyBatisArticleFavoriteRepository.java` | MyBatisArticleFavoriteRepository | Blocking JDBC calls via MyBatis | Same as above |

### MyBatis Mappers (Blocking)
| File | Interface | Issue |
|------|-----------|-------|
| `src/main/java/io/spring/infrastructure/mybatis/mapper/ArticleMapper.java` | ArticleMapper | Blocking SQL operations |
| `src/main/java/io/spring/infrastructure/mybatis/mapper/UserMapper.java` | UserMapper | Blocking SQL operations |
| `src/main/java/io/spring/infrastructure/mybatis/mapper/CommentMapper.java` | CommentMapper | Blocking SQL operations |

### MyBatis Read Services (Blocking)
| File | Class | Issue |
|------|-------|-------|
| `src/main/java/io/spring/infrastructure/mybatis/readservice/ArticleReadService.java` | ArticleReadService | Blocking read operations |
| `src/main/java/io/spring/infrastructure/mybatis/readservice/UserReadService.java` | UserReadService | Blocking read operations |
| `src/main/java/io/spring/infrastructure/mybatis/readservice/TagReadService.java` | TagReadService | Blocking read operations |
| `src/main/java/io/spring/infrastructure/mybatis/readservice/ArticleFavoritesReadService.java` | ArticleFavoritesReadService | Blocking read operations |
| `src/main/java/io/spring/infrastructure/mybatis/readservice/UserRelationshipQueryService.java` | UserRelationshipQueryService | Blocking read operations |

### Refactor Approach
**Option A (Preferred): R2DBC Migration**
- Replace SQLite with H2 (has R2DBC support)
- Replace MyBatis with Spring Data R2DBC
- Convert repository interfaces to return `Mono<T>` and `Flux<T>`

**Option B: Bounded Elastic Isolation**
- Keep MyBatis/JDBC for complex queries
- Wrap blocking calls with `Mono.fromCallable(...).subscribeOn(Schedulers.boundedElastic())`
- Mark as tech debt for future migration

---

## 3. Application Services - Blocking Operations

### Current State
Application services use blocking patterns with `Optional<T>` returns.

| File | Class | Issue | Recommended Refactor |
|------|-------|-------|---------------------|
| `src/main/java/io/spring/application/ArticleQueryService.java` | ArticleQueryService | Returns `Optional<ArticleData>` | Return `Mono<ArticleData>` |
| `src/main/java/io/spring/application/CommentQueryService.java` | CommentQueryService | Returns `Optional<CommentData>` | Return `Mono<CommentData>` |
| `src/main/java/io/spring/application/ProfileQueryService.java` | ProfileQueryService | Returns `Optional<ProfileData>` | Return `Mono<ProfileData>` |
| `src/main/java/io/spring/application/UserQueryService.java` | UserQueryService | Returns `Optional<UserData>` | Return `Mono<UserData>` |
| `src/main/java/io/spring/application/TagsQueryService.java` | TagsQueryService | Returns `List<String>` | Return `Flux<String>` |
| `src/main/java/io/spring/application/article/ArticleCommandService.java` | ArticleCommandService | Blocking save operations | Return `Mono<Article>` |
| `src/main/java/io/spring/application/user/UserService.java` | UserService | Blocking user operations | Return `Mono<User>` |

---

## 4. Security Layer - Blocking Authentication

### Current State
JWT authentication uses blocking filter chain.

| File | Class | Issue | Recommended Refactor |
|------|-------|-------|---------------------|
| `src/main/java/io/spring/api/security/JwtTokenFilter.java` | JwtTokenFilter | Extends `OncePerRequestFilter` (blocking) | Convert to `WebFilter` for WebFlux |
| `src/main/java/io/spring/api/security/WebSecurityConfig.java` | WebSecurityConfig | Uses servlet-based security config | Convert to reactive security config |
| `src/main/java/io/spring/infrastructure/service/DefaultJwtService.java` | DefaultJwtService | Blocking JWT operations | Wrap with reactive types if needed |

---

## 5. GraphQL Layer - DGS Framework

### Current State
Netflix DGS framework is used for GraphQL. DGS supports both blocking and reactive patterns.

| File | Class | Issue | Recommended Refactor |
|------|-------|-------|---------------------|
| `src/main/java/io/spring/graphql/UserMutation.java` | UserMutation | Blocking data fetchers | Return `Mono<T>` from data fetchers |
| `src/main/java/io/spring/graphql/ArticleMutation.java` | ArticleMutation | Blocking data fetchers | Return `Mono<T>` from data fetchers |
| `src/main/java/io/spring/graphql/ArticleDatafetcher.java` | ArticleDatafetcher | Blocking data fetchers | Return `Mono<T>` or `Flux<T>` |
| `src/main/java/io/spring/graphql/CommentDatafetcher.java` | CommentDatafetcher | Blocking data fetchers | Return `Mono<T>` or `Flux<T>` |
| `src/main/java/io/spring/graphql/ProfileDatafetcher.java` | ProfileDatafetcher | Blocking data fetchers | Return `Mono<T>` |
| `src/main/java/io/spring/graphql/TagDatafetcher.java` | TagDatafetcher | Blocking data fetchers | Return `Flux<T>` |
| `src/main/java/io/spring/graphql/MeDatafetcher.java` | MeDatafetcher | Blocking data fetchers | Return `Mono<T>` |
| `src/main/java/io/spring/graphql/CommentMutation.java` | CommentMutation | Blocking mutations | Return `Mono<T>` |
| `src/main/java/io/spring/graphql/RelationMutation.java` | RelationMutation | Blocking mutations | Return `Mono<T>` |

---

## 6. Configuration - Transaction Management

### Current State
| File | Class | Issue | Recommended Refactor |
|------|-------|-------|---------------------|
| `src/main/java/io/spring/MyBatisConfig.java` | MyBatisConfig | Blocking transaction manager | Use R2DBC transaction manager |

---

## 7. No Blocking Issues Found

The following patterns were **NOT** found in the codebase (good):
- `RestTemplate` usage
- `HttpURLConnection` usage
- `Thread.sleep()` calls
- `synchronized` blocks
- `.block()` calls
- `.toFuture().get()` calls

---

## Summary Statistics

| Category | Blocking Components | Priority |
|----------|---------------------|----------|
| REST Controllers | 8 | High |
| Repositories | 4 | High |
| MyBatis Mappers | 3+ | High |
| Read Services | 5 | High |
| Application Services | 7 | High |
| Security Components | 3 | High |
| GraphQL Components | 9 | Medium |
| Configuration | 1 | Medium |

**Total Blocking Components to Refactor: ~40+**

---

## Migration Priority Order

1. **Phase 1**: Upgrade Java and Spring Boot versions
2. **Phase 2**: Convert security layer to reactive
3. **Phase 3**: Convert data access layer (MyBatis → R2DBC or bounded elastic)
4. **Phase 4**: Convert application services to reactive
5. **Phase 5**: Convert REST controllers to WebFlux handlers
6. **Phase 6**: Convert GraphQL data fetchers to reactive
7. **Phase 7**: Update tests for reactive patterns

---

## Risk Assessment

| Risk | Impact | Mitigation |
|------|--------|------------|
| SQLite lacks R2DBC driver | High | Switch to H2 database (has R2DBC support) |
| MyBatis not reactive-native | High | Use Spring Data R2DBC or isolate with boundedElastic |
| DGS reactive support complexity | Medium | Follow DGS reactive documentation |
| Test coverage may drop during migration | Medium | Maintain parallel test suites |
| Breaking API changes | High | Maintain backward compatibility at API level |
