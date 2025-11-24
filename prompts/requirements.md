# Developer Guide: Add a React Frontend to the Spring Boot Project

This guide explains, step by step, how to integrate a modern React + TypeScript frontend into this Spring Boot project so that:
- You can develop the UI with hot reload alongside the running backend.
- A single `mvn -Pwith-frontend clean package` builds a self-contained Spring Boot JAR embedding the compiled frontend under `classpath:/static`.
- The frontend uses generated TypeScript API clients from the project's OpenAPI spec.

The instructions and examples are tailored to this repository:
- Backend: Spring Boot 3.x (Java 21)
- Frontend location: `src/main/frontend`
- OpenAPI spec: `openapi/openapi/openapi.yaml`
- Build integration: Maven profile `with-frontend` + `frontend-maven-plugin`
- Vite + React + TypeScript; Testing with Vitest + React Testing Library

--------------------------------------------------------------------------------

## Part 1 — Foundation and Setup

### 1. Objective and Final State
- Goal: Integrate a React application served by Spring Boot in production, with a smooth DX during development.
- Final artifact: a Spring Boot JAR that includes the compiled frontend assets under `src/main/resources/static`.

### 2. Recommended Project Structure

The repository already follows this structure. If starting fresh, mirror this layout.

```
/ (project root)
├─ pom.xml
├─ openapi/
│  └─ openapi/openapi.yaml
├─ src/
│  └─ main/
│     ├─ java/...
│     ├─ resources/
│     │  └─ static/            # Vite build output (production)
│     └─ frontend/             # React app source (development)
│        ├─ package.json
│        ├─ vite.config.ts
│        ├─ index.html
│        ├─ src/
│        │  ├─ lib/api/        # generated OpenAPI client (npm run api:gen)
│        │  ├─ app/            # app shell: routes, layout
│        │  ├─ components/     # ui components
│        │  ├─ hooks/          # custom hooks
│        │  ├─ services/       # axios instance + service modules
│        │  ├─ styles/         # tailwind entry (e.g., globals.css)
│        │  └─ main.tsx
│        └─ vitest.setup.ts
└─ prompts/
   └─ requirements.md          # this guide
```

Rationale:
- Keeping the frontend under `src/main/frontend` keeps the project single-module and lets the Maven plugin run Node/NPM tasks in place.
- Vite outputs to `../resources/static` so Spring Boot serves the assets in production from `classpath:/static`.

### 3. Initialize React + TypeScript (only if not already present)
If `src/main/frontend` does not exist yet:

```
mkdir -p src/main/frontend
cd src/main/frontend
npm create vite@latest . -- --template react-swc-ts
npm install
```

This project already contains a ready-to-use frontend with Vite and TypeScript. Review these key files:
- `src/main/frontend/vite.config.ts` — dev proxy and production output directory
- `src/main/frontend/package.json` — scripts, testing setup, and API codegen script
- `src/main/frontend/src/main.tsx` — application entry

### 4. Install and Initialize UI & Tooling
The repository already includes most dependencies. If you add new UI libraries/components:

- Tailwind CSS (v4): create a stylesheet (e.g., `src/styles/globals.css`) and include Tailwind at-rules:

```
@import "tailwindcss";
```

Ensure you import this CSS in `src/main.tsx`:

```ts
import './styles/globals.css'
```

- Radix UI and icons are available via `@radix-ui/*` and `lucide-react`.
- Shadcn UI is optional and typically added via its CLI which scaffolds components into your repo. If you choose to use it:

```
# Example (optional):
npx shadcn@latest init
# Then generate specific components
npx shadcn@latest add button input dialog
```

Note: Shadcn adds files under `src/components` and does not add a runtime dependency; it scaffolds source you own.

--------------------------------------------------------------------------------

## Part 2 — Build Integration and Configuration

### 1. Vite Configuration for This Project
The existing `vite.config.ts` is already configured correctly for development and production:

```ts
// src/main/frontend/vite.config.ts (excerpt)
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    open: true,
    proxy: {
      '/api': { target: 'http://localhost:8080', changeOrigin: true, secure: false },
      '/actuator': { target: 'http://localhost:8080', changeOrigin: true, secure: false },
    },
  },
  build: {
    outDir: '../resources/static',
    emptyOutDir: true,
  },
  test: { /* Vitest config */ }
})
```

- Development: Vite proxies `/api` and `/actuator` to `http://localhost:8080` to avoid CORS issues.
- Production: build output goes to `src/main/resources/static`, which Spring Boot serves from `classpath:/static`.

Environment variables:
- Create a `.env` file in `src/main/frontend/` as needed. Only variables prefixed with `VITE_` are exposed to the client, for example:

```
# src/main/frontend/.env
VITE_API_BASE=/api
```

Use in code via `import.meta.env.VITE_API_BASE`.

### 2. Maven Integration (already configured)
The Maven profile `with-frontend` is preconfigured in `pom.xml` to build the frontend during the Maven lifecycle:

