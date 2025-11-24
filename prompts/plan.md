# Project Improvement Plan — juniemvc

Date: 2025-11-09
Owner: Team Juniemvc
Target: Deliver a production-ready Spring Boot + React application, aligned with the provided engineering guidelines and the current repository setup.

---

## Milestones (High-Level Roadmap)

1. M1: Foundation & Tooling (1–2 weeks)
2. M2: API & Data Layer (1 week)
3. M3: Application Shell & Navigation (1 week)
4. M4: Feature Epics (CRUD for core resources) (3–5 weeks)
5. M5: Quality: Testing, CI, and Observability (1–2 weeks)
6. M6: Security, Config, and Operations (1–2 weeks)
7. M7: Documentation & Developer Experience (ongoing)

Each milestone has exit criteria and measurable deliverables.

---

## Cross-Cutting Standards (Apply in all work)

- Spring Boot Guidelines (constructor injection, package-private components, typed properties, transaction boundaries, OSIV disabled, DTOs, REST design, centralized exception handling, actuator hardening, i18n, Testcontainers, random ports for ITs, logging hygiene, Flyway migrations).
- OpenAPI authoring and validation (Redocly CLI, modular refs; keep spec current with implementation).
- Frontend principles: TypeScript strict mode, ESLint + Prettier, Vitest + RTL, generated API clients, consistent error handling, accessible UI.

---

## Epic 1 — Project Foundation & Setup (M1)

Goal: Ensure repo layout, build, and dev workflows are consistent and repeatable.

Tasks:
- E1.1 Verify frontend location and Vite baseline
  - Confirm `src/main/frontend` contains a Vite React TS app, strict TS config, and `vite.config.ts` outputs to `../resources/static` for prod.
  - Add Tailwind v4 entry `src/main/frontend/src/styles/globals.css` and import in `main.tsx` if missing.
- E1.2 NPM scripts and Node versions
  - In `src/main/frontend/package.json`, standardize scripts: `dev`, `build`, `preview`, `test`, `lint`, `format`, `api:gen`.
  - Align Node/NPM versions with `frontend-maven-plugin` in `pom.xml` (currently Node v22.11.0, npm 10.9.0). Document rationale.
- E1.3 Maven integration
  - Keep `with-frontend` Maven profile wiring `ci` and `build` steps using `frontend-maven-plugin`.
  - Verify `maven-clean-plugin` deletes `src/main/resources/static/**`.
- E1.4 Backend dev profile & CORS
  - Configure dev CORS (if needed) for API routes; rely on Vite proxy during dev.
- E1.5 Git hygiene
  - Ensure `.gitignore` excludes `node_modules`, `src/main/resources/static`, and local tool caches.

Exit criteria:
- `mvn -Pwith-frontend clean package` produces a JAR that serves the Vite build.
- `npm run dev` runs the frontend with API proxy to Spring Boot.

---

## Epic 2 — OpenAPI & API Client Strategy (M2)

Goal: Single source of truth for the API with generated TS client.

Tasks:
- E2.1 Redocly toolchain
  - From `openapi/` run `npm ci` once and `npm test` (Redocly lint) to validate `openapi/openapi/openapi.yaml` and referenced files.
  - Add simple `README` in `openapi/` explaining `npm start`, `npm run build`, `npm test` usage.
- E2.2 API client generation
  - Choose a generator (e.g., `openapi-typescript` + custom axios wrapper or `openapi-generator-cli typescript-axios`).
  - Implement script `api:gen` that outputs to `src/main/frontend/src/lib/api` with immutable types and clients.
  - Gate commit by regenerating on spec change.
- E2.3 Contract-first discipline
  - Workflow: Update spec → lint → generate clients → implement backend & UI.

Exit criteria:
- `npm run api:gen` produces typed clients and models, imported by services.
- Redocly `npm test` lints clean.

---

## Epic 3 — Application Shell & Navigation (M3)

Goal: Create a robust, scalable app shell with routing and shared layout.

Tasks:
- E3.1 Routing
  - Configure React Router with nested routes, error boundaries, and lazy routes.
- E3.2 Layout
  - Implement `AppLayout` with header, nav, content area, and footer; ensure responsiveness.
- E3.3 State & theming
  - Add basic theme tokens via Tailwind; optional dark mode toggle.
- E3.4 Error & loading UX
  - Global error boundary and toast/notification pattern.

Exit criteria:
- Navigable shell with placeholder pages for Beers, Customers, Beer Orders.

---

## Epic 4 — Data Layer & Services (M2–M4)

Goal: Centralized API access, error handling, caching primitives.

