# Assignment 4 – Implementation Plan

This plan translates the requirements in `prompts/assignment4/requirements.md` into an actionable, incremental delivery plan aligned with the provided Spring Boot Guidelines. It is organized into phases with clear deliverables, acceptance criteria, and cross‑cutting concerns.

## Objectives
1. Introduce a new JPA entity `BeerOrderShipment` and establish a `OneToMany` relationship from `BeerOrder` to `BeerOrderShipment`.
2. Add Flyway migrations to persist the new model.
3. Implement full CRUD for `BeerOrderShipment` with DTOs, MapStruct mappers, repository, service, and REST controller.
4. Provide comprehensive test coverage (unit + integration) and update OpenAPI documentation.
5. Ensure the implementation adheres to the project’s Spring Boot guidelines (constructor injection, DTO separation, transactions, exception handling, etc.).

---

## Phase 1 – Domain & Persistence Model
1. Create `BeerOrderShipment` JPA entity
   - Fields (non-null):
     - `shipmentDate` (LocalDate or OffsetDateTime based on business need; default LocalDate unless timezone is required)
     - `carrier` (String)
     - `carrierNumber` (String) – rename of "carrier number" to Java-friendly `carrierNumber`.
   - Primary key: `UUID id` (consistent with project style if `BeerOrder` uses UUID; otherwise match existing strategy).
   - Auditing: mirror existing base entity conventions in the project (e.g., `createdDate`, `lastModifiedDate`) if present.
   - Constraints: JSR-380 annotations on entity fields (`@NotNull`, `@Size` as needed), leaving DTO validation primary.

2. Relationship from `BeerOrder` to `BeerOrderShipment`
   - In `BeerOrder` entity: add `@OneToMany(mappedBy = "beerOrder", cascade = CascadeType.ALL, orphanRemoval = true)` collection `Set<BeerOrderShipment> shipments`.
   - In `BeerOrderShipment`: add `@ManyToOne(optional = false)` to `BeerOrder` with `@JoinColumn(name = "beer_order_id", nullable = false)`.
   - Ensure equals/hashCode are identifier-based to avoid persistence issues (follow project conventions).

3. Flyway migration
   - Create versioned SQL in `src/main/resources/db/migration`:
     - `Vxx__add_beer_order_shipment.sql` that:
       - Creates `beer_order_shipment` table with columns: `id`, `beer_order_id` (FK), `shipment_date`, `carrier`, `carrier_number`, standard auditing columns if used, plus constraints and indexes.
       - Adds foreign key constraint after column creation (per Guidelines §15), e.g., separate `ALTER TABLE ... ADD CONSTRAINT ... FOREIGN KEY ...`.
       - Adds an index on `beer_order_id`.
   - Verify naming aligns with existing tables (`snake_case`).

Deliverables:
- `BeerOrderShipment` entity class
- Updated `BeerOrder` mapping
- Flyway SQL migration file

Acceptance criteria:
- Application starts and Flyway applies migration successfully on a clean DB.
- JPA schema matches the migration (no hbm2ddl conflicts if enabled).

---

## Phase 2 – DTOs and Mapping
1. Define DTO records under `.../dto/` (match project package structure):
   - `BeerOrderShipmentRequest` (for create/update):
     - `UUID beerOrderId` (required for create unless using nested create via order)
     - `LocalDate shipmentDate`
     - `String carrier`
     - `String carrierNumber`
     - Add Jakarta validation annotations: `@NotNull`, `@Size(max=...)` as appropriate.
   - `BeerOrderShipmentResponse` (for reads):
     - `UUID id`, `UUID beerOrderId`, `LocalDate shipmentDate`, `String carrier`, `String carrierNumber`, audit fields if exposed.

2. MapStruct mapper
   - `BeerOrderShipmentMapper`:
     - `toEntity(BeerOrderShipmentRequest req, BeerOrder beerOrder)`
     - `toResponse(BeerOrderShipment entity)`
     - `updateEntity(@MappingTarget BeerOrderShipment entity, BeerOrderShipmentRequest req)`
   - Configure component model per project (e.g., `componentModel = "spring"`).

