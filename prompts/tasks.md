# Detailed Task List — Based on prompts/plan.md

Note: Use [x] to mark items done. Keep tasks in order; complete all subtasks before marking a parent task complete.

## 0. Cross-Cutting Standards (apply in all work)
1. [ ] Adopt Spring Boot guidelines across modules (constructor injection, package-private components, typed properties, transaction boundaries, OSIV disabled, DTOs, REST design, centralized exception handling, actuator hardening, i18n, Testcontainers, random ports for ITs, logging hygiene, Flyway migrations).
2. [x] Maintain OpenAPI as single source of truth; validate with Redocly; keep spec current with implementation. (Redocly toolchain configured; openapi/README.md documents workflow; api clients generated in build)
3. [x] Enforce frontend principles (TypeScript strict mode, ESLint + Prettier, Vitest + RTL, generated API clients, consistent error handling, accessible UI). (Strict TS enabled; ESLint/Prettier scripts present; Vitest + RTL configured; API clients generated; error handling centralized)

## 1. Epic 1 — Project Foundation & Setup (M1)
1.1. [x] Verify frontend location and Vite baseline.
1.1.1. [x] Confirm `src/main/frontend` contains a Vite React TS app with strict TS config.
1.1.2. [x] Ensure `vite.config.ts` outputs build to `../resources/static` for prod.
1.1.3. [x] Add Tailwind v4 entry `src/main/frontend/src/styles/globals.css` and import in `main.tsx` (if missing).
1.2. [x] Standardize NPM scripts and align Node/NPM versions.
1.2.1. [x] In `src/main/frontend/package.json`, ensure scripts: `dev`, `build`, `preview`, `test`, `lint`, `format`, `api:gen`.
1.2.2. [x] Align Node/NPM versions with `frontend-maven-plugin` (Node v22.11.0, npm 10.9.0) and document rationale.
1.3. [x] Verify Maven integration for frontend.
1.3.1. [x] Keep `with-frontend` Maven profile wiring `ci` and `build` via `frontend-maven-plugin`.
1.3.2. [x] Verify `maven-clean-plugin` deletes `src/main/resources/static/**`.
1.4. [x] Configure backend dev profile & CORS (if needed) and rely on Vite proxy during dev. (Using Vite proxy; no backend CORS changes required)
1.5. [x] Ensure `.gitignore` excludes `node_modules`, `src/main/resources/static`, and local tool caches.
1.6. [x] Exit criteria validation for Epic 1.
1.6.1. [x] `mvn -Pwith-frontend clean package` produces a JAR serving the Vite build.
1.6.2. [x] `npm run dev` runs the frontend with API proxy to Spring Boot.

## 2. Epic 2 — OpenAPI & API Client Strategy (M2)
2.1. [x] Set up Redocly toolchain.
2.1.1. [x] From `openapi/`, run `npm ci` and `npm test` (Redocly lint) to validate `openapi/openapi/openapi.yaml` and refs.
2.1.2. [x] Add a simple `openapi/README.md` explaining `npm start`, `npm run build`, `npm test` usage.
2.2. [x] Implement API client generation.
2.2.1. [x] Choose a generator (`openapi-typescript` + axios wrapper OR `openapi-generator-cli typescript-axios`).  (Chosen: openapi-typescript-codegen)
2.2.2. [x] Implement `api:gen` script outputting to `src/main/frontend/src/lib/api` (immutable types and clients).
2.2.3. [x] Gate commits by regenerating clients on spec change (document/check in scripts). (Build runs api:gen before vite build)
2.3. [x] Establish contract-first workflow (Update spec → lint → generate clients → implement backend & UI). (Documented in openapi/README.md)
2.4. [x] Exit criteria validation for Epic 2.
2.4.1. [x] `npm run api:gen` produces typed clients and models with no compile errors.
2.4.2. [x] Redocly `npm test` lints clean.

## 3. Epic 3 — Application Shell & Navigation (M3)
3.1. [x] Configure React Router with nested routes, error boundaries, and lazy routes. (createBrowserRouter + lazy + errorElement)
3.2. [x] Implement `AppLayout` with header, navigation, content area, and footer (responsive). (added footer, skip link, responsive nav)
3.3. [x] Add basic theming via Tailwind; optional dark mode toggle. (ThemeProvider + dark-mode toggle)
3.4. [x] Implement global error boundary and toast/notification pattern for loading/error UX. (ErrorBoundary + Radix Toasts)
3.5. [x] Exit criteria validation for Epic 3: Navigable shell with placeholder pages for Beers, Customers, Beer Orders. (Home, Beers, Customers, Orders routes reachable)

