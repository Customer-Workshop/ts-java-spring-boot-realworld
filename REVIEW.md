# Java 25 Upgrade — Review Checkpoints

This document defines the review checkpoints for the incremental upgrade from Java 11 + Spring Boot 2.6.3 to Java 25 + Spring Boot 3.5+. Each milestone must pass its review criteria before proceeding.

## Coding Standards Reference

All changes must comply with `.windsurf/rules/java-developer-guide.md`. Key enforcement points:
- PascalCase for classes, camelCase for methods/variables, ALL_CAPS for constants
- Constructor injection over field injection
- Spring Boot best practices and proper annotation usage
- JUnit 5 for all tests
- Spotless formatting (Google Java Format) must pass
- JaCoCo coverage ≥ 80%

---

### Milestone 1: Branch Setup + REVIEW.md
**Scope:** Create branch, add this file  
**Review criteria:**
- [ ] Branch `java-25-target` exists off `main`
- [ ] `REVIEW.md` present at repo root
- [ ] No functional code changes

---

### Milestone 2: Spring Boot 2.6.3 → 2.7.x
**Scope:** `build.gradle` — update Spring Boot plugin and `io.spring.dependency-management` plugin version  
**Files:** `build.gradle`  
**Review criteria:**
- [ ] `org.springframework.boot` plugin version is 2.7.x (latest 2.7)
- [ ] `io.spring.dependency-management` plugin updated to compatible version
- [ ] `./gradlew clean test` passes
- [ ] Note any new deprecation warnings in build output (these guide Milestone 4/5)

---

### Milestone 3: Joda-Time → java.time Migration
**Scope:** Replace all `org.joda.time.DateTime` usage with `java.time.Instant` or `java.time.LocalDateTime`. Remove `joda-time` dependency.  
**Files to modify (all files containing Joda-Time DateTime):**
- `src/main/java/io/spring/core/article/Article.java`
- `src/main/java/io/spring/core/comment/Comment.java`
- `src/main/java/io/spring/application/data/ArticleData.java`
- `src/main/java/io/spring/application/data/CommentData.java`
- `src/main/java/io/spring/application/DateTimeCursor.java`
- `src/main/java/io/spring/application/ArticleQueryService.java`
- `src/main/java/io/spring/application/CommentQueryService.java`
- `src/main/java/io/spring/infrastructure/mybatis/DateTimeHandler.java`
- `src/main/java/io/spring/JacksonCustomizations.java`
- `src/main/java/io/spring/graphql/ArticleDatafetcher.java`
- `src/main/java/io/spring/graphql/CommentDatafetcher.java`
- `src/main/java/io/spring/infrastructure/mybatis/readservice/CommentReadService.java`
- `src/test/java/io/spring/TestHelper.java`
- `src/test/java/io/spring/application/article/ArticleQueryServiceTest.java`
- `src/test/java/io/spring/api/ArticleApiTest.java`
- `src/test/java/io/spring/api/ArticlesApiTest.java`
- `build.gradle` (remove `joda-time:joda-time:2.10.13`)

**Review criteria:**
- [ ] Zero `org.joda.time` imports in codebase (`grep -r "org.joda.time" src/` returns nothing)
- [ ] `joda-time` dependency removed from `build.gradle`
- [ ] `DateTimeHandler` updated to handle `java.time` types for MyBatis
- [ ] `JacksonCustomizations` serializers updated for `java.time`
- [ ] All tests pass (`./gradlew clean test`)
- [ ] JaCoCo coverage ≥ 80%

---

