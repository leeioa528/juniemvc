# React Frontend Developer Guide for the Spring Boot Project

This guide walks an experienced Java developer through adding a modern React + TypeScript frontend to this Spring Boot application and integrating it into the Maven build so a single JAR serves the UI. It is tailored to this repository (APIs described in `openapi/openapi/openapi.yaml`).

Follow the steps in order. Each step is actionable, with commands and code snippets you can paste.

---

## Part 1: Foundation and Setup

### 1. Objective and Final State
- Integrate a React application into this Spring Boot project.
- Develop with Vite dev server (proxy to Spring Boot).
- Build with Maven to produce one Spring Boot JAR that serves the compiled UI from `src/main/resources/static`.

### 2. Recommended Project Structure
We colocate frontend sources under `src/main/frontend` to keep the Maven module self‑contained and make `frontend-maven-plugin` paths simple.

Final structure (key paths only):
```
/ (project root)
├─ pom.xml
├─ openapi/
│  └─ openapi/openapi.yaml
├─ src/
│  ├─ main/
│  │  ├─ java/ ... (Spring Boot)
│  │  ├─ resources/
│  │  │  └─ static/            # Vite production build output (served by Spring Boot)
│  │  └─ frontend/             # React app sources (Vite project root)
│  │     ├─ index.html
│  │     ├─ vite.config.ts
│  │     ├─ package.json
│  │     ├─ tsconfig.json
│  │     ├─ src/
│  │     │  ├─ main.tsx
│  │     │  ├─ app/
│  │     │  │  ├─ routes/
│  │     │  │  └─ layout/
│  │     │  ├─ components/
│  │     │  ├─ hooks/
│  │     │  ├─ lib/
│  │     │  │  ├─ api/         # OpenAPI generated types and API client
│  │     │  │  └─ axios.ts     # Axios instance
│  │     │  ├─ pages/
│  │     │  └─ styles/
│  │     └─ .eslintrc.cjs, .prettierrc, etc.
│  └─ test/ ... (Java tests)
```

Why `src/main/frontend`?
- Keeps the frontend under Maven’s default module, so `frontend-maven-plugin` runs in the same project.
- Clear separation from `resources/static` (build output only, no hand‑written files).

### 3. Initialize the React + TypeScript app
From project root:
```bash
cd src/main
mkdir -p frontend
cd frontend
npm create vite@latest . -- --template react-swc-ts
npm pkg set name="juniemvc-frontend"
```
Key files Vite creates:
- `index.html` – dev entry HTML (Vite injects scripts in dev; copied to build output in prod)
- `src/main.tsx` – application bootstrap
- `vite.config.ts` – Vite config (we will customize)
- `package.json` – scripts and dependencies

### 4. Install UI and tooling dependencies
Install the stack specified for this project:
```bash
# Core React + Router
npm i react@19.1.0 react-dom@19.1.0 react-router-dom@7.6.36
npm i -D @types/react@19.1.0 @types/react-dom@19.1.0

# Styling: Tailwind CSS v4, shadcn, Radix, icons, helpers
npm i -D tailwindcss@4.1.10 postcss@8.5.5 autoprefixer@10.4.20
npm i class-variance-authority@0.7.1 clsx@2.1.1 tailwind-merge@3.3.1
npm i lucide-react@0.515.0
# Radix primitives (install as needed per component usage)
npm i @radix-ui/react-slot @radix-ui/react-dialog @radix-ui/react-toast
# Optional helper animations for Tailwind
npm i -D tw-animate-css@1.3.4

# HTTP
npm i axios@1.10.0

# TypeScript + Node types (align with Node 22)
npm i -D typescript@5.8.3 @types/node@24.0.1

# Vite + React plugin
npm i -D vite@6.3.5 @vitejs/plugin-react@4.5.2

# Linting/Formatting
npm i -D eslint prettier eslint-config-prettier eslint-plugin-react eslint-plugin-react-hooks @typescript-eslint/parser @typescript-eslint/eslint-plugin

# Testing (Vitest is recommended with Vite)
npm i -D vitest @vitest/ui @vitest/coverage-v8 jsdom @testing-library/react @testing-library/user-event @testing-library/jest-dom

# OpenAPI type/code generation
npm i -D openapi-typescript-codegen
```

