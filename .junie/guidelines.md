
# Spring Boot Guidelines

## 1. Prefer Constructor Injection over Field/Setter Injection
* Declare all the mandatory dependencies as `final` fields and inject them through the constructor.
* Spring will auto-detect if there is only one constructor; no need to add `@Autowired` on the constructor.
* Avoid field/setter injection in production code.

**Explanation:**

* Making all the required dependencies `final` and injecting them via the constructor ensures the object is always in a properly initialized state using plain Java, without relying on framework-specific mechanisms.
* You can write unit tests without reflection-based initialization or mocking.
* Constructor-based injection clearly communicates a class’s dependencies.
* Spring Boot provides extension points as builders (for example, `RestClient.Builder`, `ChatClient.Builder`). Using constructor injection, you can customize and initialize the actual dependency.

```java
@Service
public class OrderService {
   private final OrderRepository orderRepository;
   private final RestClient restClient;

   public OrderService(OrderRepository orderRepository, 
                       RestClient.Builder builder) {
       this.orderRepository = orderRepository;
       this.restClient = builder
               .baseUrl("http://catalog-service.com")
               .requestInterceptor(new ClientCredentialTokenInterceptor())
               .build();
   }

   //... methods
}
```

## 2. Prefer package-private over public for Spring components
* Declare Controllers, their request-handling methods, `@Configuration` classes, and `@Bean` methods with default (package-private) visibility whenever possible. There’s no obligation to make everything `public`.

**Explanation:**

* Keeping classes and methods package-private reinforces encapsulation and abstraction by hiding implementation details from the rest of your application.
* Spring Boot’s classpath scanning still detects and invokes package-private components (for example, invoking your `@Bean` methods or controller handlers), so you can safely restrict visibility to only what clients truly need.

## 3. Organize Configuration with Typed Properties
* Group application-specific configuration properties with a common prefix in `application.properties` or `application.yml`.
* Bind them to `@ConfigurationProperties` classes with validation annotations so that the application fails fast if the configuration is invalid.
* Prefer environment variables instead of profiles for passing different configuration properties for different environments.

**Explanation:**

* By grouping and binding configuration in a single `@ConfigurationProperties` bean, you centralize both the property names and their validation rules. In contrast, using `@Value("${…}")` across many components forces you to update each injection point whenever a key or validation requirement changes.
* Overusing profiles to customize application configuration may lead to unexpected issues due to the order of profiles specified. As you can enable multiple profiles with different combinations, understanding the effective configuration becomes tricky.

## 4. Define Clear Transaction Boundaries
* Define each Service-layer method as a transactional unit.
* Annotate query-only methods with `@Transactional(readOnly = true)`.
* Annotate data-modifying methods with `@Transactional`.
* Limit the code inside each transaction to the smallest necessary scope.

**Explanation:**

* **Single Unit of Work:** Group all database operations for a given use case into one atomic unit, typically a method of a `@Service`-annotated class. This ensures that either all operations succeed or none do.
* **Connection Reuse:** A `@Transactional` method runs on a single database connection for its entire scope, avoiding the overhead of repeatedly acquiring/returning connections from the pool.
* **Read-only Optimizations:** Marking methods as `readOnly = true` disables unnecessary dirty-checking and flushes, improving performance for pure reads.
* **Reduced Contention:** Keeping transactions brief minimizes lock duration, lowering the chance of contention in high-traffic applications.

## 5. Disable Open Session in View Pattern
* When using Spring Data JPA, disable the OSIV filter by setting `spring.jpa.open-in-view=false` in `application.properties` or `application.yml`.

**Explanation:**

* The OSIV filter transparently enables loading lazy associations while rendering the view or serializing JPA entities. This may lead to the N + 1 select problem.
* Disabling OSIV forces you to fetch exactly the associations you need via fetch joins, entity graphs, or explicit queries, helping you avoid unexpected N + 1 selects and `LazyInitializationException`s.

## 6. Separate Web Layer from Persistence Layer
* Don’t expose entities directly as responses in controllers.
* Define explicit request and response DTO (record) classes instead.
* Apply Jakarta Validation annotations on your request records to enforce input rules.

**Explanation:**

