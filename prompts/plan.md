# React Frontend Implementation Plan (Epics → User Stories/Tasks)

Source of truth: prompts/requirementsLee.md and OpenAPI spec at openapi/openapi/openapi.yaml. Target location for the frontend app: src/main/frontend. Production assets served by Spring Boot from src/main/resources/static.

Note on sequencing: Epics 1 → 3 are foundational and largely sequential. Epics 4–6 can proceed in parallel once the API client and Axios instance exist. Epic 7 runs continuously but has concrete deliverables.


## Epic 1 — Project Foundation & Setup
Outcome: A Vite + React + TypeScript app initialized under src/main/frontend with Tailwind and shadcn UI ready, standard tooling in place.

1.1 Create React app scaffolding (Vite + TS)
- Tasks:
  - Create directory src/main/frontend and initialize Vite React SWC TS template.
  - Set package name to "juniemvc-frontend".
  - Verify dev server runs (`npm run dev`).
- Deliverables:
  - src/main/frontend with index.html, vite.config.ts, tsconfig.json, src/main.tsx.

1.2 Install core dependencies and UI stack
- Tasks:
  - Install React 19, React Router, TypeScript, Vite + plugin-react.
  - Install styling dependencies: Tailwind CSS v4, PostCSS, Autoprefixer, class-variance-authority, clsx, tailwind-merge, lucide-react, Radix primitives used by shadcn components.
  - Add optional tw-animate-css if animations are required.
- Deliverables:
  - Updated package.json with deps and devDeps as in requirementsLee.md.

1.3 Initialize Tailwind CSS v4
- Tasks:
  - Create src/styles/index.css with Tailwind layer imports (v4’s file-less config model).
  - Import styles in src/main.tsx.
  - Add basic CSS variables for light/dark if using shadcn tokens.
- Deliverables:
  - Tailwind working in dev (inspect with a sample component).

1.4 Initialize shadcn UI baseline
- Tasks:
  - Add utility helpers (cn()) in src/lib/utils.ts.
  - Create minimal design tokens (CSS vars) and a base Theme wrapper if needed.
  - Add initial components: Button, Input, Dialog, Table primitives (either hand-rolled or from shadcn patterns using Radix primitives).
