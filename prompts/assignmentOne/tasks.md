# Assignment One — Detailed Task List (from plan.md)

Updated: 2025-11-02 00:40

1. [x] Create JPA Entities for Beer Orders
   - [x] Define `BeerOrder` entity with fields: `id`, `version`, `customerRef`, `orderLines`, `createdDate`, `updateDate` (and optional `orderStatus`).
   - [x] Annotate `BeerOrder` with `@Entity`, `@Table(name = "beer_order")`, `@Version`, `@CreationTimestamp`, `@UpdateTimestamp`.
   - [x] Configure one-to-many relationship: `@OneToMany(mappedBy = "beerOrder", cascade = ALL, orphanRemoval = true)`.
   - [x] Add helper methods `addLine` and `removeLine` maintaining both sides of the association.
   - [x] Add Lombok annotations: `@Getter`, `@Setter`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@ToString(exclude = "orderLines")`, `@EqualsAndHashCode(onlyExplicitlyIncluded = true)`.
   - [x] Define `BeerOrderLine` entity with fields: `id`, `version`, `beerOrder`, `beer`, `orderQuantity`, `price`, `createdDate`, `updateDate`.
   - [x] Annotate `BeerOrderLine` with `@Entity`, `@Table(name = "beer_order_line")` and relationships: `@ManyToOne(fetch = LAZY)` + `@JoinColumn(name = "beer_order_id", nullable = false)` and `@JoinColumn(name = "beer_id", nullable = false)`.
   - [x] Apply Lombok to `BeerOrderLine` (exclude large associations from `toString`).
   - [x] Ensure conventions: explicit FK column names; do not cascade from line → beer; `@ManyToOne` uses LAZY.

2. [x] Create DTOs and Add Validation
   - [x] Implement `BeerOrderLineDto`: `id`, `beerId`, `orderQuantity`, `price`, `version`, `createdDate`, `updateDate`.
   - [x] Add validation: `@NotNull beerId`, `@NotNull @Positive orderQuantity`.
   - [x] Implement `BeerOrderDto`: `id`, `customerRef`, `orderLines`, `version`, `createdDate`, `updateDate`.
   - [x] Add validation: `@NotBlank customerRef`, `@NotNull @Size(min = 1) orderLines` and `@Valid` for elements.
   - [x] Treat id/version/dates as server-managed on input; document this behavior.

3. [x] Implement MapStruct Mappers
   - [x] Create `BeerOrderLineMapper` with methods: `toEntity(BeerOrderLineDto)` and `toDto(BeerOrderLine)`.
   - [x] In `toEntity`, ignore server-managed fields: `id`, `version`, `createdDate`, `updateDate`.
   - [x] Map `beerId` (DTO) → `beer.id` (entity) to be resolved by service.
   - [x] Create `BeerOrderMapper` with methods: `toEntity(BeerOrderDto)` and `toDto(BeerOrder)`.
   - [x] In `toEntity`, ignore server-managed fields and delegate `orderLines` to `BeerOrderLineMapper`.
   - [x] Configure mappers with `@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)`.

4. [x] Create Spring Data Repositories (package-private where possible)
   - [x] `BeerOrderRepository extends JpaRepository<BeerOrder, Integer>`.
   - [x] Add `@EntityGraph(attributePaths = "orderLines") Optional<BeerOrder> findWithOrderLinesById(Integer id)`.
   - [x] `BeerOrderLineRepository extends JpaRepository<BeerOrderLine, Integer>` (primarily for tests/inspections).

5. [x] Implement Service Layer with Transaction Boundaries
   - [x] Define `BeerOrderService` interface with: `createOrder`, `getOrder`, `listOrders`, `updateOrder`, `deleteOrder`.
   - [x] Implement `BeerOrderServiceImpl` annotated with `@Service` using constructor injection.
   - [x] Annotate methods: `@Transactional` for create/update/delete; `@Transactional(readOnly = true)` for get/list.
   - [x] On create/update: validate each `beerId` with `BeerRepository.findById`; throw `NotFoundException` if missing.
   - [x] Map DTO → entity; set `beerOrder` on lines; attach resolved `Beer` entities; save via `BeerOrderRepository.save`.
   - [x] Implement update semantics: clear and re-add lines (orphanRemoval ensures cleanup) and handle optimistic locking via `@Version`.
   - [x] Map entity → DTO for all returns; do not expose entities.

6. [x] Build REST Controller for `/api/v1/orders`
   - [x] Create `BeerOrderController` (package-private if feasible) with constructor injection.
   - [x] POST `/` → create; return `201 Created` with `Location: /api/v1/orders/{id}` and response body.
   - [x] GET `/{id}` → return `200 OK` with body or `404 Not Found`.
   - [x] GET `/` → paginated listing; accept `page`, `size`, `sort`; return `200 OK` with `Page<BeerOrderDto>`.
   - [x] PUT `/{id}` → full replace; return `200 OK` or `404 Not Found`.
   - [x] DELETE `/{id}` → idempotent delete; return `204 No Content`.
   - [x] Ensure controller only uses DTOs; no entity leakage.

7. [x] Extend Global Error Handling
   - [x] Define `NotFoundException extends RuntimeException` in a suitable package.
   - [x] Add handler in `GlobalExceptionHandler` for `NotFoundException` returning RFC 9457 ProblemDetails with HTTP 404.
   - [x] Verify/implement validation error handling for `MethodArgumentNotValidException` to return ProblemDetails (HTTP 400).

8. [x] Configuration and OSIV
   - [x] Verify `spring.jpa.open-in-view=false` is set in `application.properties` and remains enforced.
   - [x] Ensure services fetch required data within transactions (e.g., `findWithOrderLinesById`) to avoid lazy-load during serialization.

9. [ ] Testing — Repository Layer (`@DataJpaTest` + Testcontainers)
   - [ ] Configure Testcontainers with pinned database image version (e.g., `postgres:16.x`).
   - [x] Seed necessary `Beer` records for FK references.
   - [x] Verify CRUD on `BeerOrderRepository` including cascade persist of lines.
   - [x] Verify orphan removal when lines are removed or orders deleted.
   - [x] Test `findWithOrderLinesById` fetches lines eagerly to avoid N+1.

10. [x] Testing — Service Layer
   - [x] Use real mappers; mock `BeerRepository` and `BeerOrderRepository` (or use slice tests as appropriate).
   - [x] Test `createOrder` happy path and `NotFoundException` when any `beerId` is missing.
   - [x] Test `getOrder` returns DTO or throws `NotFoundException` when not found.
   - [x] Test `updateOrder` replaces lines and respects orphan removal.
   - [x] Test `deleteOrder` is idempotent (no exception if id missing).

11. [x] Testing — Controller Layer (`@SpringBootTest(webEnvironment = RANDOM_PORT)` + Testcontainers)
   - [x] POST returns `201 Created` with `Location` header and response body.
   - [x] GET by id returns `200 OK` or `404 Not Found`.
   - [x] GET collection returns `200 OK` with pagination metadata.
   - [x] PUT returns `200 OK` or `404 Not Found`.
   - [x] DELETE returns `204 No Content` (idempotent behavior).
   - [x] Validate request constraints: invalid payload returns `400 Bad Request` with ProblemDetails JSON.

12. [x] Observability and Logging
   - [x] Use SLF4J (no System.out) for key operations in service/controller.
   - [x] Ensure no sensitive data is logged; keep logs concise (ids, counts).

13. [ ] Documentation and Developer Experience
   - [ ] Update README with endpoints summary and example requests/responses.
   - [ ] Confirm prompts/requirements are aligned with the implementation.

14. [x] Non-Functional and Cross-Cutting Checks
   - [x] Use constructor injection and prefer package-private visibility for components where possible.
   - [x] Ensure pagination support for list endpoint with sensible defaults (sort by `id`).
   - [x] Verify optimistic locking via `@Version` fields on entities.

15. [x] Final Acceptance Check (Definition of Done)
   - [x] Entities created and mapped correctly with helper methods.
   - [x] DTOs with validation annotations completed.
   - [x] Mappers implemented with correct ignore rules and field mappings.
   - [x] Repositories created with entity graph fetch for lines.
   - [x] Service interface and implementation with transactional CRUD logic.
   - [x] Controller with versioned endpoints and correct HTTP semantics.
   - [x] Global exception handling covers 404 and validation errors with ProblemDetails.
   - [x] OSIV disabled and no lazy-loading during serialization paths.
   - [ ] Repository, service, and controller tests implemented and passing with RANDOM_PORT and Testcontainers.
   - [ ] README updated (optional but recommended).