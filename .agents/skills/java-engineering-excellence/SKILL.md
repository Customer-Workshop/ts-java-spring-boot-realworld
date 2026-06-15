---
name: java-engineering-excellence
description: >
  Repo-specific mechanics for the Java Engineering Excellence playbook on
  ts-java-spring-boot-realworld: build commands, verification gates, package
  layout, dependency coordinates, and branching conventions.
---

# Java Engineering Excellence — Repo Mechanics

## Build & Verification Commands

```bash
# Compile (no tests) — confirm build is healthy
./gradlew clean build -x test

# Full verification gate (the bar for every PR)
./gradlew clean test spotlessCheck

# Auto-format before committing
./gradlew spotlessApply

# Coverage report + enforcement (80% line coverage minimum)
./gradlew jacocoTestReport jacocoTestCoverageVerification

# Single test class (fast feedback)
./gradlew test --tests "io.spring.api.ArticlesApiTest"

# Dependency insight (for CVE triage)
./gradlew dependencyInsight --dependency <group:artifact>

# Selenium E2E tests (separate task, TestNG — excluded from main gate)
./gradlew seleniumTest
```

## Project Layout

```
ts-java-spring-boot-realworld/
├── build.gradle              # Gradle build (Spring Boot 2.6.3, Java 11)
├── gradlew / gradlew.bat     # Gradle wrapper
├── src/main/java/io/spring/
│   ├── api/                  # REST controllers (@RestController)
│   │   ├── security/         # WebSecurityConfig, JwtTokenFilter
│   │   └── exception/        # @ControllerAdvice, custom exceptions
│   ├── application/          # Query services, DTOs, command services
│   │   ├── article/          # ArticleCommandService
│   │   ├── data/             # ArticleData, ProfileData, CommentData
│   │   └── user/             # UserQueryService
│   ├── core/                 # Domain entities and service interfaces
│   │   ├── article/          # Article, Tag, ArticleRepository
│   │   ├── comment/          # Comment, CommentRepository
│   │   ├── favorite/         # ArticleFavorite, ArticleFavoriteRepository
│   │   ├── service/          # JwtService interface
│   │   └── user/             # User, UserRepository, FollowRelation
│   ├── graphql/              # DGS GraphQL datafetchers + mutations
│   │   └── exception/        # GraphQL error handling
│   └── infrastructure/       # Implementation layer
│       ├── mybatis/mapper/   # MyBatis mapper interfaces
│       ├── mybatis/readservice/ # Read-model query services
│       ├── repository/       # MyBatis-backed repository impls
│       └── service/          # DefaultJwtService (jjwt)
├── src/main/resources/
│   ├── db/migration/         # Flyway SQL migrations (V1__, V2__, ...)
│   ├── mapper/               # MyBatis XML mapper files
│   ├── schema/               # GraphQL schema (.graphqls)
│   └── application.properties
├── src/test/java/io/spring/
│   ├── api/                  # REST API tests (JUnit 5 + REST-Assured MockMvc)
│   ├── infrastructure/       # Repository + service integration tests
│   └── selenium/             # Selenium E2E tests (TestNG, excluded from gate)
└── frontend/                 # Next.js frontend (separate, optional)
```

## Key Dependencies (current baseline — Spring Boot 2.6.3 / Java 11)

| Artifact | Version | Upgrade Target (Boot 3.5.x) |
|----------|---------|----------------------------|
| `org.springframework.boot` (plugin) | 2.6.3 | 3.5.x |
| `io.spring.dependency-management` | 1.0.11 | 1.1.x |
| Java `sourceCompatibility` | 11 | 21 |
| `com.netflix.dgs.codegen` | 5.0.6 | 7.x+ |
| `com.netflix.graphql.dgs:graphql-dgs-spring-boot-starter` | 4.9.21 | 8.x+ (Boot 3 compatible) |
| `org.mybatis.spring.boot:mybatis-spring-boot-starter` | 2.2.2 | 3.0.x |
| `io.jsonwebtoken:jjwt-api` | 0.11.2 | 0.12.x |
| `joda-time:joda-time` | 2.10.13 | remove → `java.time` |
| `org.xerial:sqlite-jdbc` | 3.36.0.3 | 3.45.x+ |
| `io.rest-assured:spring-mock-mvc` (test) | 4.5.1 | 5.4.x |
| `com.diffplug.spotless` | 6.2.1 | 6.25.x |
| `org.flywaydb:flyway-core` | (managed) | 10.x (Boot 3.5 managed) |
| Selenium (test) | 4.15.0 | 4.20.x |