Initialize Tailwind CSS v4 (file‑less by default):
- Create `src/styles/index.css` and include Tailwind layers:
```css
@import "tailwindcss";
@plugin "tw-animate-css"; /* optional */
```
- Import the styles in `src/main.tsx`:
```ts
import "./styles/index.css";
```

Initialize shadcn UI:
```bash
npx shadcn-ui@latest init
# Answer prompts to use Tailwind 4 and your chosen component paths (e.g., src/components/ui)
```
Add components when needed:
```bash
npx shadcn-ui@latest add button card input table dialog toast
```


---

## Part 2: Build Integration and Configuration

### 5. Vite configuration for Spring Boot
Create `vite.config.ts` (or update existing) under `src/main/frontend`:
```ts
import { defineConfig, loadEnv } from "vite";
import react from "@vitejs/plugin-react";
import path from "node:path";

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), "");
  return {
    plugins: [react()],
    base: "/", // app served from root in production
    root: ".", // Vite project root
    server: {
      port: 3000,
      strictPort: false,
      open: true,
      proxy: {
        // Proxy API calls to Spring Boot during dev, avoiding CORS
        "/api": {
          target: env.VITE_BACKEND_URL || "http://localhost:8080",
          changeOrigin: true,
          secure: false,
        },
      },
    },
    resolve: {
      alias: {
        "@": path.resolve(__dirname, "./src"),
      },
    },
    build: {
      outDir: path.resolve(__dirname, "../resources/static"), // Spring Boot serves from here
      emptyOutDir: true,
      sourcemap: true,
    },
    preview: {
      port: 5174,
    },
    define: {
      __APP_VERSION__: JSON.stringify(process.env.npm_package_version),
    },
  };
});
```
Notes:
- API base path per OpenAPI is `/api/v1` + resource paths. With the proxy above, calls to `/api/...` (e.g. `/api/v1/beers`) will be forwarded to `http://localhost:8080` in dev.
- In production, built assets go into `src/main/resources/static` and are served directly by Spring Boot.

Environment variables:
- Create `.env` files in `src/main/frontend` (Vite will load them). Only variables prefixed with `VITE_` are exposed to the client.
```
# .env.development
VITE_BACKEND_URL=http://localhost:8080
```
Access in code: `import.meta.env.VITE_BACKEND_URL`.

### 6. Maven integration via frontend-maven-plugin
Add the plugin to your `pom.xml` (project root). Bind install/build phases, and point to the frontend working directory.
```xml
<build>
  <plugins>
    <plugin>
      <groupId>com.github.eirslett</groupId>
      <artifactId>frontend-maven-plugin</artifactId>
      <version>1.15.1</version>
      <configuration>
        <workingDirectory>src/main/frontend</workingDirectory>
        <environmentVariables>
          <CI>false</CI>
        </environmentVariables>
      </configuration>
      <executions>
        <execution>
          <id>install-frontend</id>
          <phase>generate-resources</phase>
          <goals>
            <goal>install-node-and-npm</goal>
            <goal>npm</goal>
          </goals>
          <configuration>
            <nodeVersion>v22.16.0</nodeVersion>
            <npmVersion>11.4.0</npmVersion>
            <arguments>ci</arguments> <!-- npm ci -->
          </configuration>
        </execution>
        <execution>
          <id>build-frontend</id>
          <phase>generate-resources</phase>
          <goals>
            <goal>npm</goal>
          </goals>
          <configuration>
            <arguments>run build</arguments>
          </configuration>
        </execution>
      </executions>
    </plugin>

    <!-- Ensure Maven clean removes generated static assets -->
    <plugin>
      <artifactId>maven-clean-plugin</artifactId>
      <version>3.3.2</version>
      <configuration>
        <filesets>
          <fileset>
            <directory>src/main/resources/static</directory>
            <includes>
              <include>**/*</include>
            </includes>
          </fileset>
        </filesets>
      </configuration>
    </plugin>
  </plugins>
</build>
```
Guidance:
- Keep `src/main/resources/static` free of hand‑written files; it is a build output folder.
- To skip the frontend build (e.g., in backend‑only CI jobs), use `-Dskip.npm=true` and set up an activation property in the plugin configuration if desired.

