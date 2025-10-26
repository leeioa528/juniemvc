# Junie Project Guidelines

Purpose: Give new developers a fast, practical start. Keep PRs small and tests green.

## 1) Overview & Tech Stack
- Spring Boot 3.4.x (Java 21)
- Build: Maven (wrapper included: `./mvnw`)
- Modules/Starters: Web, Validation, Data JPA
- DB: H2 (runtime). Flyway is configured; add migrations as needed
- Mapping/boilerplate: MapStruct, Lombok

## 2) Project Layout (current)
- `src/main/java/guru/springframework/juniemvc`
  - `controllers/` – REST controllers (e.g., `BeerController`)
  - `services/` – interfaces + implementations (`BeerService`, `BeerServiceImpl`)
  - `repositories/` – Spring Data JPA repositories
  - `entities/` – JPA entities (`Beer`)
  - `JuniemvcApplication.java` – Spring Boot entry point
- `src/main/resources/`
  - `application.properties` – app config
  - `db/migration/` – place Flyway scripts (create if missing)
- `src/test/java/...` – unit and slice/integration tests
- `pom.xml` – dependencies and build plugins

Recommended structure for new code:
- Keep layered structure (`controller → service → repository`).
- Group by feature when it grows (e.g., `beer/{controller,service,repo,domain}`) to reduce coupling.

## 3) How to Run
- Run app (dev): `./mvnw spring-boot:run`
- Build jar: `./mvnw clean package`
- Run jar: `java -jar target/juniemvc-0.0.1-SNAPSHOT.jar`

Profiles & config:
- Default profile uses H2; override via `application-<profile>.properties` if needed.

## 4) Testing
- Run all tests: `./mvnw test`
- Run a single test class: `./mvnw -Dtest=BeerControllerTest test`
- Run a single test method: `./mvnw -Dtest=BeerControllerTest#methodName test`
- Generate surefire reports in `target/surefire-reports/`

Testing guidelines:
- Prefer JUnit 5; keep tests fast and isolated.
- Controller tests: Mock MVC or WebMvcTest.
- Service tests: mock repositories.
- Repository tests: use H2 with minimal seed data.

## 5) Database & Migrations (Flyway)
- Create migration scripts under `src/main/resources/db/migration` using versioned files: `V1__init.sql`, `V2__add_table.sql`, ...
- Write idempotent, forward-only migrations. Do not edit past migrations—add new ones.

## 6) MapStruct & Lombok
- MapStruct default component model is `spring` (mappers are Spring beans).
- Put mapper interfaces under the feature area (e.g., `beer/mappers`).
- With Lombok, avoid manual getters/setters; prefer `@Value` for immutables, `@Builder` for complex creation.

## 7) Coding Practices
- Null-safety: use validation (`jakarta.validation`) and Optional sparingly at boundaries.
- Controller contracts: validate input DTOs with `@Valid` and return meaningful HTTP status codes.
- Transactions: put them in the service layer (`@Transactional`).
- Logging: use `@Slf4j`; no `System.out.println`.
- Exceptions: map to problem responses (e.g., `@ControllerAdvice`) when needed.

## 8) Git & Reviews
- Branches: `feature/<short-desc>`, `bugfix/<short-desc>`.
- Commits: small, imperative message, include context if touching DB or contracts.
- PRs: include tests, keep under ~300 lines changed when possible.

## 9) Useful Maven commands
- Format (if configured): `./mvnw spotless:apply` (add plugin if team adopts)
- Dependency tree: `./mvnw dependency:tree`
- Skip tests (rare): `./mvnw -DskipTests package`

## 10) Troubleshooting
- Port busy: change `server.port` in `application.properties`.
- Failing DB tests: ensure H2 on classpath, clean `target/` and re-run.
- MapStruct errors: run `./mvnw clean compile` to see annotation processor logs.

Questions? Ask in the PR or open an issue with steps to reproduce.