```xml
<profile>
  <id>with-frontend</id>
  <build>
    <plugins>
      <plugin>
        <groupId>com.github.eirslett</groupId>
        <artifactId>frontend-maven-plugin</artifactId>
        <version>1.15.0</version>
        <configuration>
          <workingDirectory>${project.basedir}/src/main/frontend</workingDirectory>
        </configuration>
        <executions>
          <execution>
            <id>install-node-and-npm</id>
            <goals>
              <goal>install-node-and-npm</goal>
            </goals>
            <configuration>
              <nodeVersion>v22.11.0</nodeVersion>
              <npmVersion>10.9.0</npmVersion>
            </configuration>
          </execution>
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
      <plugin>
        <artifactId>maven-clean-plugin</artifactId>
        <configuration>
          <filesets>
            <fileset>
              <directory>${project.basedir}/src/main/resources/static</directory>
              <includes><include>**/*</include></includes>
            </fileset>
          </filesets>
        </configuration>
      </plugin>
    </plugins>
  </build>
</profile>
```

Usage:
- Development: run frontend and backend separately (see Part 4).
- Production build: `mvn -Pwith-frontend clean package`
  - This installs Node + npm (isolated per build), runs `npm ci` and `npm run build`, and bundles the resulting assets into the JAR.

--------------------------------------------------------------------------------

## Part 3 — Application Architecture and Implementation

### 1. API Types and Client Generation (OpenAPI)
This repo uses `openapi-typescript-codegen` to generate a typed client and models from the backend OpenAPI spec.

- Script (already present):

```json
{
  "scripts": {
    "api:gen": "openapi -i ../../../openapi/openapi/openapi.yaml -o src/lib/api --client axios --useOptions"
  },
  "devDependencies": {
    "openapi-typescript-codegen": "^0.26.0"
  }
}
```

- Run generation:

```
cd src/main/frontend
npm run api:gen
```

This produces `src/lib/api` with Axios-based client wrappers and TypeScript models. Re-run when the OpenAPI spec changes.

### 2. Axios Instance and Service Layer
Create a reusable Axios instance with base URL and interceptors, then write thin service modules that call the generated client.

Example:

```ts
// src/main/frontend/src/services/http.ts
import axios from 'axios'

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '/api',
  timeout: 15000,
})

api.interceptors.response.use(
  (resp) => resp,
  (error) => {
    // Centralized error handling/logging; rethrow standardized error
    const status = error?.response?.status
    const message = error?.response?.data?.message || error.message
    return Promise.reject({ status, message, cause: error })
  },
)
```