### 7. package.json scripts and TypeScript config
Add useful scripts to `src/main/frontend/package.json`:
```json
{
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "preview": "vite preview",
    "test": "vitest run --reporter=default",
    "test:ui": "vitest --ui",
    "lint": "eslint src --ext .ts,.tsx --max-warnings 0",
    "format": "prettier --write \"**/*.{ts,tsx,js,jsx,json,md,css}\"",
    "gen:api": "openapi -i ../../openapi/openapi/openapi.yaml -o src/lib/api --client axios --useUnionTypes true"
  }
}
```
Sample `tsconfig.json` tweaks for path alias:
```json
{
  "compilerOptions": {
    "baseUrl": ".",
    "paths": {
      "@/*": ["src/*"]
    }
  }
}
```

---

## Part 3: API Types and Client

### 8. Generate TypeScript API client from OpenAPI
Use `openapi-typescript-codegen` to generate a typed client conforming to `openapi/openapi/openapi.yaml`.

- One‑time setup already installed the package. Generate types:
```bash
cd src/main/frontend
npm run gen:api
```
- This will create `src/lib/api` with files like `models/*`, `services/*`, and an `OpenAPI` config.

Configure API base at runtime:
```ts
// src/lib/api/config.ts
import { OpenAPI } from "./api"; // adjust import to generated path

OpenAPI.BASE = "/api/v1"; // base path per server config in OpenAPI
// Optionally attach a request hook to use our Axios instance if you prefer
```

Alternatively, skip the generated HTTP client and use only generated types, then call the REST endpoints via your own Axios instance (see next step).

---

## Part 4: Application Architecture

### 9. Axios instance and error handling
Create a reusable Axios instance configured for dev proxy and prod paths.
```ts
// src/lib/axios.ts
import axios from "axios";

export const api = axios.create({
  baseURL: "/api/v1", // works with Vite proxy in dev and Spring Boot in prod
  headers: { "Content-Type": "application/json" }
});

// Response/error interceptors for consistent error objects
api.interceptors.response.use(
  (res) => res,
  (error) => {
    // Normalize backend ProblemDetails (RFC 9457) if used, or default error shape
    const problem = error?.response?.data ?? {
      title: "Request failed",
      status: error?.response?.status ?? 0,
      detail: error?.message ?? "Unknown error"
    };
    return Promise.reject(problem);
  }
);
```

### 10. Service layer: Beer, Customer, Order
Use either the generated client or hand‑written services with Axios. Below is a hand‑written example using the OpenAPI paths:

```ts
// src/services/beerService.ts
import { api } from "@/lib/axios";

export interface Beer {
  id: string;
  beerName: string;
  beerStyle?: string;
  upc?: string;
  price?: number;
  quantityOnHand?: number;
  description?: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number; // page index
}

export const BeerService = {
  list: (page = 0, size = 20, beerName?: string) =>
    api.get<Page<Beer>>("/beers", { params: { page, size, beerName } }).then(r => r.data),
  get: (id: string) => api.get<Beer>(`/beers/${id}`).then(r => r.data),
  create: (payload: Omit<Beer, "id">) => api.post<Beer>("/beers", payload).then(r => r.data),
  update: (id: string, payload: Partial<Beer>) => api.patch<Beer>(`/beers/${id}`, payload).then(r => r.data),
  remove: (id: string) => api.delete<void>(`/beers/${id}`).then(r => r.data)
};
```
Create similar services for Customers and Orders according to `openapi.yaml` (`/customers`, `/orders`). If pagination schemas exist (see `components/schemas/*Page.yaml`), mirror fields in the `Page<T>` interface.

### 11. Custom hooks for state management
Encapsulate async logic, loading, and errors:
```ts
// src/hooks/useBeers.ts
import { useEffect, useState } from "react";
import { Beer, BeerService, Page } from "@/services/beerService";

export function useBeers(initialPage = 0, initialSize = 20, beerName?: string) {
  const [data, setData] = useState<Page<Beer> | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<any>(null);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    BeerService.list(initialPage, initialSize, beerName)
      .then((res) => { if (!cancelled) setData(res); })
      .catch((err) => { if (!cancelled) setError(err); })
      .finally(() => { if (!cancelled) setLoading(false); });
    return () => { cancelled = true; };
  }, [initialPage, initialSize, beerName]);

  return { data, loading, error };
}
```