* Returning or binding directly to entities couples your public API to your database schema, making future changes riskier.
* DTOs let you clearly declare which fields clients can send or receive, improving clarity and security.
* With dedicated DTOs per use case, you can annotate fields for validation without complex validation groups.
* Use bean mapper libraries to simplify DTO conversions. Prefer MapStruct, which generates mappers at compile time (no runtime reflection overhead).

## 7. Follow REST API Design Principles
* **Versioned, resource-oriented URLs:** Structure endpoints as `/api/v{version}/resources` (for example, `/api/v1/orders`).
* **Consistent patterns for collections and sub-resources:** Keep URL conventions uniform (for example, `/posts` for posts collection and `/posts/{slug}/comments` for comments of a specific post).
* **Explicit HTTP status codes via `ResponseEntity`:** Use `ResponseEntity<T>` to return the correct status (for example, 200 OK, 201 Created, 404 Not Found) along with the response body.
* Use pagination for collection resources that may contain an unbounded number of items.
* The JSON payload must use a JSON object as a top-level data structure to allow for future extension.
* Use snake_case or camelCase for JSON property names consistently.

**Explanation:**

* **Predictability and discoverability:** Adhering to well-known REST conventions makes your API intuitive. Clients can guess URLs and behaviors without extensive documentation.
* **Reliable client integrations:** Standardized URL structures, status codes, and headers enable consumers to build against your API with confidence.
* For more comprehensive guidance, see the Zalando RESTful API and Event Guidelines: https://opensource.zalando.com/restful-api-guidelines/

## 8. Use Command Objects for Business Operations
* Create purpose-built command records (for example, `CreateOrderCommand`) to wrap input data.
* Accept these commands in your service methods to drive creation or update workflows.

**Explanation:**

* Using use-case-specific Command and Query objects clearly communicates what input data is expected from the caller. Otherwise, the caller must guess whether to create and pass the unique key or timestamps, or whether they’ll be generated by the server/database.

## 9. Centralize Exception Handling
* Define a global handler class annotated with `@ControllerAdvice` (or `@RestControllerAdvice` for REST APIs) using `@ExceptionHandler` methods to handle specific exceptions.
* Return consistent error responses. Consider using the Problem Details response format (RFC 9457).

**Explanation:**

* Handle all expected exceptions and return a standard error response instead of letting exceptions propagate to the client.
* Centralize exception handling in a `GlobalExceptionHandler` using `(Rest)ControllerAdvice` rather than duplicating `try/catch` across controllers.

## 10. Actuator
* Expose only essential actuator endpoints (such as `/health`, `/info`, `/metrics`) without requiring authentication. Secure all other actuator endpoints.

**Explanation:**

* Endpoints like `/actuator/health` and `/actuator/metrics` are critical for external health checks and metric collection. Allowing anonymous access ensures monitoring tools can function without extra credentials. All remaining endpoints should be secured.
* In non-production environments (DEV, QA), you may expose additional actuator endpoints such as `/actuator/beans` and `/actuator/loggers` for debugging.

## 11. Internationalization with ResourceBundles
* Externalize all user-facing text such as labels, prompts, and messages into ResourceBundles rather than embedding them in code.

**Explanation:**

* Hardcoded strings make it difficult to support multiple languages. By placing your labels, error messages, and other text in locale-specific ResourceBundle files, you can maintain separate translations for each language.
* At runtime, Spring can load the appropriate bundle based on the user’s locale or a preference setting, making it simple to add new languages and switch between them dynamically.

## 12. Use Testcontainers for Integration Tests
* Spin up real services (databases, message brokers, etc.) in your integration tests to mirror production environments.

**Explanation:**

* Modern applications use a wide range of technologies. Instead of using in-memory variants or mocks, Testcontainers can run those dependencies as Docker containers so you test with the same types used in production. This reduces environment inconsistencies and increases confidence in integration tests.
* Always use Docker images with the specific version you use in production, rather than `latest`.