## 4. Epic 4 — Data Layer & Services (M2–M4)
4.1. [x] Implement shared Axios instance with base URL, interceptors (auth if any), error normalization, and timeouts.
4.2. [x] Create services per resource using generated clients.
4.2.1. [x] `src/main/frontend/src/services/beers.ts` exposing CRUD functions.
4.2.2. [x] `src/main/frontend/src/services/customers.ts` exposing CRUD functions.
4.2.3. [x] `src/main/frontend/src/services/orders.ts` exposing CRUD functions.
4.3. [x] Create data-fetching hooks.
4.3.1. [x] `useBeers` hook.
4.3.2. [x] `useCustomers` hook.
4.3.3. [x] `useOrders` hook.
4.3.4. [x] Consider React Query integration (decision: not adopted yet; simple hooks suffice for now).
4.4. [x] Exit criteria validation for Epic 4: Components use hooks/services only; no direct axios usage in components.

## 5. Epic 5 — Feature Delivery (CRUD) (M4)
5.0. [x] Establish initial feature order: 1) Beers, 2) Customers, 3) Beer Orders.

5.A Beers
5.1. [x] Backend API check for Beers: endpoints present in OpenAPI; adjust spec if needed; regenerate clients.
5.2. [x] DTOs & MapStruct for Beers: ensure no entity leakage at controllers.
5.3. [x] Transactions for Beer services: annotate with `@Transactional` / `@Transactional(readOnly = true)` accordingly.
5.4. [x] Controller contracts for Beers: `ResponseEntity<T>`, proper status codes, pagination as applicable.
5.5. [ ] Frontend Beers pages & components: list (with filtering/pagination if supported), detail, create/edit forms, delete flows, optimistic UX.
5.6. [x] Validation & errors for Beers: Jakarta validation; propagate `ProblemDetails`; user-friendly UI errors.

5.B Customers
5.7. [x] Backend API check for Customers; adjust spec; regenerate clients.
5.8. [x] DTOs & MapStruct for Customers.
5.9. [x] Transactions for Customer services.
5.10. [x] Controller contracts for Customers.
5.11. [x] Frontend Customers pages & components. (List and detail pages implemented: CustomersList, CustomersDetails; services and hooks wired)
5.12. [x] Validation & errors for Customers.

5.C Beer Orders
5.13. [x] Backend API check for Beer Orders; adjust spec; regenerate clients.
5.14. [x] DTOs & MapStruct for Beer Orders.
5.15. [x] Transactions for Order services.
5.16. [x] Controller contracts for Beer Orders.
5.17. [ ] Frontend Beer Orders pages & components (list, detail, create/update, manage shipments).
5.18. [x] Validation & errors for Beer Orders.

5.19. [ ] Exit criteria validation for Epic 5: Full CRUD for Beers, Customers, and Beer Orders with E2E happy paths.

## 6. Epic 6 — Persistence & Migrations (M1–M4)
6.1. [x] Verify Flyway setup: migrations under `src/main/resources/db/migration/` using `V…__…` naming; H2-compatible local SQL.
6.2. [x] Ensure FK changes follow two-step pattern (add column, then `ALTER TABLE` to add constraint).
6.3. [x] Add repeatable migrations `R__...` for views/reference data as needed.
6.4. [x] Exit criteria validation for Epic 6: App boots clean with Flyway applying all migrations from scratch. (Verified V1–V3 plus repeatable R__beer_views.sql; two-step FKs present)

## 7. Epic 7 — Exception Handling & OSIV (M1–M3)
7.1. [x] Implement global `@RestControllerAdvice` returning RFC 9457 `ProblemDetails` for common and validation exceptions. (Validation, not-found, conflict, and generic 500 covered)
7.2. [x] Disable OSIV: set `spring.jpa.open-in-view=false`; audit and replace lazy-loads with fetch joins/projections. (Already set in application.properties; controllers use DTOs)
7.3. [x] Exit criteria validation for Epic 7: Consistent error JSON; no lazy-loading at serialization time.

## 8. Epic 8 — Configuration & Security (M6)
8.1. [x] Introduce `@ConfigurationProperties` classes for app-specific settings with validation annotations. (ShipmentApiProperties bound with @EnableConfigurationProperties and @Validated)
8.2. [x] Prefer env vars for environment differences; document mapping and usage. (Properties are namespaced; can be overridden via env: SHIPMENT_API_ENFORCE_ORDER_MATCH_ON_GET)
8.3. [x] Actuator exposure: expose `/actuator/health`, `/info`, `/metrics` anonymously; secure all others; document non-prod relaxations. (Configured exposure include; no extra endpoints are exposed)
8.4. [x] Exit criteria validation for Epic 8: App fails fast on invalid config; actuator endpoints follow policy. (Typed props bound; actuator exposure configured)

## 9. Epic 9 — Internationalization (M6–M7)
9.1. [x] Externalize backend messages to `messages.properties` (and `messages_{locale}.properties` for future locales).
9.2. [x] Frontend i18n readiness: introduce light i18n layer (e.g., `i18next`) and move strings to resource files.
9.3. [x] Exit criteria validation for Epic 9: No hard-coded end-user copy in controllers or core UI components.

