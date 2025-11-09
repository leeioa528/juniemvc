# Detailed Task Checklist derived from prompts/plan.md

Note: Mark tasks as done by changing [ ] to [x]. Follow the sequence Epic 1 → Epic 9.

## Epic 1 — Project Foundation & Setup

1.1 Create React app scaffolding (Vite + TS)
- [x] Create directory `src/main/frontend` and initialize Vite React SWC TS template.
- [x] Set package name to `juniemvc-frontend`.
- [ ] Verify dev server runs (`npm run dev`).

1.2 Install core dependencies and UI stack
- [x] Install React 19, React Router, TypeScript, Vite and `@vitejs/plugin-react`.
- [x] Install styling dependencies: Tailwind CSS v4, PostCSS, Autoprefixer, `class-variance-authority`, `clsx`, `tailwind-merge`, `lucide-react`, and required Radix primitives.
- [ ] Add optional `tw-animate-css` if animations are required.

1.3 Initialize Tailwind CSS v4
- [x] Create `src/styles/index.css` with Tailwind layer imports (v4 file-less config model).
- [x] Import styles in `src/main.tsx`.
- [x] Add basic CSS variables for light/dark if using shadcn tokens.
- [ ] Confirm Tailwind works in dev with a sample component.

1.4 Initialize shadcn UI baseline
- [x] Add `cn()` utility in `src/lib/utils.ts`.
- [x] Create minimal design tokens (CSS vars) and a base Theme wrapper if needed.
- [x] Add initial components: Button, Input, Dialog, Table primitives (based on shadcn/Radix patterns).
- [x] Create a sandbox page with examples/stories for the primitives.

1.5 Tooling setup (ESLint, Prettier, TS config)
- [x] Add ESLint config with `@typescript-eslint`, `eslint-plugin-react`, `eslint-plugin-react-hooks`, and Prettier integration (`eslint-config-prettier`).
- [x] Configure `tsconfig` paths and strict compiler options.
- [x] Add `.editorconfig` and Prettier config to normalize formatting.
- [ ] Optionally add Husky + lint-staged for changed-file linting on commit.
- [ ] Ensure `npm run lint` works and passes.

## Epic 2 — Build Integration & Configuration

2.1 Configure Vite for development
- [x] Setup dev server proxy to Spring Boot backend (proxy `/api` and `/actuator` to `http://localhost:8080`) with needed CORS headers.
- [x] Configure `.env`/`.env.development` for API base URL if required (prefer relative URLs with proxy in dev).
- [ ] Confirm local dev hot reload and proxied requests function correctly.

2.2 Configure Vite for production build output
- [x] Set `build.outDir` to `../resources/static` (relative to `src/main/frontend`).
- [x] Ensure `build.emptyOutDir = true` to clean `static` before build.
- [x] Verify `index.html` and hashed assets are emitted under `src/main/resources/static` after `npm run build`.

2.3 Frontend scripts in package.json
- [x] Add scripts: `dev`, `build`, `preview`, `test`, `test:watch`, `lint`, `typecheck`, `api:gen`.
- [ ] Ensure scripts run under Node 20/22 as assumed by the toolchain.

2.4 Integrate with Maven build (frontend-maven-plugin)
- [x] Add `frontend-maven-plugin` executions in `pom.xml` for `npm ci` (phase `generate-resources`).
- [x] Add execution for `npm run build` (phase `prepare-package`).
- [x] Set `<workingDirectory>${project.basedir}/src/main/frontend</workingDirectory>`.
- [x] Verify `mvn -q -DskipTests package` builds the frontend and nests static assets into the JAR.

2.5 Configure Maven clean for frontend artifacts
- [x] Configure `maven-clean-plugin` to delete `src/main/resources/static/*` on `mvn clean`.
- [ ] Optionally configure cleaning of `node_modules` and Vite cache via profiles for CI.
- [ ] Verify `mvn clean` removes generated frontend artifacts safely.

## Epic 3 — API Integration & Data Layer

3.1 Generate TypeScript client and models from OpenAPI
- [x] Add `openapi-typescript-codegen` and set output to `src/main/frontend/src/lib/api` (or `src/lib/api-client`).
- [x] Configure npm script `api:gen` pointing to `openapi/openapi/openapi.yaml`.
- [x] Choose generation pattern: (B) generate axios client directly for faster start.
- [x] Add README in the api folder documenting regeneration command and check-in policy.
- [x] Run `npm run api:gen` to generate the client and types.

3.2 Create Axios instance with interceptors
- [x] Create `src/lib/axios.ts` exporting configured Axios instance.
- [x] Set base URL to relative `/api` (works with dev proxy and prod reverse proxy).
- [x] Add request interceptor to set JSON headers.
- [x] Add response interceptor to normalize error objects; consider retry/backoff for idempotent GETs.
- [x] Integrate axios instance with generated client (if using axios-based generation) via DI or configuration.

3.3 Service layer per resource
- [x] Create `src/services/beers.service.ts` with list (pagination), getById, create, update, delete.
- [x] Create `src/services/customers.service.ts` with list, getById, create, update, delete.
- [x] Create `src/services/orders.service.ts` with list, getById, create, update (and delete if applicable).
- [ ] Map OpenAPI DTOs to view models where needed; re-export types for UI consumption.
- [ ] Add unit tests for services mocking axios.