## 13. Use a random port for integration tests
* When writing integration tests, start the application on a random available port to avoid port conflicts:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
```

**Explanation:**

* In CI/CD, multiple builds may run in parallel on the same server/agent. Running on a random available port avoids conflicts.

## 14. Logging
* **Use a proper logging framework.** Never use `System.out.println()` for application logging. Rely on SLF4J (or a compatible abstraction) and your chosen backend (Logback, Log4j2, etc.).
* **Protect sensitive data.** Ensure no credentials, personal information, or other confidential details appear in logs.
* **Guard expensive log calls.** When building verbose messages at `DEBUG` or `TRACE` level, wrap them in a level check or use suppliers:

```java
if (logger.isDebugEnabled()) {
    logger.debug("Detailed state: {}", computeExpensiveDetails());
}

// Using Supplier/Lambda expression
logger.atDebug()
    .setMessage("Detailed state: {}")
    .addArgument(() -> computeExpensiveDetails())
    .log();
```

**Explanation:**

* **Flexible verbosity control:** A logging framework lets you adjust what gets logged and where, with support for tuning log levels per environment.
* **Rich contextual metadata:** Capture class/method names, thread IDs, process IDs, and custom context via MDC.
* **Multiple outputs and formats:** Direct logs to consoles, rolling files, or remote systems, and choose formats like JSON for ingestion into ELK/Loki.
* **Better tooling and analysis:** Structured logs and controlled log levels make it easier to filter noise, automate alerts, and visualize application behavior.

## 15. Flyway Migrations with Spring Boot
* Default location: place versioned SQL scripts on the classpath at `src/main/resources/db/migration`.
* Versioned migration naming: `V<version>__<description>.sql` (double underscore as the separator). Examples:

```
V1__init_schema.sql
V2__add_beer_table.sql
V3_1__add_indexes.sql   // semantic version segments are allowed
```

* Repeatable migrations: use `R__<description>.sql` for scripts that can be re-run when contents change (e.g., views, functions).
* Schema history: Flyway records applied migrations in `flyway_schema_history` to ensure each migration runs exactly once per database.
* Basic configuration (optional): defaults usually work; you can customize in application properties if needed.

```
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
```

* Use H2-compliant SQL syntax for database migrations (if H2 is used locally).
* When altering tables to add a property with a foreign key constraint, add the new column first and then add the foreign key constraint in a second SQL statement.

## 16. OpenAPI Documentation for This Project
This project ships an OpenAPI 3.1 specification under `openapi/openapi/openapi.yaml`. It uses `$ref` file references to keep the spec maintainable.

- Entrypoint: `openapi/openapi/openapi.yaml`
- Tooling: Redocly CLI (declared in `openapi/package.json`)
    - `npm start` previews docs
    - `npm run build` bundles the spec
    - `npm test` lints/validates the spec

### 16.1 Paths and File Naming Conventions
- Paths are defined in separate YAML files under `openapi/openapi/paths`, referenced from `paths:` in `openapi.yaml`.
- Conventions:
    - File-per-path; use underscore `_` to represent `/` in filenames when keeping files flat.
    - Keep path parameters wrapped in `{}` in filenames (for example, `{username}`).
    - `$ref` paths relative to `openapi.yaml` when referenced from the entrypoint.

### 16.2 Components (Schemas, Headers, Responses, Security)
- Reusable components live under `openapi/openapi/components` and are referenced from both the entrypoint and path files.
- Use PascalCase filenames for schemas/headers/responses (for example, `User.yaml`, `Problem.yaml`).

### 16.3 Servers, Tags, and Webhooks (Context)
- Servers are defined in `openapi.yaml` with templated variables.
- Tags are grouped via `tags` and `x-tagGroups`.
- Webhooks can reference components just like standard paths.

### 16.4 Validate and Test the OpenAPI Spec
From the repository root or the `openapi` folder:

```bash
cd openapi
npm ci
npm test       # redocly lint
npm start      # preview docs (optional)
npm run build  # bundle spec (optional)
```

### 16.5 Authoring Checklist
- When adding a new API path, create a matching file under `openapi/openapi/paths` using the underscore `_` separator and curly-braced parameters, and add a `$ref` under `paths:` in `openapi.yaml`.
- Define reusable request/response bodies as schemas under `components/schemas` and reference them via `$ref`.
- Put reusable headers in `components/headers` and common error responses in `components/responses`.
- Keep references relative and verify them with `npm test` before committing.

---

## 17. Contributor Workflow
- See .junie/CONTRIBUTING_WORKFLOW.md for the day-to-day contributor workflow (branching, commit messages, PR checklist, OpenAPI flow, and build verification).

# Frontend Guidelines (React + Vite)

## Frontend Overview and Project Structure
This project embeds a Vite + React + TypeScript app inside the Spring Boot repo.

- Location: `src/main/frontend`
- OpenAPI spec: `openapi/openapi/openapi.yaml` (validated with Redocly CLI)
- Production assets: emitted to `src/main/resources/static` and served by Spring Boot from `classpath:/static`

```
/ (project root)
├─ pom.xml
├─ openapi/
│  └─ openapi/openapi.yaml
├─ src/
│  └─ main/
│     ├─ java/...
│     ├─ resources/
│     │  └─ static/                  # Vite build output (generated)
│     └─ frontend/                   # React app
│        ├─ package.json
│        ├─ vite.config.ts           # dev proxy + prod outDir = ../resources/static
│        ├─ index.html
│        └─ src/
│           ├─ lib/api/              # generated OpenAPI client (`npm run api:gen`)
│           ├─ app/                  # app shell, routes, layout (if present)
│           ├─ components/           # UI components
│           ├─ hooks/                # custom hooks
│           ├─ services/             # axios instance + resource services
│           ├─ styles/               # Tailwind entry (e.g., globals.css)
│           └─ main.tsx
└─ .junie/guidelines.md
```

Guidance:
- Keep `src/main/resources/static` out of version control (generated output).
- Vite `build.outDir` is `../resources/static` in `vite.config.ts`.
- Generated API client lives in `src/main/frontend/src/lib/api`.

## Frontend Build and Run (Developer Workflow)
- Prerequisites:
    - Java 21+ for the Spring Boot backend
    - Node and npm (the Maven `with-frontend` profile provisions specific versions during CI builds)

- Start backend (typical): run the Spring Boot app on `http://localhost:8080`.