### 12. Routing and Layout
Set up a standard app shell with React Router v7 and shadcn UI components:
```tsx
// src/app/layout/AppLayout.tsx
import { Outlet, NavLink } from "react-router-dom";

export default function AppLayout() {
  return (
    <div className="min-h-screen flex flex-col">
      <header className="border-b">
        <nav className="container mx-auto flex gap-4 py-3">
          <NavLink to="/" className={({isActive}) => isActive ? "font-semibold" : ""}>Home</NavLink>
          <NavLink to="/beers">Beers</NavLink>
          <NavLink to="/customers">Customers</NavLink>
          <NavLink to="/orders">Orders</NavLink>
        </nav>
      </header>
      <main className="container mx-auto flex-1 py-6">
        <Outlet />
      </main>
      <footer className="border-t text-sm text-muted-foreground py-4 text-center">© Beer Co</footer>
    </div>
  );
}
```

```tsx
// src/main.tsx
import React from "react";
import ReactDOM from "react-dom/client";
import { createBrowserRouter, RouterProvider } from "react-router-dom";
import AppLayout from "@/app/layout/AppLayout";
import Home from "@/pages/Home";
import BeersPage from "@/pages/BeersPage";

import "./styles/index.css";

const router = createBrowserRouter([
  {
    path: "/",
    element: <AppLayout />,
    children: [
      { index: true, element: <Home /> },
      { path: "beers", element: <BeersPage /> },
      // add customers, orders pages
    ]
  }
]);

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <RouterProvider router={router} />
  </React.StrictMode>
);
```

Example page consuming the hook and service with error states:
```tsx
// src/pages/BeersPage.tsx
import { useBeers } from "@/hooks/useBeers";

export default function BeersPage() {
  const { data, loading, error } = useBeers(0, 20);

  if (loading) return <p>Loading…</p>;
  if (error) return <p className="text-red-600">{error.title ?? "Error"}: {error.detail}</p>;

  return (
    <div>
      <h1 className="text-2xl font-semibold mb-4">Beers</h1>
      <ul className="space-y-2">
        {data?.content.map(b => (
          <li key={b.id} className="border rounded p-3">
            <div className="font-medium">{b.beerName}</div>
            <div className="text-sm text-muted-foreground">{b.beerStyle}</div>
          </li>
        ))}
      </ul>
    </div>
  );
}
```

### 13. CRUD patterns to implement
Use the service layer with forms and dialogs. For each resource (`Beer`, `Customer`, `Order`):
- List (paginated): GET `/api/v1/{resource}` with `page`, `size`, and optional filters.
- Detail: GET `/api/v1/{resource}/{id}`.
- Create: POST with request DTO (see corresponding schema under `openapi/openapi/components/schemas`).
- Update/patch: PATCH `/api/v1/{resource}/{id}` with partial body (see `BeerPatchRequest.yaml`).
- Delete: DELETE `/api/v1/{resource}/{id}`.

Follow the JSON shape in OpenAPI components:
- Beers: `components/schemas/Beer.yaml`, pages: `BeerPage.yaml`.
- Customers: `CustomerRequest.yaml`, `CustomerResponse.yaml`, page: `CustomerPage.yaml`.
- Orders: `BeerOrder.yaml`, `BeerOrderPage.yaml`, shipments endpoints at `/api/v1/beer-orders/{orderId}/shipments`.

Use shadcn UI components (`Dialog`, `Form`, `Button`, `Input`, `Table`) for create/edit flows and table listing. Apply client‑side validation with HTML5 attributes, and if needed, add `zod` for schema validation.

---

## Part 5: Quality: Linting, Formatting, Testing

