# Contributor Workflow

This project follows a lightweight, Git-based workflow optimized for small teams.

- Branching model
  - main: always green; production-ready
  - feature/<short-description>: new work
  - fix/<short-description>: fixes and small improvements
  - docs/<short-description>: documentation-only changes
- Commit messages
  - Use Conventional Commits where practical: <type>(scope): short summary
  - Types: feat, fix, docs, style, refactor, perf, test, build, ci, chore
  - Keep messages concise; include context in the body if needed
- Pull requests
  - Link issues or task IDs when available
  - Include a short description and testing notes (how you verified the change)
  - Keep PRs focused and under ~300 lines when possible; split large changes
- Code style and quality
  - Backend: follow Spring Boot Guidelines in .junie/guidelines.md
  - Frontend: run npm run lint and npm run typecheck; fix errors
  - Tests: include/adjust tests for significant changes (Vitest for frontend)
- OpenAPI
  - Update openapi/openapi/openapi.yaml first (contract-first)
  - cd openapi && npm ci && npm test (lint)
  - cd src/main/frontend && npm run api:gen
- Build & verify
  - mvn -Pwith-frontend clean package
  - Start app and smoke test UI and key endpoints
- Review checklist (PR author)
  - [ ] Code compiles and tests pass locally
  - [ ] Lint/typecheck clean
  - [ ] OpenAPI updated and client regenerated (if API changed)
  - [ ] No secrets or sensitive info committed
  - [ ] Documentation added/updated (README, requirements.md as needed)
- Review checklist (reviewer)
  - [ ] Meets acceptance criteria and guidelines
  - [ ] Reasonable test coverage
  - [ ] Clear, minimal, reversible change
  - [ ] Naming and structure consistent with repository