- Start frontend (dev server with proxy):

```bash
cd src/main/frontend
npm ci
npm run dev
```

- App runs at `http://localhost:5173`.
- `vite.config.ts` proxies `/api` and `/actuator` to `http://localhost:8080`.

- Generate API client from OpenAPI (when the spec changes):

```bash
cd src/main/frontend
npm run api:gen
```

- Uses `openapi-typescript-codegen` (or configured generator) to emit typed axios clients into `src/lib/api`.

- Production build (standalone):

```bash
cd src/main/frontend
npm run build
```

- Outputs static assets to `src/main/resources/static`.

## Build and Package via Maven
- Build the backend JAR including the compiled frontend in one command:

```bash
mvn -Pwith-frontend clean package
```

What happens:
- `frontend-maven-plugin` installs Node/npm, runs `npm ci`, then `npm run build` in `src/main/frontend`.
- `maven-clean-plugin` removes generated `src/main/resources/static` files on `mvn clean`.

Version note:
- The Maven profile currently pins Node `v22.11.0` and npm `10.9.0`.
- If your `src/main/frontend/package.json` `engines` differ (currently Node `24.11.0` / npm `11.6.2`), the plugin’s versions take precedence during Maven builds. Consider aligning versions to reduce confusion.

## Frontend Testing Guidance
- Test runner: `vitest` with JSDOM environment and `@testing-library/react` + `@testing-library/jest-dom`.
- Commands:

```bash
cd src/main/frontend
npm test         # CI-friendly (runs once)
npm run test:watch
```

- Coverage: V8 coverage enabled; reports under `src/main/frontend/coverage`.
- Type safety: run `npm run typecheck` in CI to enforce TypeScript soundness.

Suggested CI steps:

```bash
cd src/main/frontend
npm ci
npm run lint
npm run typecheck
npm test
```

## Linting, Formatting, and Type Safety
- Linting: `eslint` with React and React Hooks plugins, plus `eslint-config-prettier`.
    - Run: `npm run lint` (configured with `--max-warnings=0`).
- Formatting: `prettier`.
    - Run: `npm run format`.
- Type checking: enable strict TypeScript; run `npm run typecheck` to ensure no type errors.

## OpenAPI and API Client Generation
- Authoring & validation:
    - OpenAPI entrypoint: `openapi/openapi/openapi.yaml`.
    - Using Redocly CLI from the `openapi/` folder:

