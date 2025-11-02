### Assignment One — Create Beer Order Domain with CRUD API (Repositories, Services, DTOs, Mappers, Controllers, and Tests)

#### Objective
Implement the Beer Order domain according to the provided ERD and expose a clean, versioned REST API with full CRUD operations. Follow the Spring Boot guidelines provided, with an emphasis on:
- Constructor injection, package-private visibility for Spring components where possible
- DTOs and MapStruct mappers (ignore id/createdDate/updateDate when mapping from DTO to entity)
- Clear transaction boundaries at the service layer
- Separation of web and persistence layers (no JPA entities in controllers)
- Global exception handling with consistent error responses
- Testcontainers-based integration tests and random ports for HTTP tests

The end result should be production-quality code with comprehensive tests.

---

### 1) ERD Summary and Domain Model
- Entities:
  - BeerOrder (1 → N BeerOrderLine)
  - BeerOrderLine (N → 1 Beer)
  - Beer (already implemented in the project)
- Common columns for all entities: `id` (PK), `version` (optimistic lock), `createdDate`, `updateDate`.
- Relationships:
  - BeerOrder has many BeerOrderLine items
  - BeerOrderLine references exactly one Beer

Notes and conventions:
- Use `@Version Integer version` for optimistic locking.
- Use `@CreationTimestamp` and `@UpdateTimestamp` for audit timestamps stored as `LocalDateTime`.
- Use `FetchType.LAZY` for `@ManyToOne` and keep `@OneToMany` LAZY (default) to prevent N+1.
- Cascade ONLY from BeerOrder to its OrderLines: `cascade = CascadeType.ALL, orphanRemoval = true`.
- Do NOT cascade from BeerOrderLine to Beer.
- Explicit FK column names: `beer_order_id`, `beer_id`.
- Lombok for entities: prefer `@Getter`, `@Setter`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@ToString(exclude=...)`, `@EqualsAndHashCode(onlyExplicitlyIncluded = true)` including only the primary key.

---

### 2) JPA Entities
Implement the missing entities under `guru.springframework.juniemvc.entities`:

- Beer (already present) — ensure it has fields: `id`, `version`, `beerName`, `beerStyle`, `upc` (unique), `quantityOnHand`, `price` (`@Column(precision=19, scale=2)`), `createdDate`, `updateDate`.

- BeerOrder
  - Fields: `id`, `version`, `customerRef` (string), `orderStatus` (enum if desired), `orderLines` (List<BeerOrderLine>), `createdDate`, `updateDate`.
  - Mapping: `@OneToMany(mappedBy = "beerOrder", cascade = ALL, orphanRemoval = true)` on `orderLines`.
  - Helper methods to manage bi-directional links: `addLine(BeerOrderLine line)` sets `line.setBeerOrder(this)`.

- BeerOrderLine
  - Fields: `id`, `version`, `beerOrder` (ManyToOne), `beer` (ManyToOne), `orderQuantity` (Integer), `price` (BigDecimal), `createdDate`, `updateDate`.
  - Mapping: `@ManyToOne(fetch = LAZY) @JoinColumn(name = "beer_order_id", nullable = false)` to BeerOrder; `@ManyToOne(fetch = LAZY) @JoinColumn(name = "beer_id", nullable = false)` to Beer.

Visibility: Entities remain `public`.

---

### 3) DTOs (Records/POJOs) and Validation
Create DTOs under `guru.springframework.juniemvc.models` (reusing existing `BeerDto`):

- BeerOrderLineDto
  - Fields: `Integer id`, `Integer beerId`, `Integer orderQuantity`, `BigDecimal price`, `Integer version`, `LocalDateTime createdDate`, `LocalDateTime updateDate`.
  - Validation (for create/update requests): `beerId` not null, `orderQuantity` >= 1.

- BeerOrderDto
  - Fields: `Integer id`, `String customerRef`, `List<BeerOrderLineDto> orderLines`, `Integer version`, `LocalDateTime createdDate`, `LocalDateTime updateDate`.
  - Validation (for create/update requests): `customerRef` not blank, `orderLines` not null and not empty, and each `orderLines[i]` must be valid.

Notes:
- Use Jakarta Validation annotations on request DTOs: `@NotNull`, `@NotBlank`, `@Positive`, `@Size(min=1)`, etc.
- For create/update inputs, ignore `id`, `createdDate`, `updateDate` in mapping to entities.

---

### 4) MapStruct Mappers
Create mappers under `guru.springframework.juniemvc.mappers` (parallel to `BeerMapper`):

- BeerOrderLineMapper
  - Methods: `BeerOrderLine toEntity(BeerOrderLineDto dto)`, `BeerOrderLineDto toDto(BeerOrderLine entity)`.
  - Mappings:
    - From DTO → Entity: ignore `id`, `version`, `createdDate`, `updateDate`.
    - Map `beerId` → `beer.id` (only the id; entity reference will be resolved in the service layer or via `@ObjectFactory`).

- BeerOrderMapper
  - Methods: `BeerOrder toEntity(BeerOrderDto dto)`, `BeerOrderDto toDto(BeerOrder entity)`.
  - Mappings:
    - From DTO → Entity: ignore `id`, `version`, `createdDate`, `updateDate`.
    - Map `orderLines` using `BeerOrderLineMapper`.

Configuration:
- Use `@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)`.
- Prefer constructor injection for any helper dependencies (MapStruct will generate an autowired constructor when needed).
- If you use an `@ObjectFactory` to attach existing `Beer` entities by id, document that the service must provide a `Beer` lookup.

---

### 5) Repositories
Create Spring Data repositories under `guru.springframework.juniemvc.repositories`:

- BeerOrderRepository extends `JpaRepository<BeerOrder, Integer>`
  - Add method to fetch an order with lines: either
    - `@EntityGraph(attributePaths = "orderLines") Optional<BeerOrder> findWithOrderLinesById(Integer id);` or
    - a JPQL fetch join query for the same purpose.

- BeerOrderLineRepository extends `JpaRepository<BeerOrderLine, Integer>`
  - Typically used for tests or administrative operations; most write operations flow through `BeerOrder` aggregate.

Repository guidelines:
- No business logic in repositories.
- Keep interfaces package-private where possible.

---

### 6) Service Layer (Transactional Boundaries)
Create an interface and implementation under `guru.springframework.juniemvc.services` using constructor injection and package-private visibility when applicable:

- BeerOrderService (interface)
  - `BeerOrderDto createOrder(@Valid BeerOrderDto request)` — returns created DTO with id, 201 status at controller.
  - `BeerOrderDto getOrder(Integer id)` — 404 if not found.
  - `Page<BeerOrderDto> listOrders(Pageable pageable)` — default sort by `id`.
  - `BeerOrderDto updateOrder(Integer id, @Valid BeerOrderDto request)` — full replace (except id/version/audit).
  - `void deleteOrder(Integer id)` — idempotent; no-op if not found.

- BeerOrderServiceImpl
  - Annotate class with `@Service` and methods with transactions:
    - `@Transactional` for create/update/delete
    - `@Transactional(readOnly = true)` for get/list
  - Responsibilities:
    - Validate and load `Beer` references for each line by `beerId`. Throw a domain exception (e.g., `NotFoundException`) if any referenced beer doesn’t exist.
    - Manage the aggregate: set bi-directional references `line.setBeerOrder(order)` and maintain `order.addLine(line)`.
    - Persist using `BeerOrderRepository` only (let cascade persist lines).
    - Use mappers for all conversions; never return entities from the service layer.

---

### 7) REST Controllers (Versioned, DTO-based)
Create REST controller under `guru.springframework.juniemvc.controllers`:

- BeerOrderController mapped to `/api/v1/orders` (resource-oriented, versioned)
  - POST `/` — create order
    - Input: `BeerOrderDto` (validated)
    - Output: `ResponseEntity<BeerOrderDto>` with status 201 and `Location` header `/api/v1/orders/{id}`
  - GET `/{id}` — get one
    - Output: 200 with body or 404 via global exception handler
  - GET `/` — list with pagination
    - Params: `page`, `size`, `sort` (Spring Data semantics)
    - Output: 200 with a paginated response object `{ "content": [...], "page": n, ... }`
  - PUT `/{id}` — update (full)
    - Input: `BeerOrderDto` (validated)
    - Output: 200 with updated object (or 404 if missing)
  - DELETE `/{id}` — delete
    - Output: 204 No Content (idempotent)

Controller guidelines:
- Use constructor injection and package-private visibility where possible.
- Only deal with DTOs; do not expose entities.
- Rely on `GlobalExceptionHandler` for `NotFoundException`, validation errors, and provide RFC 9457 ProblemDetails-like responses if available.

---

### 8) Exception Handling
- Reuse existing `GlobalExceptionHandler` for consistent errors.
- Add/extend a `NotFoundException` (runtime) for missing Beer/BeerOrder.
- Validation errors should be returned as a JSON object with field errors; ensure the handler is in place.

---

### 9) Configuration and OSIV
- Confirm `spring.jpa.open-in-view=false` in `application.properties`.
- Ensure lazy loading issues are handled via service fetch strategies (`findWithOrderLinesById`) and transactional reads.

---

### 10) Tests
Follow a pyramid: repository tests, service tests, and controller tests.

- Repository Tests (`@DataJpaTest` + Testcontainers)
  - Spin up a real database with Testcontainers (use a fixed image tag, not `latest`).
  - Verify basic CRUD for `BeerOrderRepository` including cascade of `orderLines` and orphan removal on delete.
  - Verify `findWithOrderLinesById` fetches lines without N+1.

- Service Tests (JUnit + Mockito or Spring Boot test slice)
  - Use real mappers (MapStruct impls) where possible.
  - Mock repositories and verify:
    - `createOrder` persists aggregate, sets relationships, and fails when a `beerId` is not found.
    - `getOrder` returns DTO or throws `NotFoundException`.
    - `updateOrder` replaces lines correctly (remove missing, add new) while preserving id/version semantics.
    - `deleteOrder` delegates to repository; is idempotent.

- Controller Tests
  - Prefer `@SpringBootTest(webEnvironment = RANDOM_PORT)` for end-to-end with real HTTP and use Testcontainers DB. Alternatively, use `@WebMvcTest` + mocked service for pure web tests.
  - Cover all endpoints: POST 201, GET 200/404, PUT 200/404, DELETE 204, GET page 200.
  - Validate request body constraints (400 with details) and response schemas.

General test guidelines:
- Use Jackson ObjectMapper for JSON, keep snake_case or camelCase consistently with existing project (camelCase preferred).
- Guard logs (no sensitive data) and include helpful debug messages only when necessary.
- Seed any Beer data needed for foreign key references in tests.

---

### 11) Logging and Internationalization
- Use SLF4J; no `System.out.println`.
- Place user-facing error messages in `messages.properties` for future i18n (optional, at least structure for it).

---

### 12) Deliverables (Files to Create/Update)
- Entities: `BeerOrder.java`, `BeerOrderLine.java` (entities package).
- DTOs: `BeerOrderDto.java`, `BeerOrderLineDto.java` (models package).
- Mappers: `BeerOrderMapper.java`, `BeerOrderLineMapper.java` (mappers package).
- Repositories: `BeerOrderRepository.java`, `BeerOrderLineRepository.java` (repositories package).
- Services: `BeerOrderService.java`, `BeerOrderServiceImpl.java` (services package).
- Controller: `BeerOrderController.java` (controllers package).
- Tests: repository, service, controller test classes mirroring above components. Use Testcontainers and RANDOM_PORT for HTTP tests.
- Configuration: ensure `spring.jpa.open-in-view=false` in application properties.

---

### 13) Acceptance Criteria
- DTO-based API at `/api/v1/orders` with fully functional CRUD.
- MapStruct mappers correctly ignore `id`, `createdDate`, `updateDate` when mapping from DTO to entity.
- All services use constructor injection; controllers and configuration use package-private where possible.
- Transactions applied at service layer with readOnly/write separation.
- OSIV disabled; no LazyInitializationExceptions under normal API use.
- Tests pass locally, using Testcontainers for repositories/integration; HTTP tests run on a random port.
- GlobalExceptionHandler returns consistent error responses for 400/404.
- No entities exposed directly to the web layer; only DTOs on the API boundary.
