# Assignment One — Beer Order Domain and CRUD API

Updated: 2025-11-02

## Purpose
Design and implement a production-quality Beer Order domain and expose a versioned, DTO-based REST API with full CRUD. Follow the Spring Boot Guidelines included in this repository, especially:
- Constructor injection; avoid field/setter injection
- Prefer package-private visibility for Spring components where possible
- Separate web and persistence layers (no entities in controllers); use DTOs + MapStruct
- Define clear transactional boundaries at the service layer
- Disable OSIV and fetch exactly what you need
- Centralize exception handling; return consistent error responses (Problem Details)
- Use Testcontainers for integration tests and RANDOM_PORT for HTTP tests

The project already contains a Beer domain (entity, DTO, mapper, service, controller, and tests). Use the same packaging, style, and patterns when building the Beer Order domain.

## What You Will Build
Create a Beer Order aggregate:
- BeerOrder (root) has many BeerOrderLine
- BeerOrderLine references exactly one Beer (already implemented)

Expose a versioned REST API at `/api/v1/orders` that supports create, read (single and list with pagination), update, and delete. All requests and responses must use DTOs.

## Data Model (ERD Summary)
- Entities
  - BeerOrder (1 → N BeerOrderLine)
  - BeerOrderLine (N → 1 Beer)
  - Beer (already exists)
- Common columns for all entities: `id` (PK), `version` (optimistic lock), `createdDate`, `updateDate`
- Relationships
  - BeerOrder has many BeerOrderLine items
  - BeerOrderLine references a Beer