## 10. Epic 10 — Testing Strategy (M5)
10.1. [x] Backend unit and slice tests (services with constructor injection; repositories with `@DataJpaTest`).
10.2. [x] Integration tests with Testcontainers (Postgres) and random port `@SpringBootTest(webEnvironment = RANDOM_PORT)`.
10.3. [x] Frontend tests with Vitest + RTL; mock network via MSW.
10.4. [x] Contract checks validating OpenAPI responses and `ProblemDetails` on error paths.
10.5. [x] Exit criteria validation for Epic 10: CI runs tests reliably in parallel; random ports prevent conflicts.

## 11. Epic 11 — Logging & Observability (M5–M6)
11.1. [x] Enforce SLF4J usage; remove `System.out.println`; guard expensive debug logs; avoid logging sensitive data.
11.2. [ ] (Optional) Add request correlation IDs via MDC; surface correlation IDs in frontend error reporting if feasible.
11.3. [x] Ensure key Micrometer metrics; expose via `/actuator/metrics`.
11.4. [x] Exit criteria validation for Epic 11: Clean structured logging; basic metrics visible.

## 12. Epic 12 — CI/CD & Release (M5–M6)
12.1. [x] Implement CI pipeline steps: checkout → JDK 21 → cache Maven & npm → `openapi/npm ci && npm test` → `mvn -Pwith-frontend -B clean verify` → publish artifacts.
12.2. [x] Add static analysis gates (SpotBugs, Checkstyle or Spring Java format, ESLint) and fail build on critical issues.
12.3. [x] Configure release artifacts: include fat JAR and optionally bundled OpenAPI `dist/bundle.yaml`.
12.4. [x] Exit criteria validation for Epic 12: CI green with caching; artifacts uploaded; linting enforced.

## 13. Epic 13 — Developer Documentation (M7)
13.1. [x] Maintain `prompts/requirements.md` as definitive frontend guide; link from `README.md`. (Added link and quick-start section in README)
13.2. [x] Update top-level `README.md` with quick start, build commands, profiles, OpenAPI workflow, testing, troubleshooting (ports, Node version, OS notes). (Added Frontend Quick Start, Build, and OpenAPI sections)
13.3. [x] Create/update `.junie/guidelines.md` to document contributor workflow (branching, commit messages, code review checklist). (Added contributor workflow section)
13.4. [ ] Exit criteria validation for Epic 13: New devs can set up and contribute in under 1 hour.

## 14. Acceptance Criteria (Global) — Verification
14.1. [x] Build: `mvn -Pwith-frontend clean package` produces a runnable JAR serving React build and APIs. (Validated in 1.6.1)
14.2. [x] Spec: `openapi/npm test` passes; `npm run api:gen` generates clients with no compile errors. (Validated in 2.4.1–2.4.2)
14.3. [ ] Quality: ESLint, Prettier, Vitest tests pass locally and in CI.
14.4. [x] Backend: DTOs at controller boundaries; services transactional; OSIV disabled; Flyway migrations apply; global exception handler returns `ProblemDetails`. (Per README and config)
14.5. [x] Security/Ops: Actuator exposure policy enforced; configuration bound to typed properties with validation. (Epic 8 completed)

## 15. Risks & Mitigations — Ongoing Checks
15.1. [ ] Prevent spec/implementation drift (contract-first flow; PR checklist includes spec and client regeneration).
15.2. [ ] Pin Node/NPM versions and document (`.nvmrc`/`.tool-versions` optional) to avoid drift; align with Maven profile.
15.3. [ ] Mitigate OSIV disablement issues (audit queries; add fetch joins/projections; tests that serialize responses).
15.4. [ ] Reduce CI flakiness (random ports; container reuse; backoff in tests where needed).

## 16. Work Breakdown — First Two Weeks (Guidance)
16.1. [x] Week 1: Verify Vite output path, Tailwind setup, scripts (E1.1–E1.2).
16.2. [x] Week 1: Run `mvn -Pwith-frontend clean package`; fix plugin/path issues (E1.3). (Validated in 1.6.1)
16.3. [x] Week 1: Add/verify `.gitignore` entries (E1.5).
16.4. [x] Week 1: Run Redocly lint; fix spec warnings; write `openapi/README` (E2.1). (Completed per 2.1.1–2.1.2)
16.5. [x] Week 2: Implement `api:gen` flow; choose generator; integrate with services (E2.2, E4.1). (Completed per 2.2 and 4.1)
16.6. [x] Week 2: Scaffold `AppLayout` + Router; placeholder pages (E3.1–E3.2). (Completed per 3.1–3.2)
16.7. [x] Week 2: Create beers service + `useBeers` hook and list page skeleton (E4.2–E5.1 initial). (Completed per 4.2–4.3 and BeersList)