Tasks:
- E4.1 Axios instance
  - Implement a shared axios instance with base URL, interceptors for auth (if any), error normalization, and timeouts.
- E4.2 Services per resource
  - `services/beers.ts`, `services/customers.ts`, `services/orders.ts` using generated clients; expose CRUD functions.
- E4.3 Hooks
  - `hooks/useBeers`, `useCustomers`, `useOrders` encapsulate fetching state; consider React Query if desired.

Exit criteria:
- Feature pages consume hooks/services only; no direct axios usage in components.

---

## Epic 5 — Feature Delivery (CRUD) (M4)

Goal: Implement core features incrementally. Use DTOs and adhere to REST design.

Resources to align with: Spring controllers, service layer, MapStruct mappers, and Flyway schema.

Tasks per feature (repeatable pattern):
- E5.X.1 Backend API check
  - Validate endpoints exist in OpenAPI; adjust spec if needed; regenerate clients.
- E5.X.2 DTOs & Mappers
  - Ensure no entity leakage to controllers. Use records/DTOs + MapStruct.
- E5.X.3 Transactions
  - Annotate service methods: `@Transactional` or `@Transactional(readOnly = true)`.
- E5.X.4 Controller contracts
  - Return `ResponseEntity<T>`; proper status codes (201, 200, 204, 404, 400); pagination for collections where applicable.
- E5.X.5 Frontend pages & components
  - List view (with pagination/sort if supported), detail view, create/edit forms, delete flows, optimistic UX.
- E5.X.6 Validation & errors
  - Jakarta validation on requests; propagate `ProblemDetails` from global exception handler; show user-friendly errors.

Initial feature order:
1) Beers, 2) Customers, 3) Beer Orders

Exit criteria:
- Full CRUD for the three resources with E2E happy paths tested.

---

## Epic 6 — Persistence & Migrations (M1–M4)

Goal: Reliable DB schema evolution with Flyway.

Tasks:
- E6.1 Verify Flyway setup
  - Place SQL under `src/main/resources/db/migration/` using `V…__…` naming; H2-compatible syntax for local.
- E6.2 Schema ownership
  - When adding FKs: add column first, then `ALTER TABLE` for constraint (per guideline).
- E6.3 Seed/reference data
  - Optional: `R__` repeatables for views or reference data.

Exit criteria:
- App boots clean with Flyway applying all migrations from scratch.

---

## Epic 7 — Exception Handling & OSIV (M1–M3)

Goal: Predictable error semantics and performance-safe JPA usage.

Tasks:
- E7.1 Global exception handler
  - Implement `@RestControllerAdvice` returning RFC 9457 `ProblemDetails` for common exceptions and validation errors.
- E7.2 Disable OSIV
  - Ensure `spring.jpa.open-in-view=false` in application properties; remove reliance on lazy loads in views; use fetch joins or projections.

Exit criteria:
- Consistent error JSON; no lazy-loading at serialization time.

---

## Epic 8 — Configuration & Security (M6)

Goal: Hardened runtime configuration with typed properties and actuator rules.

Tasks:
- E8.1 Typed properties
  - Introduce `@ConfigurationProperties` classes for app-specific settings; add validation annotations.
- E8.2 Profiles and env vars
  - Prefer env vars for environment differences; document mapping.
- E8.3 Actuator exposure
  - Expose `/actuator/health`, `/info`, `/metrics` anonymously; secure the rest; document non-prod relaxations.

Exit criteria:
- App fails fast on invalid config; actuator endpoints follow policy.

---

## Epic 9 — Internationalization (M6–M7)

Goal: Externalize user-facing messages and support future locales.

Tasks:
- E9.1 Backend messages
  - Externalize validation and error messages into `messages.properties` (and future `messages_{locale}.properties`).
- E9.2 Frontend i18n readiness
  - Introduce a light i18n layer (e.g., `i18next`) for strings in UI; keep keys in resource files.

Exit criteria:
- No hard-coded end-user copy in controllers or core UI components.

---

## Epic 10 — Testing Strategy (M5)

Goal: Confidence via unit, integration, and e2e-level checks.

Tasks:
- E10.1 Backend unit & slice tests
  - Services with constructor injection; use Mockito if needed; repositories with `@DataJpaTest`.
- E10.2 Integration tests with Testcontainers
  - Use Postgres container; start app on random port with `@SpringBootTest(webEnvironment = RANDOM_PORT)`.
- E10.3 Frontend tests
  - Vitest + RTL: component and hook tests; mock network with MSW.
- E10.4 Contract checks
  - Validate OpenAPI responses format (ProblemDetails on error paths) with tests.

Exit criteria:
- CI runs tests reliably in parallel agents; random port prevents conflicts.

