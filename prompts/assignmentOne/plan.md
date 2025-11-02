# Assignment One — Implementation Plan for Beer Order Domain and CRUD API

Updated: 2025-11-02

## 0) Scope Overview
Implement a production-quality Beer Order domain with full CRUD REST API at `/api/v1/orders`, backed by DTOs, MapStruct mappers, repositories, transactional services, and comprehensive tests. Follow the Spring Boot Guidelines in this repo (constructor injection, package-private components, DTO separation, clear transactions, OSIV disabled, centralized exception handling, Testcontainers, RANDOM_PORT tests).

## 1) Assumptions and Constraints
- Java 17+ and Spring Boot as per existing project `pom.xml`.
- MapStruct already configured (BeerMapper and generated impls exist). We will mirror its configuration.
- Global exception handler exists; we will extend it with `NotFoundException` handling for Beer/BeerOrder lookups.
- Database is configured for tests via Testcontainers; we will add container setup to test slices as needed.
- JSON uses camelCase and DTOs are the only payloads exposed.
- `spring.jpa.open-in-view=false` must remain enforced.

## 2) High-Level Work Breakdown and Order of Execution
1. Entities (JPA): `BeerOrder`, `BeerOrderLine` with relationships and timestamps.
2. DTOs + Validation: `BeerOrderDto`, `BeerOrderLineDto` with Jakarta validation annotations.
3. MapStruct Mappers: `BeerOrderMapper`, `BeerOrderLineMapper` with proper ignores and id mapping.
4. Repositories: `BeerOrderRepository`, `BeerOrderLineRepository` (package-private) with entity graph for lines.
5. Service Layer: `BeerOrderService` and `BeerOrderServiceImpl` with transactional methods and business logic.
6. REST Controller: `BeerOrderController` implementing versioned endpoints and correct HTTP semantics.
7. Error Handling: Ensure `NotFoundException` mapped; validation errors return ProblemDetails.
8. Configuration: Verify OSIV setting; no lazy-loading in serialization paths.
9. Tests: Repository → Service → Controller using Testcontainers and RANDOM_PORT.
10. Documentation: README notes and API examples (optional); ensure prompts alignment.

## 3) Detailed Implementation Steps

