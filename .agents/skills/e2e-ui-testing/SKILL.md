---
name: e2e-ui-testing
description: How to run and E2E-test the RealWorld app (Spring Boot backend + Next.js frontend) through the browser UI, including non-ASCII text input.
---

# E2E UI Testing — ts-java-spring-boot-realworld

## Running the app
- Backend: `JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64 ./gradlew bootRun` (recreates SQLite dev.db with seed data). Ready when `curl http://localhost:8080/tags` returns JSON (startup can take ~1–2 min).
- Frontend: `(cd frontend && NODE_OPTIONS=--openssl-legacy-provider npm run dev)` on http://localhost:3000 (talks to :8080 via `frontend/lib/utils/constant.ts`).

## Auth
- No seed-user password known; just register a fresh user at http://localhost:3000/user/register (username/email/password) — it logs you in immediately.

## Frontend quirks to know
- Publishing/updating an article redirects to `/` (home), NOT the article page. To see the article's slug URL, click the article in Global Feed and read the address bar (`/article/<slug>`).
- The home feed is SWR-cached: a just-created/updated article may show stale data until you press F5.
- Seed articles have future-dated timestamps, so new articles may appear BELOW seed articles in Global Feed — scroll down to find them.
- Edit page is `/editor/<slug>` (reached via "Edit Article" button on the article page).

## Typing non-ASCII (CJK, etc.) text in the GUI
- The computer-use `type` action silently drops CJK characters (xdotool limitation) — the field ends up with only the ASCII parts. Always verify what actually got typed.
- Workaround: put text on the clipboard and paste:
  ```bash
  printf '中文：标题' | DISPLAY=:0 xclip -selection clipboard   # install xclip via apt if missing
  ```
  then in the browser: click field, ctrl+a, ctrl+v. Accented Latin (é, ï, ñ) types fine directly.

## API cross-checks
- `curl http://localhost:8080/articles/<slug>` returns the article JSON (slug, title, id) without auth — handy to confirm stored slug/title exactly.