Then wire the generated client to use this Axios instance (supported by the generator's config), or wrap calls:

```ts
// src/main/frontend/src/services/beers.ts
import { api } from './http'
import { BeersService } from '../lib/api'

export async function listBeers(params?: { page?: number; size?: number }) {
  // If the generated client accepts a custom axios instance, pass it; otherwise proxy via axios directly
  const res = await api.get('/api/beers', { params })
  return res.data
}
```

Tip: Prefer the generated `BeersService`/`CustomersService` when possible to keep types in sync with OpenAPI.

### 3. Custom Hooks for Data Fetching and State
Encapsulate loading/error handling and caching-friendly patterns in hooks.

```
// src/main/frontend/src/hooks/useBeers.ts
import { useEffect, useState } from 'react'
import { listBeers } from '../services/beers'

export function useBeers(initial = { page: 0, size: 20 }) {
  const [data, setData] = useState<any[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<null | { status?: number; message?: string }>(null)

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    listBeers(initial)
      .then((d) => !cancelled && setData(d?.content ?? d))
      .catch((e) => !cancelled && setError({ status: e.status, message: e.message }))
      .finally(() => !cancelled && setLoading(false))
    return () => {
      cancelled = true
    }
  }, [initial.page, initial.size])

  return { data, loading, error }
}
```

### 4. Application Shell, Routing, and Layout
Set up React Router and a shared layout for consistent navigation.

```tsx
// src/main/frontend/src/app/AppLayout.tsx
import { Link, Outlet } from 'react-router-dom'

export default function AppLayout() {
  return (
    <div className="min-h-screen flex flex-col">
      <header className="border-b p-4 flex gap-4">
        <Link to="/">Home</Link>
        <Link to="/beers">Beers</Link>
        <Link to="/customers">Customers</Link>
        <Link to="/orders">Orders</Link>
      </header>
      <main className="p-6 container mx-auto">
        <Outlet />
      </main>
    </div>
  )
}
```

```tsx
// src/main/frontend/src/app/router.tsx
import { createBrowserRouter } from 'react-router-dom'
import AppLayout from './AppLayout'
import Home from '../pages/Home'
import BeersPage from '../pages/BeersPage'

export const router = createBrowserRouter([
  {
    path: '/',
    element: <AppLayout />,
    children: [
      { index: true, element: <Home /> },
      { path: 'beers', element: <BeersPage /> },
      // customers, orders ...
    ],
  },
])
```

```tsx
// src/main/frontend/src/main.tsx
import React from 'react'
import ReactDOM from 'react-dom/client'
import { RouterProvider } from 'react-router-dom'
import { router } from './app/router'
import './styles/globals.css'

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <RouterProvider router={router} />
  </React.StrictMode>,
)
```

### 5. CRUD Features per Resource
Using pages/components and the service/hooks from above, implement CRUD for the key domain resources exposed by the backend (consult the controllers and `openapi/openapi/openapi.yaml`). Typical patterns:

- Beers
  - List with pagination and search
  - View details
  - Create/Update/Delete (forms + dialogs)
- Customers
  - List, detail, create, update, delete
- Beer Orders
  - List, detail, create, update

Use Radix and Shadcn components (if added) for accessible dialogs, forms, and toasts.

### 6. Error Handling and UX
- Centralize HTTP error handling in `api.interceptors.response`.
- Show user-friendly messages (e.g., toast via `@radix-ui/react-toast`).
- For validation errors from backend, map field errors to form inputs.
- Ensure empty states, loading states, and retry actions are present.

### 7. Code Quality: ESLint and Prettier
This repo already includes ESLint and Prettier. Common scripts:

```
cd src/main/frontend
npm run lint
npm run format
npm run typecheck
```

Keep logging restricted to development and avoid printing sensitive data. Use the testing section below to validate components and services.

--------------------------------------------------------------------------------

## Part 4 — Running, Testing, and Building

### 1. Development Workflow
Run backend and frontend in parallel:

- Terminal A (Spring Boot):

```
./mvnw spring-boot:run
```

- Terminal B (Vite dev server):

```
cd src/main/frontend
npm install   # first time
npm run dev   # opens http://localhost:5173
```

API calls from the frontend to `/api` and `/actuator` are proxied to `http://localhost:8080` per `vite.config.ts`.

### 2. Testing (Vitest + React Testing Library)
Testing is configured with Vitest and JSDOM.

- Write a component test:

```tsx
// src/main/frontend/src/components/Hello.test.tsx
import { render, screen } from '@testing-library/react'
import '@testing-library/jest-dom'

function Hello({ name }: { name: string }) {
  return <div>Hello {name}</div>
}

test('renders greeting', () => {
  render(<Hello name="Junie" />)
  expect(screen.getByText('Hello Junie')).toBeInTheDocument()
})
```

- Run tests:

```
cd src/main/frontend
npm test
```

Coverage reporting is enabled via Vitest config in `vite.config.ts`.

### 3. Production Build and Run
- Build everything (backend + frontend) into one JAR:

```
./mvnw -Pwith-frontend clean package
```

- Run the JAR:

```
java -jar target/juniemvc-0.0.1-SNAPSHOT.jar
```

- Access the app at `http://localhost:8080/`.
  - Static assets are served from `classpath:/static` (i.e., `src/main/resources/static`).

--------------------------------------------------------------------------------

## Part 5 — Conventions and Guidance (Project-Specific)

- Do NOT implement authentication for now; API endpoints are public in this project.
- Follow the repository’s Spring Boot guidelines for the backend:
  - Constructor injection for services
  - Package-private visibility for controllers/beans where appropriate
  - Clear transaction boundaries
  - Avoid OSIV; design queries explicitly
  - Separate DTOs from entities; validate request DTOs
  - RESTful paths, explicit `ResponseEntity` status codes, pagination for collections
  - Centralized exception handling with `@RestControllerAdvice`
- Frontend state management: prefer local component state and custom hooks; consider a lightweight cache (e.g., React Query) if needed later.
- Keep the API layer the single source of truth for endpoint URLs and models; regenerate clients when the OpenAPI spec changes.

--------------------------------------------------------------------------------

## Part 6 — Updating Team Documentation

Add the following to `.junie/guidelines.md` (or update an existing section) to reflect the new frontend:

```
### Frontend (React + Vite)

- Location: src/main/frontend
- Dev server: npm run dev (http://localhost:5173)
- API proxy: /api and /actuator → http://localhost:8080
- OpenAPI types & client: npm run api:gen (reads openapi/openapi/openapi.yaml)
- Lint/Format/Typecheck: npm run lint | npm run format | npm run typecheck
- Tests: npm test (Vitest)

### Build Integration

- Development: run backend and frontend separately (see commands above)
- Production build: mvn -Pwith-frontend clean package
  - Bundles compiled frontend into classpath:/static
```

--------------------------------------------------------------------------------

## Quick Reference Commands

- Start backend: `./mvnw spring-boot:run`
- Start frontend: `cd src/main/frontend && npm run dev`
- Generate API client: `cd src/main/frontend && npm run api:gen`
- Lint/Format/Typecheck: `npm run lint` | `npm run format` | `npm run typecheck`
- Run tests: `npm test`
- Package full app: `./mvnw -Pwith-frontend clean package`

--------------------------------------------------------------------------------

## Notes & Caveats

- Node/NPM versions used by the Maven plugin are pinned (Node v24.11.0 npm 11.6.2) for reproducible builds. Your local Node can be newer as long as the dev server runs, but the CI build uses the pinned versions.
- Ensure the OpenAPI spec remains the single source of truth; regenerate frontend clients after backend changes to keep types in sync.
- Spring Boot serves static content from `classpath:/static` at the root (`/`). A file at `src/main/resources/static/js/app.js` is available at `http://localhost:8080/js/app.js` in production.