Deliverables:
- DTOs with validation
- Mapper interface and generated implementation via MapStruct

Acceptance criteria:
- Mapper compiles; basic unit tests cover essential mappings.

---

## Phase 3 – Repository Layer
1. Spring Data repository
   - `BeerOrderShipmentRepository extends JpaRepository<BeerOrderShipment, UUID>`
   - Query helpers as needed: `List<BeerOrderShipment> findByBeerOrderId(UUID beerOrderId)`

Deliverables:
- Repository interface

Acceptance criteria:
- Repository bean loads; basic CRUD verified in a small repository test (optional if covered by service/integration tests).

---

## Phase 4 – Service Layer
1. Service API (package-private where possible)
   - `BeerOrderShipmentService` with methods:
     - `BeerOrderShipmentResponse create(BeerOrderShipmentRequest request)`
     - `BeerOrderShipmentResponse getById(UUID id)`
     - `List<BeerOrderShipmentResponse> listByOrderId(UUID beerOrderId, Pageable pageable)` (or simple list if pagination not needed; prefer pagination per Guidelines §7)
     - `BeerOrderShipmentResponse update(UUID id, BeerOrderShipmentRequest request)`
     - `void delete(UUID id)`

2. Service implementation
   - Constructor injection of dependencies: `BeerOrderShipmentRepository`, `BeerOrderRepository` (if present), `BeerOrderShipmentMapper`.
   - Transaction boundaries (Guidelines §4):
     - `@Transactional` on create/update/delete
     - `@Transactional(readOnly = true)` on read methods
   - Business rules:
     - Validate parent `BeerOrder` existence; if missing, throw `NotFoundException` (reuse existing exception class `handlers/NotFoundException`).
     - On update, only modify allowed fields; preserve immutable identifiers.

Deliverables:
- Service interface and implementation with tests

Acceptance criteria:
- Unit tests for service methods covering happy paths and error conditions.

---

## Phase 5 – REST Controller
1. Controller (package-private) with versioned resource paths (Guidelines §7)
   - Base path: `/api/v1/beer-orders/{orderId}/shipments` for sub-resource collection
     - `POST /` → create shipment for the given order
     - `GET /` → list shipments for order (support pagination via `page`, `size`, `sort`)
   - Item path: `/api/v1/beer-orders/{orderId}/shipments/{id}`
     - `GET` → get by id (optionally validate `orderId` matches entity)
     - `PUT` → update
     - `DELETE` → delete
   - Use `ResponseEntity<T>` with explicit status codes: 201 Created (with `Location` header) on create; 200 on read/update; 204 on delete.
   - Request/response bodies use DTOs, not entities (Guidelines §6).
   - Validation via `@Valid` and method-level annotations; return errors via centralized handler (Guidelines §9).

2. Global exception handling
   - Ensure the existing `@ControllerAdvice` returns consistent error responses (ProblemDetails, if available). Add handlers for `NotFoundException` if not present.

Deliverables:
- Controller class and (if needed) updates to global exception handling

Acceptance criteria:
- Controller unit tests with `@WebMvcTest` or slice tests verifying status codes, payloads, and validation.

---

## Phase 6 – OpenAPI Documentation
1. Update `openapi/openapi/openapi.yaml`:
   - Add paths files under `openapi/openapi/paths` using flat `_` naming (Guidelines §16.1), e.g.,
     - `'/api/v1/beer-orders/{orderId}/shipments': $ref: 'paths/api_v1_beer-orders_{orderId}_shipments.yaml'`
     - `'/api/v1/beer-orders/{orderId}/shipments/{id}': $ref: 'paths/api_v1_beer-orders_{orderId}_shipments_{id}.yaml'`