### Milestone 4: javax → jakarta Namespace Migration
**Scope:** Replace all `javax.validation`, `javax.servlet`, `javax.crypto` imports with `jakarta.*` equivalents. This is prep for Spring Boot 3 — the actual switch happens in Milestone 5 but the awareness of affected files is tracked here. NOTE: On Spring Boot 2.7, keep `javax.*` — this milestone documents the scope and may be merged with Milestone 5 if preferred.  
**Files affected (21 files with javax imports):**
- `src/main/java/io/spring/api/security/JwtTokenFilter.java` (javax.servlet)
- `src/main/java/io/spring/application/user/UserService.java` (javax.validation)
- `src/main/java/io/spring/api/UsersApi.java`
- `src/main/java/io/spring/api/CommentsApi.java`
- `src/main/java/io/spring/api/ArticleApi.java`
- `src/main/java/io/spring/api/ArticlesApi.java`
- `src/main/java/io/spring/api/CurrentUserApi.java`
- `src/main/java/io/spring/api/exception/CustomizeExceptionHandler.java`
- `src/main/java/io/spring/application/article/DuplicatedArticleConstraint.java`
- `src/main/java/io/spring/application/article/DuplicatedArticleValidator.java`
- `src/main/java/io/spring/application/article/ArticleCommandService.java`
- `src/main/java/io/spring/application/article/NewArticleParam.java`
- `src/main/java/io/spring/application/user/DuplicatedEmailConstraint.java`
- `src/main/java/io/spring/application/user/DuplicatedEmailValidator.java`
- `src/main/java/io/spring/application/user/DuplicatedUsernameConstraint.java`
- `src/main/java/io/spring/application/user/DuplicatedUsernameValidator.java`
- `src/main/java/io/spring/application/user/RegisterParam.java`
- `src/main/java/io/spring/application/user/UpdateUserParam.java`
- `src/main/java/io/spring/graphql/exception/GraphQLCustomizeExceptionHandler.java`
- `src/main/java/io/spring/graphql/UserMutation.java`
- `src/main/java/io/spring/infrastructure/service/DefaultJwtService.java` (javax.crypto)