- Deliverables:
  - Reusable UI primitives folder in src/components/ui/* with stories/examples in a sandbox page.

1.5 Tooling setup (ESLint, Prettier, TS config)
- Tasks:
  - Add ESLint config with @typescript-eslint, react, react-hooks and Prettier integration (eslint-config-prettier).
  - Configure tsconfig paths and strict compiler options.
  - Add .editorconfig and Prettier config to normalize formatting.
  - Add husky + lint-staged (optional) to enforce changed-file linting on commit.
- Deliverables:
  - Lint passes; `npm run lint` script working.


## Epic 2 — Build Integration & Configuration
Outcome: Vite configured for dev proxy to Spring Boot; production build emitted into src/main/resources/static; wired into Maven lifecycle.

2.1 Configure Vite for development
- Tasks:
  - Setup dev server proxy to Spring Boot backend (e.g., proxy /api and /actuator to http://localhost:8080) with CORS headers as needed.
  - Configure env variables via .env/.env.development for API base URL if required; prefer relative URLs with proxy in dev.
- Deliverables:
  - vite.config.ts proxy section; local dev hot reload confirmed.

2.2 Configure Vite for production build output
- Tasks:
  - Set build.outDir to ../resources/static (relative to src/main/frontend) so Vite outputs assets that Spring Boot will serve.
  - Ensure build.emptyOutDir = true to clean static before build (coordinated with Maven clean plugin too).
  - Verify index.html and assets hashed filenames are emitted under static/.
- Deliverables:
  - Production build artifacts appear in src/main/resources/static after `npm run build`.

2.3 Frontend scripts in package.json
- Tasks:
  - Add scripts: dev, build, preview, test, test:watch, lint, typecheck, api:gen.
  - Add cross-platform environment handling (Node 20/22 assumed by toolchain).
- Deliverables:
  - package.json scripts ready for devs and CI.

2.4 Integrate with Maven build (frontend-maven-plugin)
- Tasks:
  - In pom.xml, add frontend-maven-plugin executions for `install node and npm` (if desired) and `npm ci` + `npm run build` bound to Maven phases (`generate-resources` or `prepare-package`).
  - Set workingDirectory to ${project.basedir}/src/main/frontend.
  - Prefer `npm ci` for reproducibility.
- Deliverables:
  - `mvn -q -DskipTests package` builds frontend and nests static assets into the JAR.

2.5 Configure Maven clean for frontend artifacts
- Tasks:
  - Add maven-clean-plugin configuration to delete src/main/resources/static/* on `mvn clean`.
  - Optionally clean node_modules and vite cache via plugin profiles if desired for CI.
- Deliverables:
  - `mvn clean` removes generated frontend artifacts safely.


## Epic 3 — API Integration & Data Layer
Outcome: Typed client generated from OpenAPI; Axios instance with interceptors; service modules encapsulate data access per resource.

3.1 Generate TypeScript client and models from OpenAPI
- Tasks:
  - Use openapi-typescript-codegen to generate into src/main/frontend/src/lib/api (or src/lib/api-client).
  - Configure npm script `api:gen` pointing to openapi/openapi/openapi.yaml; set `--useOptions`, `--exportCore false` if wrapping via Axios, or `--client axios` if using generated client.
  - Decide pattern: (A) generate fetch-based client and wrap with Axios, or (B) generate axios client directly. Choose B for faster start.
  - Add README in api folder documenting regen command.
- Deliverables:
  - Generated client code under src/lib/api, not checked in or checked-in per team policy (documented).

3.2 Create Axios instance with interceptors
- Tasks:
  - Create src/lib/axios.ts exporting configured Axios instance.
  - Base URL: use relative `/api` for dev with proxy; in prod same path served by Boot reverse proxy.
  - Add request interceptor to set JSON headers; add response interceptor for unified error objects; optional retry/backoff for idempotent GETs.
  - Integrate with generated client if using axios-based generation via dependency injection of axios instance.
- Deliverables:
  - Shared axios instance with robust error handling.

3.3 Service layer per resource
- Tasks:
  - Create modules: src/services/beers.service.ts, customers.service.ts, orders.service.ts.
  - Each exposes functions for list (with pagination), getById, create, update, delete (where applicable).
  - Map OpenAPI request/response DTOs to view models when needed; keep types re-exported for UI consumption.
- Deliverables:
  - Stable API facade for UI; unit tests mocking axios.


## Epic 4 — Application Architecture & UI Shell
Outcome: App routing, layout, navigation, and shared UX primitives in place.

4.1 Routing scaffold with nested layouts
- Tasks:
  - Add React Router v7 routes: `/`, `/beers`, `/beers/:id`, `/customers`, `/customers/:id`, `/orders`, `/orders/:id`.
  - Use a top-level `AppLayout` with header/sidebar and an `Outlet` for nested routes.
  - Add a NotFound route.
- Deliverables:
  - src/app/routes with route elements and lazy-loaded pages (code-splitting).

4.2 AppLayout and navigation
- Tasks:
  - Implement `AppLayout` using shadcn components: top nav, sidebar, breadcrumb, main content container.
  - Include global toasts area (Radix Toast) and a ConfirmDialog portal.
  - Add active route highlighting and accessible skip links.
- Deliverables:
  - Consistent layout and navigation across pages.

4.3 Shared hooks and utilities
- Tasks:
  - Create hooks: usePaginatedQuery, useDebouncedValue, useBeers/useCustomers/useOrders (wrapping services), useApiError.
  - Add helpers for date/number formatting and query param sync.
- Deliverables:
  - Hooks with tests and examples.


## Epic 5 — Feature Implementation: Beers (CRUD)
Outcome: Complete Beer management UX with pagination.

5.1 List Beers page with pagination and filters
- Tasks:
  - Table view using shadcn Table; columns: name, style, price, upc, quantityOnHand (as available in backend), actions.
  - Pagination controls bound to backend query params; optional client-side page size control.
  - Optional text filter (name contains) if backend supports.
- Deliverables:
  - Route `/beers` functional; empty-state and loading-skeletons implemented.

5.2 Beer details page
- Tasks:
  - `/beers/:id` shows key fields; includes Edit and Delete actions.
  - Handle 404 by redirecting to list with toast.
- Deliverables:
  - Details page with error states covered.

5.3 Create/Update Beer forms
- Tasks:
  - Reusable BeerForm component using shadcn Form primitives with Zod validation (install zod + @hookform/resolvers + react-hook-form).
  - Support create (dialog or page) and update (dialog/page) flows; optimistic UI optional.
- Deliverables:
  - Form validation, submit, success/error toasts wired to services.

5.4 Delete Beer
- Tasks:
  - Add ConfirmDialog before delete; show toast on success; refresh list.
- Deliverables:
  - Delete flow complete and covered by tests.


## Epic 6 — Feature Implementation: Customers (CRUD)
Outcome: Customer management UX.

6.1 List Customers page
- Tasks:
  - Table with basic fields (name, email, phone, etc. as per API); pagination.
- Deliverables:
  - Route `/customers` functional.

6.2 Customer details page
- Tasks:
  - `/customers/:id` detail view; link to related orders if available.
- Deliverables:
  - Details implemented with error handling.

6.3 Create/Update/Delete Customer
- Tasks:
  - CustomerForm with validation (react-hook-form + zod).
  - Create and update flows; delete with confirmation.
- Deliverables:
  - Full CRUD flows with toasts and redirects.


## Epic 7 — Feature Implementation: Beer Orders
Outcome: Order listing, detail, and creation/update flows.

7.1 List Orders page
- Tasks:
  - Table of orders: id, customer, status, total items, created date; pagination and basic filtering (status/customer).
- Deliverables:
  - Route `/orders` functional.

7.2 Order details
- Tasks:
  - `/orders/:id` shows line items, totals, status history (as available), and actions.
- Deliverables:
  - Detail view with loading and error states.

7.3 Create/Update Order
- Tasks:
  - Order builder form: select customer, add/remove beer line items (beer autocomplete + quantity), compute totals.
  - Persist via service; show validation errors from backend.
- Deliverables:
  - End-to-end order creation/update UX.


## Epic 8 — Quality, Testing, and DX
Outcome: Reliable test environment, consistent coding standards, and CI-ready scripts.

8.1 Testing setup
- Tasks:
  - Install Vitest, @testing-library/react, @testing-library/user-event, @testing-library/jest-dom, jsdom.
  - Configure vitest in vite.config.ts or vitest.config.ts (environment: jsdom, setup file for RTL matchers).
  - Add example tests for a component, a hook (with MSW or axios mock), and a service.
- Deliverables:
  - `npm test` runs and reports coverage via v8 provider.

8.2 ESLint & Prettier enforcement
- Tasks:
  - Add rules for React 19, hooks, typescript-eslint recommended; enable import/order rule optionally.
  - Add `npm run lint` and `npm run format` scripts; make CI fail on lint errors.
- Deliverables:
  - Clean lint baseline and formatting consistency.

8.3 Mocking and API test strategy
- Tasks:
  - Choose between MSW for component/integration tests or axios-mock-adapter for service tests; implement one example each.
- Deliverables:
  - Documented approach and sample specs.


## Epic 9 — Documentation & Workflow
Outcome: Clear developer workflow, local dev steps, and production build docs.

9.1 Developer workflow guide (.junie/guidelines.md)
- Tasks:
  - Document local setup, scripts, code conventions, directory layout, and PR checklist.
  - Include guidance on generating the API client, when to regenerate, and how to reconcile breaking changes.
  - Note on Spring Boot guidelines (constructor injection, package-private controllers) for any backend touches needed by the frontend.
- Deliverables:
  - New/updated .junie/guidelines.md.

9.2 Local dev and production build verification
- Tasks:
  - Document concurrent dev workflow: run backend on :8080, frontend vite dev server on :5173 with proxy.
  - Document production build path: `mvn -q package` produces JAR that serves UI at `http://localhost:8080/`.
  - Include troubleshooting tips (CORS, proxy mismatches, 404 on refresh → configure SPA fallback in Spring if needed).
- Deliverables:
  - README section or doc in prompts/ confirming both workflows.


## Cross-Cutting Non-Functional Requirements
- Accessibility: Use semantic HTML, ARIA roles from Radix, keyboard navigation, focus management on route/dialog open.
- Performance: Code-split routes, use React.lazy/Suspense, leverage React Router data APIs where suitable; cache list queries as appropriate.
- Error Handling: Centralize API error normalization; show user-friendly messages via toasts and page-level error boundaries.
- i18n (optional): Prepare for i18n by externalizing strings where feasible.


## Milestones & Suggested Order
1) Epics 1–2 (Foundation + Build Integration)
2) Epic 3 (API Client + Axios + Services)
3) Epic 4 (Routing + Layout + Hooks)
4) Epic 5 (Beers CRUD)
5) Epic 6 (Customers CRUD)
6) Epic 7 (Orders)
7) Epics 8–9 (Quality + Docs)


## Acceptance Criteria Summary (per Epic)
- Epic 1: `npm run dev` shows a styled skeleton page using shadcn components; lint passes.
- Epic 2: `mvn package` places built assets under src/main/resources/static and serves them from the JAR.
- Epic 3: `npm run api:gen` generates types; services call backend successfully; shared axios instance handles errors globally.
- Epic 4: Navigable shell with working routes and NotFound; layout consistent.
- Epic 5–7: CRUD flows are functional end-to-end with validation, toasts, and loading states.
- Epic 8: `npm test` executes example unit/integration tests with coverage; lint/format enforced.
- Epic 9: Clear docs exist for both dev and production workflows.


## Implementation Details & Snippets

A) Example vite.config.ts essentials
```ts
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'node:path'

export default defineConfig(({ mode }) => ({
  plugins: [react()],
  root: '.',
  server: {
    port: 5173,
    strictPort: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/actuator': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
  build: {
    outDir: path.resolve(__dirname, '../resources/static'),
    emptyOutDir: true,
  },
  test: {
    environment: 'jsdom',
    setupFiles: ['./src/test/setup.ts'],
  },
}))
```

B) Axios instance (src/lib/axios.ts)
```ts
import axios from 'axios'

export const api = axios.create({
  baseURL: '/api',
  withCredentials: false,
  headers: { 'Content-Type': 'application/json' },
})

api.interceptors.response.use(
  (res) => res,
  (error) => {
    const status = error?.response?.status
    const message = error?.response?.data?.detail || error.message
    return Promise.reject({ status, message, original: error })
  }
)
```

C) OpenAPI client generation script (package.json)
```json
{
  "scripts": {
    "api:gen": "openapi-typescript-codegen --input ../../../../openapi/openapi/openapi.yaml --output ./src/lib/api --client axios",
    "dev": "vite",
    "build": "vite build",
    "preview": "vite preview",
    "test": "vitest run",
    "test:watch": "vitest",
    "lint": "eslint .",
    "typecheck": "tsc --noEmit"
  }
}
```

D) Maven frontend-maven-plugin snippet (pom.xml)
```xml
<plugin>
  <groupId>com.github.eirslett</groupId>
  <artifactId>frontend-maven-plugin</artifactId>
  <version>1.15.0</version>
  <configuration>
    <workingDirectory>${project.basedir}/src/main/frontend</workingDirectory>
  </configuration>
  <executions>
    <execution>
      <id>npm-ci</id>
      <goals><goal>npm</goal></goals>
      <phase>generate-resources</phase>
      <configuration>
        <arguments>ci</arguments>
      </configuration>
    </execution>
    <execution>
      <id>npm-build</id>
      <goals><goal>npm</goal></goals>
      <phase>prepare-package</phase>
      <configuration>
        <arguments>run build</arguments>
      </configuration>
    </execution>
  </executions>
</plugin>
```

E) Maven clean plugin to remove generated static assets
```xml
<plugin>
  <artifactId>maven-clean-plugin</artifactId>
  <version>3.3.2</version>
  <configuration>
    <filesets>
      <fileset>
        <directory>${project.basedir}/src/main/resources/static</directory>
      </fileset>
    </filesets>
  </configuration>
</plugin>
```

F) Example services (src/services/beers.service.ts)
```ts
import { api } from '@/lib/axios'
import type { BeerDto, CreateBeerRequest, UpdateBeerRequest } from '@/lib/api'

export async function listBeers(params: { page?: number; size?: number; q?: string } = {}) {
  const res = await api.get<BeerDto[]>(`/beers`, { params })
  return res.data
}

export async function getBeer(id: string) {
  const res = await api.get<BeerDto>(`/beers/${id}`)
  return res.data
}

export async function createBeer(payload: CreateBeerRequest) {
  const res = await api.post<BeerDto>(`/beers`, payload)
  return res.data
}

export async function updateBeer(id: string, payload: UpdateBeerRequest) {
  const res = await api.put<BeerDto>(`/beers/${id}`, payload)
  return res.data
}

export async function deleteBeer(id: string) {
  await api.delete(`/beers/${id}`)
}
```

G) Router skeleton (src/app/routes/index.tsx)
```tsx
import { createBrowserRouter } from 'react-router-dom'
import { AppLayout } from '@/app/layout/AppLayout'
import { BeersPage } from '@/pages/beers/BeersPage'
import { BeerDetailsPage } from '@/pages/beers/BeerDetailsPage'
// ... other imports

export const router = createBrowserRouter([
  {
    path: '/',
    element: <AppLayout />,
    children: [
      { index: true, element: <BeersPage /> },
      { path: 'beers', element: <BeersPage /> },
      { path: 'beers/:id', element: <BeerDetailsPage /> },
      // customers, orders...
      { path: '*', element: <div>Not Found</div> },
    ],
  },
])
```

This plan provides a concrete, sequential roadmap with code-ready snippets aligned with the repository’s structure and the requirements document. Developers can pick up each epic and complete the listed tasks to deliver a production-ready frontend integrated with the Spring Boot build. 