2. Define or reuse components under `openapi/openapi/components`:
   - Schemas: `BeerOrderShipmentRequest.yaml`, `BeerOrderShipmentResponse.yaml` (PascalCase filenames; §16.2)
   - Common error response: reuse `Problem.yaml` if present.

3. Lint and validate
   - From `/openapi`, run `npm ci` (first time) then `npm test` to lint with Redocly (Guidelines §16.4).

Deliverables:
- New path files and component schemas; passing OpenAPI lint

Acceptance criteria:
- `npm test` passes; docs render via `npm start` (optional manual check).

---

## Phase 7 – Testing Strategy
1. Unit tests
   - Mapper tests: verify field mapping both directions.
   - Service tests: happy/edge cases; use mocks for repositories.
   - Controller slice tests: `@WebMvcTest` with mocked service; validation and error shapes verified.

2. Integration tests (Guidelines §12 & §13)
   - Use Testcontainers for the database consistent with production DB type.
   - Start application with `@SpringBootTest(webEnvironment = RANDOM_PORT)`.
   - Repository and controller integration tests covering typical CRUD flows.

3. Test data
   - Create fixtures/builders for `BeerOrder` and `BeerOrderShipment`.

Deliverables:
- Comprehensive test suite across layers

Acceptance criteria:
- All tests pass locally and in CI; coverage includes success and failure paths.

---

## Phase 8 – Cross‑Cutting Concerns & Housekeeping
1. Transactions: Ensure each service method has proper `@Transactional` annotation (Guidelines §4).
2. OSIV: Confirm `spring.jpa.open-in-view=false` is set (Guidelines §5); adjust fetch strategies and queries as needed.
3. Logging: Use SLF4J; avoid sensitive data; guard expensive logs (Guidelines §14).
4. Visibility: Prefer package‑private controllers/configs/beans where feasible (Guidelines §2).
5. Constructor injection: Apply to all new beans (Guidelines §1).
6. Validation: Ensure DTO validation annotations and method `@Validated` as needed.
7. Actuator: Verify only essential endpoints are public; others secured (Guidelines §10).
8. i18n: Externalize any new user‑facing messages if applicable (Guidelines §11).
9. Properties: If any configuration is added, use typed `@ConfigurationProperties` (Guidelines §3).

Deliverables:
- Configuration/property updates as needed; no functional drift

Acceptance criteria:
- Static analysis and linters pass; application starts cleanly without warnings related to the new module.

---

## Definition of Done
- Flyway migration is applied on startup without errors; schema contains `beer_order_shipment` with proper constraints.
- Full CRUD API for shipments under `/api/v1/beer-orders/{orderId}/shipments` works and returns correct HTTP statuses.
- DTOs, repository, mappers, service, and controller implemented with constructor injection and package‑private visibility where possible.
- OpenAPI spec updated; `npm test` in `openapi` passes.
- Unit and integration tests implemented; `mvn test` passes locally and in CI using random port for web tests.
- Adheres to all applicable Spring Boot guidelines provided.

---

## Risks & Mitigations
- Referential integrity: Ensure `beer_order_id` FK and orphan removal semantics are correct; add tests for deletion behavior.
- Time semantics: If shipments require timezone, switch `shipmentDate` to `OffsetDateTime`; document decision in DTO schema and OpenAPI.
- Existing `BeerOrder` equals/hashCode: Review to avoid issues when adding collections; adapt if necessary.
- OSIV disabled: Ensure fetch joins or transactional reads prevent `LazyInitializationException` in controller.

---

## Work Breakdown (High-Level)
1. Entity + Migration (Phase 1)
2. DTOs + Mapper (Phase 2)
3. Repository (Phase 3)
4. Service (Phase 4)
5. Controller (Phase 5)
6. OpenAPI (Phase 6)
7. Tests (Phase 7)
8. Cross‑cutting items and polish (Phase 8)

Each step should be committed separately with clear messages, and the task list in `prompts/assignment4/tasks.md` can mirror these phases for tracking.