**Review criteria:**
- [ ] All `javax.validation` → `jakarta.validation`
- [ ] All `javax.servlet` → `jakarta.servlet`
- [ ] `javax.crypto` stays as-is (it's part of `java.base`, not Jakarta EE)
- [ ] `grep -r "javax\." src/main/java/ | grep -v "javax.crypto"` returns nothing
- [ ] Code compiles (will only compile after Spring Boot 3 dep is in place — combine with Milestone 5)

---

### Milestone 5: Spring Boot 2.7 → 3.x + Java 17
**Scope:** Major framework upgrade. Update Spring Boot plugin to 3.x, set Java 17, update CI.  
**Files:**
- `build.gradle` — `org.springframework.boot` to 3.2+, `sourceCompatibility`/`targetCompatibility` to '17'
- `.java-version` — change from `11` to `17`
- `.github/workflows/gradle.yml` — change `java-version: '11'` to `'17'`
- Security configuration may need updates for Spring Security 6.x changes

**Review criteria:**
- [ ] `sourceCompatibility = '17'` and `targetCompatibility = '17'` in build.gradle
- [ ] `.java-version` reads `17`
- [ ] CI workflow uses `java-version: '17'`
- [ ] Spring Boot 3.x plugin version in `build.gradle`
- [ ] Application boots (`./gradlew bootRun` starts without error)
- [ ] All tests pass
- [ ] No `javax.*` imports remain (except `javax.crypto`)

---

### Milestone 6: Dependency Version Bumps (Spring Boot 3 Compatibility)
**Scope:** Update all dependencies to Spring Boot 3 / Java 17+ compatible versions  
**File:** `build.gradle`  
**Dependencies to update:**

| Dependency | From | To |
|---|---|---|
| `com.netflix.dgs.codegen` plugin | 5.0.6 | 6.x+ (Spring Boot 3 compatible) |
| `com.netflix.graphql.dgs:graphql-dgs-spring-boot-starter` | 4.9.21 | 7.x+ |
| `org.mybatis.spring.boot:mybatis-spring-boot-starter` | 2.2.2 | 3.x |
| `io.jsonwebtoken:jjwt-*` | 0.11.2 | 0.12.x |
| `io.rest-assured:*` | 4.5.1 | 5.x |
| `org.mockito:mockito-inline` | 4.0.0 | Remove (use `mockito-core` 5.x, inline is default) |
| `com.diffplug.spotless` plugin | 6.2.1 | Latest 6.x |
| JaCoCo `toolVersion` | 0.8.7 | 0.8.11+ |
| `org.xerial:sqlite-jdbc` | 3.36.0.3 | Latest |
| Selenium / WebDriverManager / TestNG | current | Latest compatible |

**Review criteria:**
- [ ] All dependencies resolve without conflicts
- [ ] `./gradlew clean test` passes
- [ ] `./gradlew spotlessCheck` passes
- [ ] JaCoCo coverage ≥ 80%
- [ ] No dependency vulnerability warnings

---

### Milestone 7: JJWT API Migration
**Scope:** Update `DefaultJwtService.java` to use jjwt 0.12 builder API (deprecated methods in 0.11)  
**File:** `src/main/java/io/spring/infrastructure/service/DefaultJwtService.java`  
**Changes:**
- `Jwts.builder().setSubject()` → `Jwts.builder().subject()`
- `Jwts.parserBuilder().setSigningKey().build().parseClaimsJws()` → `Jwts.parser().verifyWith().build().parseSignedClaims()`
- `claimsJws.getBody()` → `claimsJws.getPayload()`
- `SignatureAlgorithm` enum → `Jwts.SIG.HS512`

**Review criteria:**
- [ ] No deprecated jjwt API calls
- [ ] JWT token generation and validation still works (existing tests pass)
- [ ] No `SignatureAlgorithm` enum usage

---

### Milestone 8: Java 17 → 25 + Spring Boot 3.5
**Scope:** Runtime upgrade to Java 25 LTS  
**Files:**
- `build.gradle` — `sourceCompatibility`/`targetCompatibility` to '25', Spring Boot to 3.5+
- `.java-version` — `25`
- `.github/workflows/gradle.yml` — `java-version: '25'`

**Review criteria:**
- [ ] `sourceCompatibility = '25'` in build.gradle
- [ ] `.java-version` reads `25`
- [ ] CI uses `java-version: '25'`
- [ ] `./gradlew clean test` passes
- [ ] Application boots successfully
- [ ] No reflection/illegal-access warnings at runtime

---

### Milestone 9: Modernize Java Idioms
**Scope:** Adopt Java 17-25 language features per coding guide  
**Changes across all source files:**
- Convert data classes to `record` types where appropriate (e.g., param classes, data classes)
- Use pattern matching for `instanceof`
- Use text blocks for multi-line strings
- Use `sealed` classes/interfaces where inheritance is constrained
- Replace field injection with constructor injection (per coding guide)
- Use `var` for local variables where type is obvious

**Review criteria:**
- [ ] No field injection (`@Autowired` on fields) — use constructor injection
- [ ] Param/data classes evaluated for `record` conversion
- [ ] Pattern matching used for `instanceof` checks
- [ ] All tests pass
- [ ] Spotless formatting passes
- [ ] JaCoCo coverage ≥ 80%

---

### Milestone 10: Final Cleanup + Coding Standards Audit
**Scope:** Full codebase audit against `.windsurf/rules/java-developer-guide.md`  
**Review criteria:**
- [ ] `./gradlew spotlessCheck` passes
- [ ] `./gradlew clean test` passes with JaCoCo ≥ 80%
- [ ] No deprecated API usage in source code
- [ ] Naming conventions enforced (PascalCase classes, camelCase methods, ALL_CAPS constants)
- [ ] No `javax.*` imports (except `javax.crypto`)
- [ ] No Joda-Time usage
- [ ] No jjwt deprecated API
- [ ] Constructor injection throughout
- [ ] `.windsurf/rules/java-developer-guide.md` updated to reference Java 25 (instead of "Java 17 or later")
- [ ] README.md updated to reflect new Java/Spring Boot versions