## Epic 4 — Application Architecture & UI Shell

4.1 Routing scaffold with nested layouts
- [ ] Add React Router v7 routes: `/`, `/beers`, `/beers/:id`, `/customers`, `/customers/:id`, `/orders`, `/orders/:id`.
- [ ] Use a top-level `AppLayout` with header/sidebar and an `Outlet` for nested routes.
- [ ] Add a `NotFound` route.
- [ ] Code-split pages with lazy loading where appropriate.

4.2 AppLayout and navigation
- [ ] Implement `AppLayout` using shadcn components: top nav, sidebar, breadcrumb, main content container.
- [ ] Include global toasts area (Radix Toast) and a `ConfirmDialog` portal.
- [ ] Add active route highlighting and accessible skip links.

4.3 Shared hooks and utilities
- [ ] Create hooks: `usePaginatedQuery`, `useDebouncedValue`, resource hooks `useBeers`/`useCustomers`/`useOrders`, and `useApiError`.
- [ ] Add helpers for date/number formatting and query param synchronization.
- [ ] Add tests and examples for the hooks.

## Epic 5 — Feature Implementation: Beers (CRUD)

5.1 List Beers page with pagination and filters
- [ ] Build table view using shadcn Table; columns: name, style, price, upc, quantityOnHand, actions.
- [ ] Wire pagination controls to backend query params; add client-side page size control.
- [ ] Add optional text filter (name contains) if backend supports.
- [ ] Implement empty state and loading skeletons.

5.2 Beer details page
- [ ] Implement `/beers/:id` details view with key fields.
- [ ] Include Edit and Delete actions.
- [ ] Handle 404 by redirecting to list with a toast.

5.3 Create/Update Beer forms
- [ ] Create reusable `BeerForm` using shadcn Form primitives with Zod + `react-hook-form` validation.
- [ ] Support create (dialog or page) and update (dialog/page) flows.
- [ ] Add toasts for success and error; consider optimistic UI.

5.4 Delete Beer
- [ ] Add `ConfirmDialog` before delete.
- [ ] Show toast on success and refresh list.
- [ ] Add tests for the delete flow.

## Epic 6 — Feature Implementation: Customers (CRUD)

6.1 List Customers page
- [ ] Build table with basic fields (name, email, phone, etc.) and pagination.
- [ ] Implement route `/customers`.

6.2 Customer details page
- [ ] Implement `/customers/:id` detail view.
- [ ] Link to related orders if available.
- [ ] Implement error handling states.

6.3 Create/Update/Delete Customer
- [ ] Create `CustomerForm` with validation (react-hook-form + zod).
- [ ] Implement create and update flows.
- [ ] Implement delete with confirmation and toasts.

## Epic 7 — Feature Implementation: Beer Orders

7.1 List Orders page
- [ ] Build table of orders: id, customer, status, total items, created date.
- [ ] Add pagination and basic filtering (status/customer).
- [ ] Implement route `/orders`.

7.2 Order details
- [ ] Implement `/orders/:id` detail view showing line items, totals, status history (as available), and actions.
- [ ] Add loading and error states.

7.3 Create/Update Order
- [ ] Implement order builder form: select customer, add/remove beer line items (beer autocomplete + quantity), compute totals.
- [ ] Persist via service; surface backend validation errors.
- [ ] Complete end-to-end order creation/update UX.

## Epic 8 — Quality, Testing, and DX

8.1 Testing setup
- [ ] Install Vitest, `@testing-library/react`, `@testing-library/user-event`, `@testing-library/jest-dom`, `jsdom`.
- [ ] Configure Vitest in `vite.config.ts` or `vitest.config.ts` (env `jsdom`, setup file for RTL matchers).
- [ ] Add example tests for a component, a hook (with MSW or axios mock), and a service.
- [ ] Ensure `npm test` runs and reports coverage via v8 provider.

8.2 ESLint & Prettier enforcement
- [ ] Add rules for React 19, hooks, and `@typescript-eslint` recommended; optionally `import/order` rule.
- [ ] Add `npm run lint` and `npm run format` scripts.
- [ ] Make CI fail on lint errors; establish a clean lint baseline.

8.3 Mocking and API test strategy
- [ ] Choose between MSW for component/integration tests or `axios-mock-adapter` for service tests.
- [ ] Implement one example for each chosen mocking approach.
- [ ] Document the approach and include sample specs.

## Epic 9 — Documentation & Workflow

9.1 Developer workflow guide (`.junie/guidelines.md`)
- [ ] Document local setup, scripts, code conventions, directory layout, and PR checklist.
- [ ] Document API client generation: when to regenerate and how to handle breaking changes.
- [ ] Note relevant Spring Boot backend guidelines that impact the frontend workflow.

9.2 Local dev and production build verification
- [ ] Document concurrent dev workflow: backend on :8080, frontend Vite dev server on :5173 with proxy.
- [ ] Document production build path: `mvn -q package` produces JAR that serves UI at `http://localhost:8080/`.
- [ ] Add troubleshooting tips (CORS, proxy mismatches, 404 on refresh → configure SPA fallback in Spring if needed).
