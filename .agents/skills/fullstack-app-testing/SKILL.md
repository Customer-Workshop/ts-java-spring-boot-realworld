---
name: fullstack-app-testing
description: How to run and E2E-test the RealWorld app (Spring Boot backend + Next.js frontend) in this repo, including Java version pitfalls and comment/article UI flows.
---

# Full-stack testing of ts-java-spring-boot-realworld

## Running the app
- Backend MUST run on Java 11. If the shell's default `java` is 17/21, `./gradlew bootRun` fails during `:compileJava` with a Lombok error (`NoSuchFieldError: JCTree$JCImport ... qualid`). Fix: `JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64 ./gradlew bootRun`. The blueprint sets Java 11 via update-alternatives/$ENVRC, but fresh shells may still default to a newer JDK — always export JAVA_HOME explicitly.
- Backend listens on :8080 and recreates SQLite `dev.db` with seed data (users janedoe/bobsmith/johndoe, several articles) on every bootRun. Health check: `curl http://localhost:8080/tags`.
- Frontend: `cd frontend && NODE_OPTIONS=--openssl-legacy-provider npm run dev` → :3000. The openssl-legacy-provider flag is required on Node 18+.

## UI testing tips
- The comment box on `/article/<slug>` only renders when logged in. Since the DB is wiped each bootRun, register a fresh user at `/user/register` (any username/email/password) — registration auto-logs you in.
- Seeded articles exist (e.g. `/article/testing-spring-boot-applications`), so you usually don't need to create one.
- To set very long textarea values (thousands of chars) in the React frontend, don't type: use the native value setter + input event so React state updates:
  `Object.getOwnPropertyDescriptor(HTMLTextAreaElement.prototype,'value').set.call(ta, text); ta.dispatchEvent(new Event('input',{bubbles:true}));`
  Then do boundary transitions (±1 char) with real keystrokes (ctrl+End, type/Backspace) so they're visible on the recording.
- Comments POST via XHR (axios); to prove a disabled submit sends nothing, hook `XMLHttpRequest.prototype.open` and count POSTs to `/comments`.