---

## Epic 11 — Logging & Observability (M5–M6)

Goal: Actionable logs and metrics with privacy and performance in mind.

Tasks:
- E11.1 SLF4J usage only
  - Remove `System.out.println`; guard expensive debug logs; no sensitive data in logs.
- E11.2 MDC & correlation (optional)
  - Add request correlation IDs to logs; surface in frontend error reports if feasible.
- E11.3 Metrics
  - Ensure key metrics via Micrometer; expose through `/actuator/metrics`.

Exit criteria:
- Clean structured logging; basic metrics visible.

---

## Epic 12 — CI/CD & Release (M5–M6)

Goal: Reproducible builds and deploy artifacts.

Tasks:
- E12.1 CI pipeline
  - Steps: checkout → JDK 21 → cache Maven & npm → `openapi/npm ci && npm test` → `mvn -Pwith-frontend -B clean verify` → publish artifacts.
- E12.2 Static analysis
  - Add code quality gates (SpotBugs, Checkstyle or Spring Java format, ESLint). Fail build on critical issues.
- E12.3 Release artifacts
  - Attach the fat JAR and optionally the bundled OpenAPI `dist/bundle.yaml`.

Exit criteria:
- CI green with caching; artifacts uploaded; linting enforced.

---

## Epic 13 — Developer Documentation (M7)

Goal: Keep the team fast and aligned.

Tasks:
- E13.1 Frontend guide
  - Keep `prompts/requirements.md` as the definitive frontend guide. Link from `README.md`.
- E13.2 Repo `README.md`
  - Update with: quick start, build commands, profiles, OpenAPI workflow, testing, and troubleshooting (ports, Node version, OS notes).
- E13.3 `.junie/guidelines.md`
  - Document the day-to-day workflow for contributors (branching, commit messages, code review checklist).

Exit criteria:
- New devs can set up and contribute in <1 hour.

---

## Acceptance Criteria (Global)

- Build: `mvn -Pwith-frontend clean package` produces a runnable JAR serving the React build under `/` and APIs under `/api/*` (or current mapping).
- Spec: `openapi/npm test` passes; `npm run api:gen` generates clients with no compile errors.
- Quality: ESLint, Prettier, Vitest tests pass locally and in CI.
- Backend: DTOs used at controller boundaries; services are transactional; OSIV disabled; Flyway migrations auto-apply; global exception handler returns `ProblemDetails`.
- Security/Ops: Actuator exposure policy enforced; configuration bound to typed properties with validation.

---

## Risks & Mitigations

- Risk: Divergence between spec and implementation → Mitigation: contract-first flow; PR checklist requires spec and client regeneration.
- Risk: Node/NPM or plugin version drift → Mitigation: pin versions in Maven profile and `.nvmrc`/`.tool-versions` (optional) and document in README.
- Risk: OSIV disablement causing lazy-load errors → Mitigation: audit queries; add fetch joins/projections; write tests that serialize responses.
- Risk: CI flakiness due to ports/containers → Mitigation: random ports; dependency container reuse; backoff in tests.

---

## Work Breakdown Example (First 2 Weeks)

Week 1
- Verify and adjust Vite output path, Tailwind setup, scripts (E1.1–E1.2).
- Run `mvn -Pwith-frontend clean package`; fix any path or plugin issues (E1.3).
- Add/verify `.gitignore` entries (E1.5).
- Run Redocly lint; fix spec warnings; write `openapi/README` (E2.1).

Week 2
- Implement `api:gen` flow; choose generator; integrate with services (E2.2, E4.1).
- Scaffold AppLayout + Router; placeholder pages (E3.1–E3.2).
- Create beers service + `useBeers` hook and list page skeleton (E4.2–E5.1 initial).

---

## Repository Touchpoints (for implementers)

- Backend Java: `src/main/java/**` (controllers, services, mappers, DTOs), `src/main/resources/application-*.properties`, `db/migration`.
- Frontend: `src/main/frontend/**` (vite config, package.json, src/app, src/services, src/hooks, src/lib/api).
- OpenAPI: `openapi/openapi/openapi.yaml` plus modular files; `openapi/package.json` scripts.
- Build: `pom.xml` profile `with-frontend`.
- Docs: `README.md`, `prompts/requirements.md`, `prompts/plan.md`, `.junie/guidelines.md`.

---

## Definition of Done (per PR)

- Adheres to the Spring Boot and REST guidelines.
- Includes or updates OpenAPI as needed; clients regenerated.
- Unit/integration tests updated; Vitest tests for UI changes.
- Logging and configuration follow standards; no sensitive data in logs.
- Documentation updated where relevant.