### 14. ESLint and Prettier configs
`.eslintrc.cjs`:
```js
module.exports = {
  root: true,
  parser: "@typescript-eslint/parser",
  plugins: ["@typescript-eslint", "react", "react-hooks"],
  extends: [
    "eslint:recommended",
    "plugin:@typescript-eslint/recommended",
    "plugin:react/recommended",
    "plugin:react-hooks/recommended",
    "prettier"
  ],
  settings: { react: { version: "detect" } },
  env: { browser: true, es2023: true, node: true, jest: false },
};
```
`.prettierrc`:
```json
{
  "singleQuote": true,
  "trailingComma": "all",
  "printWidth": 100
}
```

### 15. Testing with Vitest + RTL
`vitest.config.ts` (optional; Vite config usually suffices):
```ts
import { defineConfig } from "vitest/config";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  test: {
    environment: "jsdom",
    setupFiles: "./src/test/setup.ts",
    globals: true,
    css: true
  }
});
```
`src/test/setup.ts`:
```ts
import '@testing-library/jest-dom';
```
Example component test:
```tsx
// src/pages/BeersPage.test.tsx
import { render, screen } from '@testing-library/react';
import BeersPage from './BeersPage';

test('renders Beers heading', () => {
  render(<BeersPage />);
  expect(screen.getByText(/Beers/i)).toBeInTheDocument();
});
```
Run tests:
```bash
npm test
```

---

## Part 6: Developer Workflow

### 16. Local development
Run backend and frontend dev server concurrently:
- Terminal A (Spring Boot):
```bash
./mvnw spring-boot:run
```
- Terminal B (frontend):
```bash
cd src/main/frontend
npm ci
npm run dev
```
Visit `http://localhost:5173`. API requests to `/api/v1/...` will proxy to `http://localhost:8080`.

### 17. Production build and run
- Single command build (backend + UI):
```bash
./mvnw clean package
```
This runs `npm ci` and `npm run build`, placing assets in `src/main/resources/static/`, then packages the JAR.

- Start the application:
```bash
java -jar target/*-SNAPSHOT.jar
```
Open `http://localhost:8080/` to view the app served by Spring Boot.

---

## Part 7: Error Handling and UX

### 18. Graceful API error handling
- Use Axios interceptor to normalize error responses.
- Display ProblemDetails fields if backend uses RFC 9457 (`title`, `status`, `detail`).
- Provide toast notifications (shadcn `useToast`) for transient failures and inline messages for form errors.

### 19. Accessibility and i18n readiness
- Radix + shadcn components are accessible by default; keep labels and aria attributes up‑to‑date.
- Externalize user‑facing strings early if internationalization is planned later.

---

## Part 8: Updating Project Guidelines

Provide the following addition to `.junie/guidelines.md`:

```
### Frontend (React + Vite) Guidelines

- Source location: src/main/frontend
- Dev server: npm run dev (Vite @ http://localhost:5173), proxies /api → http://localhost:8080
- Build output: src/main/resources/static (served by Spring Boot)
- Build with Maven: ./mvnw clean package (runs npm ci + npm run build)
- Run app: java -jar target/*.jar then open http://localhost:8080/
- Generate API types from OpenAPI: (from src/main/frontend) npm run gen:api
- Lint/Format: npm run lint / npm run format
- Test: npm test (Vitest + RTL)
```

---

## Appendix: Notes and Alternatives

- Why Vitest over Jest? Vite integrates natively with Vitest, resulting in significantly faster and simpler configuration. If your team prefers Jest, you can swap Vitest with Jest, but additional setup (babel/ts‑jest, JSDOM env) is required.
- Tailwind v4 uses a file‑less config by default; if you need customizations, create `tailwind.config.ts`.
- OpenAPI client: you may choose to use the generated HTTP client or just the generated types with your custom Axios service.

---

## Checklist
- [ ] Created Vite React TS app under `src/main/frontend`.
- [ ] Installed dependencies (React, Router, Tailwind, shadcn, Axios, Vitest, ESLint/Prettier, OpenAPI generator).
- [ ] Configured Vite (dev proxy, build to `resources/static`).
- [ ] Added `frontend-maven-plugin` and `maven-clean-plugin` in `pom.xml`.
- [ ] Implemented Axios instance, services, and hooks for Beers/Customers/Orders.
- [ ] Built routes and pages with a common layout.
- [ ] Added lint/format scripts and test setup.
- [ ] Verified dev (`npm run dev`) and prod (`mvn package`, `java -jar`) flows.