## Modernization — Namespace Migration Checklist

When upgrading to Spring Boot 3.x / Jakarta EE:

1. `javax.validation.*` → `jakarta.validation.*`
2. `javax.servlet.*` → `jakarta.servlet.*`
3. `javax.persistence.*` → `jakarta.persistence.*` (if JPA added)
4. `javax.annotation.*` → `jakarta.annotation.*`
5. Remove explicit `javax.validation:validation-api` from `build.gradle`
6. `WebSecurityConfigurerAdapter` → `@Bean SecurityFilterChain`
7. `.antMatchers(...)` → `.requestMatchers(...)`
8. `authorizeRequests()` → `authorizeHttpRequests()`
9. Update `@MockBean` → Spring Boot 3.4+ deprecation path (if applicable)

## Branching Convention

```bash
# Feature branch pattern (never merge to main for demos)
git checkout -b devin/$(date +%s)-<task-type>-<short-desc>

# Examples:
git checkout -b devin/1718451200-feature-dev-bookmarks
git checkout -b devin/1718451200-cve-remediation-jackson
git checkout -b devin/1718451200-app-modernization-boot3
git checkout -b devin/1718451200-test-generation-coverage
```

## Verification Gate — What "Green" Means

```
BUILD SUCCESSFUL in 42s
87 tests completed, 0 failed
Spotless: 120 files checked — no violations
JaCoCo: line coverage 82.3% (minimum 80.0%) — PASS
```

If any of these fail, the PR is not ready.

## Parallel Fan-Out — Layer Namespaces

For the app-modernization task, work can be parallelized by layer. Each child
session takes one layer, creates its own branch, and opens its own PR:

| Child | Layer | Packages | Branch suffix |
|-------|-------|----------|---------------|
| 1 | API + Security | `io.spring.api.*` | `-upgrade-api` |
| 2 | Infrastructure | `io.spring.infrastructure.*` | `-upgrade-infra` |
| 3 | GraphQL | `io.spring.graphql.*` | `-upgrade-graphql` |
| 4 | Core + Application | `io.spring.core.*`, `io.spring.application.*` | `-upgrade-core` |

The orchestrator merges all four branches, runs the full gate, and opens the
final consolidated PR.

## Security Scanning (for issue-triage task)

```bash
# OWASP dependency-check (if plugin configured)
./gradlew dependencyCheckAnalyze

# Or use Gradle's built-in dependency insight
./gradlew dependencies --configuration runtimeClasspath | grep -i "jackson\|log4j\|spring-"

# Known critical CVEs in baseline:
# - Spring Framework 5.3.15 (CVE-2022-22965 — Spring4Shell)
# - jackson-databind 2.13.x (multiple RCE vectors)
# - joda-time (EOL, no security patches)
# - sqlite-jdbc 3.36.x (buffer overflow CVEs)
```

## Test Patterns (follow existing conventions)

**Controller tests** — `@WebMvcTest` + `@Import(WebSecurityConfig.class)` +
REST-Assured `MockMvc`:
```java
@WebMvcTest({ArticlesApi.class})
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class ArticlesApiTest extends TestWithCurrentUser { ... }
```

**Repository tests** — extend `DbTestBase` (H2 in-memory):
```java
public class MyBatisArticleRepositoryTest extends DbTestBase { ... }
```

**Service tests** — plain JUnit 5 + Mockito:
```java
@ExtendWith(MockitoExtension.class)
public class ArticleQueryServiceTest { ... }
```

## Revert / Reset (for repeatable demos)

The before-state is always `main`. To reset after a demo run:

```bash
git checkout main
git branch -D devin/*   # remove all demo branches locally
```

Remote branches (PRs) can be closed without merging. The database is recreated
on each `bootRun` (Flyway + clean task), so no data cleanup is needed.