- Conventions
  - Use `@Version Integer version` for optimistic locking
  - Use `@CreationTimestamp` and `@UpdateTimestamp` (`LocalDateTime`)
  - Use `FetchType.LAZY` on `@ManyToOne`; keep `@OneToMany` LAZY (default)
  - Cascade only from BeerOrder to BeerOrderLine: `cascade = CascadeType.ALL, orphanRemoval = true`
  - Do NOT cascade from BeerOrderLine to Beer
  - Explicit FK column names: `beer_order_id`, `beer_id`
  - Lombok on entities: `@Getter`, `@Setter`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@ToString(exclude = ...)`, `@EqualsAndHashCode(onlyExplicitlyIncluded = true)` including only the primary key

You can use the ERD image at `prompts/assignmentOne/BeerErd.png` for a visual reference.

## Packages
- Entities: `guru.springframework.juniemvc.entities`
- DTOs: `guru.springframework.juniemvc.models`
- Mappers: `guru.springframework.juniemvc.mappers`
- Repositories: `guru.springframework.juniemvc.repositories`
- Services: `guru.springframework.juniemvc.services`
- Controllers: `guru.springframework.juniemvc.controllers`

Match existing Beer domain file layout and style.

## Entities (JPA)
Create in `guru.springframework.juniemvc.entities`:

1) BeerOrder
- Fields: `id`, `version`, `customerRef` (String), `orderStatus` (optional enum), `orderLines` (List<BeerOrderLine>), `createdDate`, `updateDate`
- Mapping: `@OneToMany(mappedBy = "beerOrder", cascade = ALL, orphanRemoval = true)` on `orderLines`
- Helper methods: `addLine(BeerOrderLine line)` and `removeLine(BeerOrderLine line)` to maintain both sides; `line.setBeerOrder(this)`

2) BeerOrderLine
- Fields: `id`, `version`, `beerOrder` (ManyToOne), `beer` (ManyToOne), `orderQuantity` (Integer), `price` (BigDecimal), `createdDate`, `updateDate`
- Mappings:
  - `@ManyToOne(fetch = LAZY) @JoinColumn(name = "beer_order_id", nullable = false)` to BeerOrder
  - `@ManyToOne(fetch = LAZY) @JoinColumn(name = "beer_id", nullable = false)` to Beer

Visibility: entities remain `public`.

## DTOs and Validation
Create in `guru.springframework.juniemvc.models` (reusing existing `BeerDto`):

- BeerOrderLineDto
  - Fields: `Integer id`, `Integer beerId`, `Integer orderQuantity`, `BigDecimal price`, `Integer version`, `LocalDateTime createdDate`, `LocalDateTime updateDate`
  - Validation: `beerId` `@NotNull`, `orderQuantity` `@NotNull @Positive`

- BeerOrderDto
  - Fields: `Integer id`, `String customerRef`, `List<BeerOrderLineDto> orderLines`, `Integer version`, `LocalDateTime createdDate`, `LocalDateTime updateDate`
  - Validation: `customerRef` `@NotBlank`, `orderLines` `@NotNull @Size(min = 1)` and cascade-validate entries

Notes
- Treat `id`, `version`, `createdDate`, `updateDate` as server-managed (ignored on create/update input)
- Use camelCase consistently in JSON

## MapStruct Mappers
Create in `guru.springframework.juniemvc.mappers` (follow the existing `BeerMapper` style):

- BeerOrderLineMapper
  - `BeerOrderLine toEntity(BeerOrderLineDto dto)`
  - `BeerOrderLineDto toDto(BeerOrderLine entity)`
  - From DTO → Entity: ignore `id`, `version`, `createdDate`, `updateDate`
  - Map `beerId` → `beer.id` (attach actual Beer in the service)

- BeerOrderMapper
  - `BeerOrder toEntity(BeerOrderDto dto)`
  - `BeerOrderDto toDto(BeerOrder entity)`
  - From DTO → Entity: ignore `id`, `version`, `createdDate`, `updateDate`
  - Map `orderLines` via `BeerOrderLineMapper`

Mapper configuration
- `@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)`
- Constructor injection for any helpers

## Repositories
Create in `guru.springframework.juniemvc.repositories`:

- BeerOrderRepository extends `JpaRepository<BeerOrder, Integer>`
  - Add a method to fetch an order with lines:
    - `@EntityGraph(attributePaths = "orderLines") Optional<BeerOrder> findWithOrderLinesById(Integer id);`
    - or an equivalent JPQL fetch-join variant

- BeerOrderLineRepository extends `JpaRepository<BeerOrderLine, Integer>`
  - Mainly for tests/inspections; writes should flow through the BeerOrder aggregate

Guidelines
- Keep repository interfaces package-private where possible
- No business logic in repositories

## Services (Transactional Boundaries)
Create in `guru.springframework.juniemvc.services` using constructor injection and package-private visibility where applicable:

- BeerOrderService (interface)
  - `BeerOrderDto createOrder(@Valid BeerOrderDto request)`
  - `BeerOrderDto getOrder(Integer id)` — 404 if not found
  - `Page<BeerOrderDto> listOrders(Pageable pageable)` — default sort by `id`
  - `BeerOrderDto updateOrder(Integer id, @Valid BeerOrderDto request)` — full replace (ignore client id/version/dates)
  - `void deleteOrder(Integer id)` — idempotent (no-op if not found)

- BeerOrderServiceImpl
  - Annotate class with `@Service`
  - Annotate methods:
    - `@Transactional` for create/update/delete
    - `@Transactional(readOnly = true)` for get/list
  - Responsibilities:
    - Validate and load each referenced `Beer` by `beerId`; throw `NotFoundException` if missing
    - Maintain aggregate relationships (`line.setBeerOrder(order)`; `order.addLine(line)`)
    - Persist via `BeerOrderRepository` (lines persisted via cascade)
    - Convert exclusively with mappers; never return entities

## REST Controller
Create in `guru.springframework.juniemvc.controllers`:

- BeerOrderController mapped to `/api/v1/orders`
  - POST `/` — create
    - Request: `@Valid BeerOrderDto`
    - Response: `ResponseEntity<BeerOrderDto>` with 201 Created and `Location: /api/v1/orders/{id}`
  - GET `/{id}` — get one (200 or 404)
  - GET `/` — list with pagination
    - Query params: `page`, `size`, `sort` (Spring Data semantics)
    - Response: a page wrapper (Spring’s `Page` JSON shape is acceptable)
  - PUT `/{id}` — full update (200 or 404)
  - DELETE `/{id}` — idempotent delete (204 No Content)

Controller rules
- Constructor injection; prefer package-private class visibility
- Only use DTOs; do not expose entities
- Delegate error handling to `GlobalExceptionHandler`

## Error Handling
- Reuse `src/main/java/.../handlers/GlobalExceptionHandler.java`
- Add/extend a runtime `NotFoundException` for missing Beer/BeerOrder
- Validation errors must return a JSON Problem Details style response (see existing handler; align with RFC 9457)

## Configuration and OSIV
- Ensure `spring.jpa.open-in-view=false` in `src/main/resources/application.properties`
- Avoid lazy-loading during serialization; fetch needed data inside transactions (e.g., `findWithOrderLinesById`)

## Logging and I18N
- Use SLF4J; never `System.out.println`
- Do not log sensitive data
- Place user-facing text/keys in `messages.properties` for future i18n (optional for this assignment)

## Tests
Target a test pyramid: repository → service → controller. Prefer realistic tests with Testcontainers for DB and RANDOM_PORT for HTTP.

- Repository Tests (`@DataJpaTest` + Testcontainers)
  - Use a specific database image tag (not `latest`)
  - Verify CRUD on BeerOrderRepository including cascade persist and orphan removal
  - Verify `findWithOrderLinesById` fetches lines without N+1

- Service Tests (JUnit + Mockito or Spring Boot test slice)
  - Use real MapStruct mappers where practical
  - Mock repositories and verify behaviors:
    - `createOrder` persists aggregate; fails when a `beerId` is missing
    - `getOrder` returns DTO or throws `NotFoundException`
    - `updateOrder` replaces lines correctly (remove missing, add new)
    - `deleteOrder` is idempotent

- Controller Tests
  - Prefer `@SpringBootTest(webEnvironment = RANDOM_PORT)` + Testcontainers for end-to-end HTTP
  - Cover: POST 201, GET 200/404, PUT 200/404, DELETE 204, GET page 200
  - Validate request body constraints (400 with details) and response schema

General testing notes
- Use Jackson `ObjectMapper` for JSON
- Keep camelCase consistently
- Seed Beer records in tests to satisfy foreign keys

## Deliverables (Create/Update)
- Entities: `BeerOrder.java`, `BeerOrderLine.java`
- DTOs: `BeerOrderDto.java`, `BeerOrderLineDto.java`
- Mappers: `BeerOrderMapper.java`, `BeerOrderLineMapper.java`
- Repositories: `BeerOrderRepository.java`, `BeerOrderLineRepository.java`
- Services: `BeerOrderService.java`, `BeerOrderServiceImpl.java`
- Controller: `BeerOrderController.java`
- Tests: repository, service, controller tests as described
- Configuration: verify `spring.jpa.open-in-view=false`

## Acceptance Criteria
- DTO-based API at `/api/v1/orders` with fully functional CRUD and proper HTTP semantics
- MapStruct ignores `id`, `version`, `createdDate`, `updateDate` when mapping DTO → entity
- Constructor injection everywhere; package-private visibility for Spring components where possible
- Transactions applied at the service layer with read-only/write separation
- OSIV disabled; no lazy-loading during serialization; fetch strategies verified in tests
- Global error handling returns consistent Problem Details for 400/404 (and other handled errors)
- Tests pass locally: repository (with Testcontainers), service, and controller (RANDOM_PORT)

## Definition of Done
- Code style and packaging consistent with existing Beer domain
- No controller or service exposes JPA entities
- DTOs, mappers, services, controllers implemented per guidelines
- Comprehensive tests exist and pass; no leaking of sensitive data in logs
- Clear documentation and self-describing code; API is predictable and versioned

## Out of Scope
- Async processing, messaging, or eventing
- Partial updates (PATCH)
- Advanced filtering/sorting beyond Spring Data basics

## Appendix A — Sample Payloads

Create (POST /api/v1/orders):
```json
{
  "customerRef": "ACME-PO-12345",
  "orderLines": [
    { "beerId": 101, "orderQuantity": 6, "price": 12.99 },
    { "beerId": 102, "orderQuantity": 12, "price": 10.50 }
  ]
}
```

Response (201):
```json
{
  "id": 1,
  "customerRef": "ACME-PO-12345",
  "orderLines": [
    { "id": 1, "beerId": 101, "orderQuantity": 6, "price": 12.99, "version": 0 },
    { "id": 2, "beerId": 102, "orderQuantity": 12, "price": 10.50, "version": 0 }
  ],
  "version": 0,
  "createdDate": "2025-11-02T00:04:00",
  "updateDate": "2025-11-02T00:04:00"
}
```

Page listing (GET /api/v1/orders?page=0&size=20):
```json
{
  "content": [ { "id": 1, "customerRef": "ACME-PO-12345", "orderLines": [] } ],
  "pageable": { "pageNumber": 0, "pageSize": 20, "offset": 0, "paged": true, "unpaged": false },
  "totalElements": 1,
  "totalPages": 1,
  "last": true,
  "size": 20,
  "number": 0,
  "sort": { "empty": true, "sorted": false, "unsorted": true },
  "numberOfElements": 1,
  "first": true,
  "empty": false
}
```