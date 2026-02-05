# Dependency Upgrade Matrix

## Executive Summary

This document outlines the dependency upgrade strategy for migrating the spring-boot-realworld-example-app from Java 11 to Java 21 with a reactive architecture.

**Current State:**
- Java Version: 11
- Spring Boot: 2.6.3
- Architecture: Blocking (Servlet-based)

**Target State:**
- Java Version: 21
- Spring Boot: 3.2.x
- Architecture: Reactive (WebFlux)

---

## 1. Core Framework Upgrades

| Artifact | Current Version | Target Version | Java 21 Ready | Risk Level | Notes |
|----------|-----------------|----------------|---------------|------------|-------|
| `org.springframework.boot:spring-boot-starter-web` | 2.6.3 | Remove | N/A | High | Replace with WebFlux |
| `org.springframework.boot:spring-boot-starter-webflux` | N/A | 3.2.2 | Yes | High | New dependency for reactive |
| `org.springframework.boot:spring-boot-starter-validation` | 2.6.3 | 3.2.2 | Yes | Low | Direct upgrade |
| `org.springframework.boot:spring-boot-starter-hateoas` | 2.6.3 | 3.2.2 | Yes | Medium | Check reactive support |
| `org.springframework.boot:spring-boot-starter-security` | 2.6.3 | 3.2.2 | Yes | High | Reactive security config needed |

---

## 2. Data Access Layer Upgrades

| Artifact | Current Version | Target Version | Java 21 Ready | Risk Level | Notes |
|----------|-----------------|----------------|---------------|------------|-------|
| `org.mybatis.spring.boot:mybatis-spring-boot-starter` | 2.2.2 | Remove | N/A | High | Replace with R2DBC |
| `org.springframework.boot:spring-boot-starter-data-r2dbc` | N/A | 3.2.2 | Yes | High | New reactive data access |
| `org.xerial:sqlite-jdbc` | 3.36.0.3 | Remove | N/A | High | SQLite lacks R2DBC support |
| `io.r2dbc:r2dbc-h2` | N/A | 1.0.0.RELEASE | Yes | Medium | H2 for reactive DB |
| `com.h2database:h2` | N/A | 2.2.224 | Yes | Low | H2 database driver |
| `org.flywaydb:flyway-core` | (managed) | 9.22.3 | Yes | Medium | Schema migration |

---

## 3. Security & JWT Upgrades

| Artifact | Current Version | Target Version | Java 21 Ready | Risk Level | Notes |
|----------|-----------------|----------------|---------------|------------|-------|
| `io.jsonwebtoken:jjwt-api` | 0.11.2 | 0.12.5 | Yes | Low | JWT library |
| `io.jsonwebtoken:jjwt-impl` | 0.11.2 | 0.12.5 | Yes | Low | JWT implementation |
| `io.jsonwebtoken:jjwt-jackson` | 0.11.2 | 0.12.5 | Yes | Low | JWT Jackson support |

---

## 4. GraphQL Upgrades

| Artifact | Current Version | Target Version | Java 21 Ready | Risk Level | Notes |
|----------|-----------------|----------------|---------------|------------|-------|
| `com.netflix.graphql.dgs:graphql-dgs-spring-boot-starter` | 4.9.21 | 8.2.0 | Yes | High | Major version upgrade |
| DGS Codegen Plugin | 5.0.6 | 6.1.0 | Yes | Medium | Code generation |

---

## 5. Utility Libraries

| Artifact | Current Version | Target Version | Java 21 Ready | Risk Level | Notes |
|----------|-----------------|----------------|---------------|------------|-------|
| `joda-time:joda-time` | 2.10.13 | Remove | N/A | Low | Use java.time instead |
| `org.projectlombok:lombok` | (managed) | 1.18.30 | Yes | Low | Annotation processor |

---

## 6. Testing Dependencies

| Artifact | Current Version | Target Version | Java 21 Ready | Risk Level | Notes |
|----------|-----------------|----------------|---------------|------------|-------|
| `org.springframework.boot:spring-boot-starter-test` | 2.6.3 | 3.2.2 | Yes | Low | Test framework |
| `org.springframework.security:spring-security-test` | (managed) | 6.2.1 | Yes | Low | Security testing |
| `io.rest-assured:rest-assured` | 4.5.1 | 5.4.0 | Yes | Medium | API testing |
| `io.rest-assured:spring-mock-mvc` | 4.5.1 | Remove | N/A | Medium | Replace with WebTestClient |
| `io.rest-assured:spring-web-test-client` | N/A | 5.4.0 | Yes | Medium | Reactive testing |
| `io.projectreactor:reactor-test` | N/A | 3.6.2 | Yes | Low | Reactor testing |
| `org.mockito:mockito-inline` | 4.0.0 | 5.2.0 | Yes | Low | Mocking framework |
| `org.mybatis.spring.boot:mybatis-spring-boot-starter-test` | 2.2.2 | Remove | N/A | Low | Remove with MyBatis |
| `org.seleniumhq.selenium:selenium-java` | 4.15.0 | 4.17.0 | Yes | Low | E2E testing |
| `io.github.bonigarcia:webdrivermanager` | 5.6.2 | 5.6.3 | Yes | Low | WebDriver management |
| `org.testng:testng` | 7.8.0 | 7.9.0 | Yes | Low | TestNG framework |
| `com.aventstack:extentreports` | 5.1.1 | 5.1.1 | Yes | Low | Test reporting |