```bash
cd openapi
npm ci
npm test      # redocly lint
npm start     # preview docs (optional)
npm run build # bundle spec (optional)
```

- Client generation (from the frontend project):

```bash
cd src/main/frontend
npm run api:gen
```

- Consumption:
    - Import generated models and API classes from `src/lib/api`.
    - Wrap them with a shared axios instance for consistent base URL, timeouts, and interceptors.

## Services, Hooks, and Components (Recommended Architecture)
- Axios instance: a single configured instance (base URL, timeouts, error normalization, optional auth interceptors).
- Services per resource: e.g., `services/beers.ts`, `services/customers.ts`, `services/orders.ts` using the generated client.
- Hooks: `hooks/useBeers.ts`, `hooks/useCustomers.ts`, `hooks/useOrders.ts` encapsulate fetching, loading, and error state.
- Components: keep presentational components unaware of data-fetching concerns.
- Routing: React Router with nested routes and error boundaries; prefer lazy-loaded routes for feature pages.

## Styling and UI Libraries
- Tailwind CSS v4: use a single entry (e.g., `src/styles/globals.css`) that contains:

```css
@import "tailwindcss";
```

Import it in `src/main.tsx`.
- Radix UI primitives and `lucide-react` icons are available.
- Shadcn UI is optional; if used, the generator scaffolds components into `src/components` (no runtime dependency).

## Environment, Dev Proxy, and Static Serving
- Dev server: port `5173`, `open: true`.
- Proxies: `/api` and `/actuator` → `http://localhost:8080` in development.
- Production output: `build.outDir = ../resources/static` so Spring Boot serves assets in production.
- Tests: `test.environment = "jsdom"` with `setupFiles = ./vitest.setup.ts` and coverage reporters configured.
- Web Crypto compatibility: ensure `globalThis.crypto` is available during test/build time (see `vite.config.ts`).

## Local Development Recipes
- Run both apps locally:
    1) Start Spring Boot on port 8080.
    2) In another terminal, run `npm run dev` in `src/main/frontend`.
    3) Visit `http://localhost:5173` — API calls proxy to `http://localhost:8080`.

- End-to-end smoke (manual):

```bash
mvn -Pwith-frontend clean package
java -jar target/*SNAPSHOT.jar
# Open http://localhost:8080 and verify the UI loads
```

## Security, Configuration, and Operations Notes
- Prefer environment variables for runtime configuration (aligns with backend typed properties).
- Expose only essential Actuator endpoints publicly (`/health`, `/info`, `/metrics`).
- Avoid exposing secrets in the frontend build; use server-side endpoints for sensitive data.

## Best Practices Evident in the Current Frontend Codebase
- Vite + React + TypeScript with clear scripts: `dev`, `build`, `preview`, `test`, `lint`, `format`, `typecheck`, `api:gen`.
- Generated, typed API clients via `openapi-typescript-codegen` under `src/lib/api`.
- Dev proxy for `/api` and `/actuator` in `vite.config.ts` for a smooth developer experience.
- Production build outputs to `src/main/resources/static`, enabling Spring Boot to serve assets without extra reverse-proxy setup.
- Frontend integrated into Maven via `frontend-maven-plugin` under the `with-frontend` profile, with pinned Node/npm versions for reproducible builds.
- Testing setup using Vitest and JSDOM, plus coverage reporting.
- Linting and formatting with ESLint + Prettier; `--max-warnings=0` keeps quality high in CI.
- Tailwind CSS v4 configured with a single CSS entry.
- React Router v7 and React 19 (modern stack readiness).

Nice-to-have follow-ups:
- Enforce TypeScript strict mode if not already enabled.
- Centralize axios instance with interceptors for error normalization and optional auth.
- Consider React Query for caching and retry logic if data fetching grows complex.
- Use MSW for API mocking in unit/integration tests if useful.

## Quick Reference
- Dev:

```bash
cd src/main/frontend
npm ci
npm run dev
```

- Test:

```bash
cd src/main/frontend
npm test
npm run typecheck
npm run lint
```

- Build (frontend only):

```bash
cd src/main/frontend
npm run build
```

- Build (full app with embedded frontend):

```bash
mvn -Pwith-frontend clean package
```

- Regenerate API client:

```bash
cd src/main/frontend
npm run api:gen
```
