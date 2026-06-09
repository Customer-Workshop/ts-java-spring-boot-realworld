# Agent Guidelines — Java 25 Upgrade

This file provides context and rules for AI agents (Devin sessions) working on the `java-25-target` branch to incrementally upgrade this application from Java 11 + Spring Boot 2.6.3 to Java 25 + Spring Boot 3.5+.

## Branch Strategy

- **`main`** — stable, Java 11 + Spring Boot 2.6.3 baseline
- **`java-25-target`** — long-lived upgrade branch; all milestone PRs target this branch
- Each milestone is a separate PR into `java-25-target`
- Do NOT merge `java-25-target` back to `main` until all milestones are complete and reviewed

## Milestone Execution Rules

1. **Read `REVIEW.md` first.** It defines every milestone's scope, files, and review criteria. Do not deviate from its scope.
2. **One milestone per PR** (exception: milestones 4+5 are combined in a single commit/PR since the javax→jakarta change won't compile without Spring Boot 3).
3. **Never skip ahead.** Milestones must be completed in order (2 → 3 → 4+5 → 6 → 7 → 8 → 9 → 10). Each milestone assumes the previous one is already merged.
4. **Branch naming:** `devin/<timestamp>-milestone-<N>-<short-description>` (e.g., `devin/1718000000-milestone-2-spring-boot-27`).
5. **Base branch for PRs:** Always `java-25-target`, never `main`.

## Verification Commands

Run these after every milestone to validate the change:

```bash
# Set Java home (adjust path if the environment blueprint provides a different JDK)
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64  # milestones 2-3
# export JAVA_HOME=<java-17-path>                     # milestones 4-7
# export JAVA_HOME=<java-25-path>                     # milestones 8-10

# Build and test
./gradlew clean test

# Spotless formatting check
./gradlew spotlessCheck

# Apply formatting if needed
./gradlew spotlessJavaApply

# JaCoCo coverage (must be ≥ 80%)
./gradlew jacocoTestReport jacocoTestCoverageVerification
```

## Coding Standards

All code must comply with `.windsurf/rules/java-developer-guide.md`. Key rules:
- **PascalCase** for classes, **camelCase** for methods/variables, **ALL_CAPS** for constants
- **Constructor injection** — never use `@Autowired` on fields
- **JUnit 5** for all tests (the project uses `useJUnitPlatform()`)
- **Google Java Format** via Spotless — always run `spotlessCheck` before committing
- Refer to `.windsurf/rules/frontend-style-guide.md` for any frontend changes (Next.js/React in `frontend/`)

## Project Architecture

```
src/main/java/io/spring/
├── api/             # REST controllers, security filters, exception handlers
├── application/     # CQRS query services, DTOs, command services, validators
├── core/            # Domain entities (Article, Comment, User, Tag, FollowRelation)
├── graphql/         # Netflix DGS GraphQL datafetchers and mutations
├── infrastructure/  # MyBatis mappers, JWT service, repository implementations
└── JacksonCustomizations.java  # JSON serialization config
```

Key technology choices:
- **Persistence:** MyBatis (not JPA) with XML mappers in `src/main/resources/mapper/`
- **Database:** SQLite (`dev.db` is ephemeral — deleted on `clean`/`bootRun`)
- **Migrations:** Flyway (`src/main/resources/db/migration/`)
- **GraphQL:** Netflix DGS with SDL in `src/main/resources/schema/`
- **Auth:** JWT via JJWT library, custom `Token` header prefix (not `Bearer`)
- **Testing:** JUnit 5 + MockMvc + RestAssured; Selenium E2E tests are separate (`seleniumTest` task)

## Dependency Notes

- `joda-time` → remove in Milestone 3, replace with `java.time`
- `javax.*` → `jakarta.*` in Milestones 4+5 (except `javax.crypto` which stays — it's in `java.base`)
- `mockito-inline` → remove in Milestone 6 (inline mocking is default in mockito-core 5.x)
- JJWT 0.11 → 0.12 API migration in Milestone 7 (builder methods renamed)

## Common Pitfalls

1. **SQLite + Flyway:** The `dev.db` file is deleted on `clean` and `bootRun`. Don't rely on persisted database state across test runs.
2. **DGS codegen:** The `generateJava` task produces code in `build/generated/`. Don't edit generated files — modify the SDL schema in `src/main/resources/schema/` instead.
3. **JaCoCo threshold:** The build enforces 80% line coverage. If you remove code, make sure test coverage doesn't drop below the threshold.
4. **Selenium tests:** These are excluded from the standard `test` task. Only run them via `./gradlew seleniumTest` and only when a running app instance is available.
5. **Spring Security 6.x (Milestone 5):** The `WebSecurityConfigurerAdapter` is removed in Spring Security 6. You'll need to migrate to component-based security configuration with `SecurityFilterChain` beans.

## Skills

See `.devin/skills/` for reusable skill definitions:
- `java25-upgrade-milestone.md` — step-by-step procedure for executing any single milestone