---

## 7. Build Tool Upgrades

| Component | Current Version | Target Version | Notes |
|-----------|-----------------|----------------|-------|
| Gradle Wrapper | (check) | 8.5 | Required for Java 21 |
| `io.spring.dependency-management` | 1.0.11.RELEASE | 1.1.4 | Spring Boot 3 compatible |
| `com.diffplug.spotless` | 6.2.1 | 6.25.0 | Code formatting |
| `jacoco` | 0.8.7 | 0.8.11 | Coverage reporting |
| `com.netflix.dgs.codegen` | 5.0.6 | 6.1.0 | GraphQL codegen |

---

## 8. CI/CD Updates

| Component | Current | Target | Notes |
|-----------|---------|--------|-------|
| GitHub Actions Java | 11 (Zulu) | 21 (Temurin) | CI runtime |
| actions/checkout | v4 | v4 | No change |
| actions/setup-java | v4 | v4 | Update java-version |
| actions/cache | v4 | v4 | No change |

---

## 9. New Dependencies Required

### For Reactive Architecture
```groovy
// Reactive Web
implementation 'org.springframework.boot:spring-boot-starter-webflux'

// Reactive Data Access
implementation 'org.springframework.boot:spring-boot-starter-data-r2dbc'
runtimeOnly 'io.r2dbc:r2dbc-h2'
runtimeOnly 'com.h2database:h2'

// Reactive Testing
testImplementation 'io.projectreactor:reactor-test'
testImplementation 'io.rest-assured:spring-web-test-client'
```

### For Quality Gates
```groovy
// SonarQube
plugins {
    id 'org.sonarqube' version '4.4.1.3373'
}

// OpenAPI for 42Crunch
implementation 'org.springdoc:springdoc-openapi-starter-webflux-ui:2.3.0'
```

---

## 10. Dependencies to Remove

| Artifact | Reason |
|----------|--------|
| `org.springframework.boot:spring-boot-starter-web` | Replaced by WebFlux |
| `org.mybatis.spring.boot:mybatis-spring-boot-starter` | Replaced by R2DBC |
| `org.mybatis.spring.boot:mybatis-spring-boot-starter-test` | Replaced by R2DBC test |
| `org.xerial:sqlite-jdbc` | SQLite not reactive-compatible |
| `joda-time:joda-time` | Use java.time (Java 8+) |
| `io.rest-assured:spring-mock-mvc` | Use WebTestClient |

---

## 11. Migration Order

### Phase 1: Build System (Day 1-2)
1. Update Gradle wrapper to 8.5
2. Update Java version to 21 in build.gradle
3. Update CI workflow to Java 21
4. Update plugin versions

### Phase 2: Core Dependencies (Day 3-5)
1. Update Spring Boot to 3.2.2
2. Update dependency management plugin
3. Replace javax.* with jakarta.* imports
4. Update validation annotations

### Phase 3: Data Layer (Week 2)
1. Add R2DBC dependencies
2. Remove MyBatis dependencies
3. Migrate repositories to R2DBC
4. Update Flyway migrations for H2

### Phase 4: Web Layer (Week 2-3)
1. Replace spring-boot-starter-web with webflux
2. Convert controllers to reactive handlers
3. Update security configuration

### Phase 5: Testing (Week 3-4)
1. Update test dependencies
2. Add reactor-test
3. Convert tests to use WebTestClient
4. Add StepVerifier tests

### Phase 6: Quality Tools (Week 4)
1. Add SonarQube plugin
2. Add OpenAPI/Springdoc for 42Crunch
3. Configure quality gates

---

## 12. Risk Mitigation

| Risk | Mitigation Strategy |
|------|---------------------|
| Breaking changes in Spring Boot 3 | Follow migration guide, test thoroughly |
| javax to jakarta namespace | Use IDE refactoring tools |
| MyBatis to R2DBC complexity | Incremental migration, maintain test coverage |
| DGS major version changes | Review DGS migration guide |
| Test failures during migration | Maintain parallel test suites |

---

## 13. Compatibility Matrix

| Component | Spring Boot 2.6.x | Spring Boot 3.2.x |
|-----------|-------------------|-------------------|
| Java | 8, 11, 17 | 17, 21 |
| Jakarta EE | javax.* | jakarta.* |
| Hibernate | 5.x | 6.x |
| Spring Security | 5.x | 6.x |
| Spring Framework | 5.x | 6.x |

---

## 14. Verification Checklist

- [ ] All dependencies resolve without conflicts
- [ ] Application compiles with Java 21
- [ ] All unit tests pass
- [ ] All integration tests pass
- [ ] API endpoints respond correctly
- [ ] GraphQL queries work
- [ ] Authentication/authorization works
- [ ] Database operations work
- [ ] Code coverage >= 95%
- [ ] Sonar quality gate passes
- [ ] 42Crunch security gate passes