### 3.1 Entities (JPA)
- Location: `src/main/java/guru/springframework/juniemvc/entities`
- Create `BeerOrder`:
  - Fields: `Integer id`, `Integer version`, `String customerRef`, `List<BeerOrderLine> orderLines`, `LocalDateTime createdDate`, `LocalDateTime updateDate`, optional `orderStatus` enum.
  - Annotations: `@Entity`, `@Table(name = "beer_order")`, `@Version`, `@CreationTimestamp`, `@UpdateTimestamp`.
  - Relationship: `@OneToMany(mappedBy = "beerOrder", cascade = CascadeType.ALL, orphanRemoval = true)`.
  - Helper methods: `addLine`, `removeLine` to manage both sides.
  - Lombok: `@Getter`, `@Setter`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@ToString(exclude = "orderLines")`, `@EqualsAndHashCode(onlyExplicitlyIncluded = true)`.
- Create `BeerOrderLine`:
  - Fields: `Integer id`, `Integer version`, `BeerOrder beerOrder`, `Beer beer`, `Integer orderQuantity`, `BigDecimal price`, `LocalDateTime createdDate`, `LocalDateTime updateDate`.
  - Annotations: `@Entity`, `@Table(name = "beer_order_line")`, `@ManyToOne(fetch = LAZY)` with `@JoinColumn(name = "beer_order_id", nullable = false)`, `@JoinColumn(name = "beer_id", nullable = false)` for beer.
  - Lombok similar to above (exclude large associations from `toString`).
- Conventions: Explicit column names for FKs, LAZY for `@ManyToOne`; do not cascade from line → beer.

### 3.2 DTOs and Validation
- Location: `src/main/java/guru/springframework/juniemvc/models`
- Create `BeerOrderLineDto` with fields: `Integer id`, `Integer beerId`, `Integer orderQuantity`, `BigDecimal price`, `Integer version`, `LocalDateTime createdDate`, `LocalDateTime updateDate`.
  - Validation: `@NotNull beerId`, `@NotNull @Positive orderQuantity`.
- Create `BeerOrderDto` with fields: `Integer id`, `String customerRef`, `List<BeerOrderLineDto> orderLines`, `Integer version`, `LocalDateTime createdDate`, `LocalDateTime updateDate`.
  - Validation: `@NotBlank customerRef`, `@NotNull @Size(min = 1) orderLines` and `@Valid` on elements.
- Treat id/version/dates as server-managed on input; do not rely on client-provided values.

### 3.3 MapStruct Mappers
- Location: `src/main/java/guru/springframework/juniemvc/mappers`
- `BeerOrderLineMapper`:
  - Methods: `BeerOrderLine toEntity(BeerOrderLineDto dto)`, `BeerOrderLineDto toDto(BeerOrderLine entity)`.
  - Ignore `id`, `version`, `createdDate`, `updateDate` in `toEntity`.
  - Map `beerId` → `beer.id` (service will resolve and attach managed Beer entity).
- `BeerOrderMapper`:
  - Methods: `BeerOrder toEntity(BeerOrderDto dto)`, `BeerOrderDto toDto(BeerOrder entity)`.
  - Ignore `id`, `version`, `createdDate`, `updateDate` in `toEntity`.
  - Delegate `orderLines` to `BeerOrderLineMapper`.
- Configuration: `@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)`; prefer constructor injection for helpers if any.

### 3.4 Repositories
- Location: `src/main/java/guru/springframework/juniemvc/repositories`
- `interface BeerOrderRepository extends JpaRepository<BeerOrder, Integer>`:
  - Add `@EntityGraph(attributePaths = "orderLines") Optional<BeerOrder> findWithOrderLinesById(Integer id);`
- `interface BeerOrderLineRepository extends JpaRepository<BeerOrderLine, Integer>` (primarily for tests/inspections).
- Visibility: default (package-private) where possible; no business logic.

### 3.5 Service Layer
- Location: `src/main/java/guru/springframework/juniemvc/services`
- `BeerOrderService` (interface):
  - `BeerOrderDto createOrder(@Valid BeerOrderDto request)`
  - `BeerOrderDto getOrder(Integer id)`
  - `Page<BeerOrderDto> listOrders(Pageable pageable)`
  - `BeerOrderDto updateOrder(Integer id, @Valid BeerOrderDto request)`
  - `void deleteOrder(Integer id)`
- `BeerOrderServiceImpl`:
  - Annotations: `@Service` on class.
  - Transactions: `@Transactional` on create/update/delete; `@Transactional(readOnly = true)` on get/list.
  - Logic:
    - Validate each `beerId`: load Beer via `BeerRepository.findById` else throw `NotFoundException`.
    - Build aggregate: map DTO → entity, set `beerOrder` on lines, attach `Beer` refs.
    - Persist via `BeerOrderRepository.save` (cascade persists lines).
    - For update: replace lines by clearing and re-adding based on DTO; handle orphan removal.
    - Return DTO via mappers only (no entity leakage).

### 3.6 REST Controller
- Location: `src/main/java/guru/springframework/juniemvc/controllers`
- `BeerOrderController` mapped to `/api/v1/orders`:
  - POST `/` -> create; returns 201 with `Location: /api/v1/orders/{id}` and body.
  - GET `/{id}` -> 200 or 404.
  - GET `/` -> page listing; supports `page`, `size`, `sort` (delegates to service).
  - PUT `/{id}` -> full replace; returns 200 or 404.
  - DELETE `/{id}` -> idempotent; returns 204.
- Controller rules: constructor injection; package-private visibility if feasible; use DTOs only; rely on `GlobalExceptionHandler`.

### 3.7 Error Handling
- Reuse `GlobalExceptionHandler` and extend if necessary:
  - Add handler for `NotFoundException` returning Problem Details (RFC 9457 aligned) with 404.
  - Ensure validation errors (MethodArgumentNotValidException) already mapped; if not, add mapping.
- Define `NotFoundException extends RuntimeException` in a suitable package (e.g., `handlers` or `services`).

### 3.8 Configuration and OSIV
- Verify `spring.jpa.open-in-view=false` in `application.properties` (already present).
- Ensure controllers do not trigger lazy-loading during serialization: fetch required data in service using `findWithOrderLinesById` before mapping to DTO.

### 3.9 Testing Strategy
- Common:
  - Use Testcontainers with explicit image tags (e.g., `postgres:16.3` if Postgres is used).
  - Seed `Beer` records needed for FK references.
  - Use camelCase JSON and `ObjectMapper` in HTTP tests.

- Repository Tests (`@DataJpaTest` + Testcontainers):
  - CRUD on `BeerOrderRepository`, cascade persist from order to lines, orphan removal on line removal and delete.
  - `findWithOrderLinesById` fetches lines without N+1 (verify with `@DataJpaTest` and possibly `hibernate.show_sql` assertion or second-level observation).

- Service Tests (JUnit with Mockito or Spring Boot test):
  - Use real mappers; mock repositories (`BeerRepository`, `BeerOrderRepository`).
  - Cases:
    - `createOrder` happy path persists aggregate; failure when any `beerId` not found -> `NotFoundException`.
    - `getOrder` returns DTO or throws `NotFoundException`.
    - `updateOrder` fully replaces lines; ensures orphan removal (verify repository interactions).
    - `deleteOrder` idempotent (no exception if id missing).

- Controller Tests:
  - Prefer `@SpringBootTest(webEnvironment = RANDOM_PORT)` + Testcontainers.
  - Cover: POST 201, GET 200/404, PUT 200/404, DELETE 204, GET page 200.
  - Validate request constraints: 400 with ProblemDetails body when invalid.

### 3.10 Observability and Logging
- Use SLF4J (`@Slf4j` on classes if Lombok) for key operations; no `System.out.println`.
- Avoid logging PII or sensitive data; keep logs concise and useful (ids, counts).

### 3.11 Documentation and Developer Experience
- Update README with API endpoints summary and example requests/responses once implemented (optional for this assignment but recommended).
- Ensure prompts and requirements remain in sync (this plan reflects `/prompts/assignmentOne/requirements.md`).

## 4) Non-Functional Requirements
- Consistent package-private visibility for components where possible.
- Thread-safe service operations via transactional boundaries.
- Pagination support for lists; default sorting by `id`.
- Optimistic locking via `@Version` fields.

## 5) Risks and Mitigations
- N+1 queries when returning orders with lines.
  - Mitigation: Use `findWithOrderLinesById` and map within transaction; OSIV disabled.
- Mapper misconfiguration leading to overwriting server-managed fields.
  - Mitigation: Explicit `@Mapping(target = ..., ignore = true)` for id/version/dates.
- Update semantics incorrectly merging lines.
  - Mitigation: Replace line collection deterministically; rely on `orphanRemoval=true`.
- Flaky tests due to container image tags.
  - Mitigation: Use pinned image versions; healthcheck waits.

## 6) Definition of Done (Acceptance Checklist)
- [ ] Entities `BeerOrder` and `BeerOrderLine` created with correct mappings and helper methods.
- [ ] DTOs `BeerOrderDto`, `BeerOrderLineDto` with validation annotations.
- [ ] Mappers implemented with correct ignore rules and field mappings.
- [ ] Repositories created with entity graph fetch method.
- [ ] Service interface and implementation with transactional boundaries and full CRUD.
- [ ] Controller with versioned endpoints and correct HTTP semantics (201, 200/404, 204, pagination).
- [ ] Global exception handling covers 404 and validation errors with Problem Details.
- [ ] `spring.jpa.open-in-view=false` verified.
- [ ] Repository, service, and controller tests implemented and passing with Testcontainers and RANDOM_PORT.
- [ ] README (optional) updated with API usage examples.

## 7) Estimated Effort and Sequence (Relative)
1. Entities: 2–3h
2. DTOs & Validation: 0.5–1h
3. Mappers: 1–2h
4. Repositories: 0.5h
5. Service: 2–3h
6. Controller: 1–2h
7. Error Handling adjustments: 0.5h
8. Tests: 4–6h (containers setup, seeds, end-to-end cases)
9. Docs: 0.5h

## 8) Implementation Notes and Conventions
- Keep package-private classes for controller and service impl if feasible; public for entities and DTOs.
- Use `Pageable` directly in controller method signature; return `Page<BeerOrderDto>`.
- Build `Location` header using created id.
- Ensure `Beer` references are attached via `BeerRepository` lookups before saving.
- Avoid bidirectional JSON serialization pitfalls by only exposing DTOs (no `@JsonManagedReference` needed on entities since they are not serialized).
