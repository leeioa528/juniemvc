# Assignment 4 – Detailed Task List

Note: Tick a task by changing [ ] to [x] when it is completed. Follow the tasks in order.

1. [x] Phase 1 – Domain & Persistence Model
   1. [x] Create `BeerOrderShipment` JPA entity with fields: `UUID id`, `LocalDate shipmentDate`, `String carrier`, `String carrierNumber` (+ audit fields if used).
   2. [x] Add `@ManyToOne(optional = false)` association from `BeerOrderShipment` to `BeerOrder` with `@JoinColumn(name = "beer_order_id", nullable = false)`.
   3. [x] Update `BeerOrder` entity to include `@OneToMany(mappedBy = "beerOrder", cascade = CascadeType.ALL, orphanRemoval = true)` as `Set<BeerOrderShipment> shipments`.
   4. [x] Ensure equals/hashCode strategy matches project conventions (identifier-based) to avoid persistence issues.
   5. [x] Create Flyway migration `Vxx__add_beer_order_shipment.sql` under `src/main/resources/db/migration` that:
      - [x] Creates table `beer_order_shipment` with `id`, `beer_order_id`, `shipment_date`, `carrier`, `carrier_number`, and auditing columns if applicable.
      - [x] Adds foreign key constraint to `beer_order_id` in a separate `ALTER TABLE` statement.
      - [x] Adds an index on `beer_order_id`.
      - [x] Uses snake_case for all column names.
   6. [x] Start the application and verify Flyway applies the migration successfully on a clean DB.

2. [x] Phase 2 – DTOs and Mapping
   1. [x] Create `BeerOrderShipmentRequest` record with fields: `UUID beerOrderId`, `LocalDate shipmentDate`, `String carrier`, `String carrierNumber` and add Jakarta validation annotations.
   2. [x] Create `BeerOrderShipmentResponse` record with fields: `UUID id`, `UUID beerOrderId`, `LocalDate shipmentDate`, `String carrier`, `String carrierNumber` (and audit fields if exposed).
   3. [x] Define MapStruct `BeerOrderShipmentMapper` with methods: `toEntity(request, beerOrder)`, `toResponse(entity)`, and `updateEntity(@MappingTarget entity, request)`; set `componentModel = "spring"`.
   4. [x] Add basic unit tests to verify mapper behavior.

3. [x] Phase 3 – Repository Layer
   1. [x] Create `BeerOrderShipmentRepository extends JpaRepository<BeerOrderShipment, UUID>`.
   2. [x] Add query helper `List<BeerOrderShipment> findByBeerOrderId(UUID beerOrderId)` if needed.

4. [x] Phase 4 – Service Layer
   1. [x] Define service API `BeerOrderShipmentService` (package-private) with methods: `create`, `getById`, `listByOrderId(Pageable)`, `update`, `delete`.
   2. [x] Implement `BeerOrderShipmentServiceImpl` using constructor injection for `BeerOrderShipmentRepository`, `BeerOrderRepository` (or equivalent), and `BeerOrderShipmentMapper`.
   3. [x] Add transaction boundaries: annotate write methods with `@Transactional` and read methods with `@Transactional(readOnly = true)`.
   4. [x] Implement business rules: validate parent `BeerOrder` existence; on missing parent or shipment throw `NotFoundException`; preserve immutable identifiers on update.
   5. [x] Write unit tests covering happy paths and error conditions for the service layer.

5. [x] Phase 5 – REST Controller
   1. [x] Create package-private controller for shipments under versioned paths:
      - [x] `POST /api/v1/beer-orders/{orderId}/shipments` – create shipment; return `201 Created` with `Location` header.
      - [x] `GET /api/v1/beer-orders/{orderId}/shipments` – list shipments (support `page`, `size`, `sort`).
      - [x] `GET /api/v1/beer-orders/{orderId}/shipments/{id}` – get by id (optionally verify order match).
      - [x] `PUT /api/v1/beer-orders/{orderId}/shipments/{id}` – update; return `200 OK`.
      - [x] `DELETE /api/v1/beer-orders/{orderId}/shipments/{id}` – delete; return `204 No Content`.
   2. [x] Ensure controller uses DTOs, `@Valid`, and returns `ResponseEntity<T>` with explicit status codes.
   3. [x] Review/update global exception handling (`@ControllerAdvice`) to include `NotFoundException` mapping and consistent error responses.
   4. [x] Add controller slice tests (`@WebMvcTest`) validating statuses, payloads, and validation errors.

6. [x] Phase 6 – OpenAPI Documentation
   1. [x] Update `openapi/openapi/openapi.yaml` to reference new path files:
      - [x] `'/api/v1/beer-orders/{orderId}/shipments': $ref: 'paths/api_v1_beer-orders_{orderId}_shipments.yaml'`
      - [x] `'/api/v1/beer-orders/{orderId}/shipments/{id}': $ref: 'paths/api_v1_beer-orders_{orderId}_shipments_{id}.yaml'`
   2. [x] Add component schemas under `openapi/openapi/components/schemas`:
      - [x] `BeerOrderShipmentRequest.yaml`
      - [x] `BeerOrderShipmentResponse.yaml`
   3. [x] Lint and validate the OpenAPI spec from `/openapi`:
      - [x] Run `npm ci` (first time only) or `npm install`.
      - [x] Run `npm test` and ensure it passes.

7. [x] Phase 7 – Testing Strategy (Implementation)
   1. [x] Unit tests: mapper tests (both directions) and service tests (happy and edge cases).
   2. [x] Controller slice tests with `@WebMvcTest` and mocked service: verify validation and error shapes.
   3. [x] Integration tests using Testcontainers for the database and `@SpringBootTest(webEnvironment = RANDOM_PORT)` covering typical CRUD flows.
   4. [x] Create test fixtures/builders for `BeerOrder` and `BeerOrderShipment`.

8. [x] Phase 8 – Cross‑Cutting Concerns & Housekeeping
   1. [x] Ensure all new beans use constructor injection only (no field/setter injection).
   2. [x] Prefer package-private visibility for controllers/config/beans where feasible.
   3. [x] Verify `spring.jpa.open-in-view=false` is set; adjust fetch strategies/queries as needed.
   4. [x] Apply proper logging via SLF4J; avoid sensitive data; guard expensive logs.
   5. [x] Ensure DTO validation annotations and add `@Validated` where needed.
   6. [x] Verify actuator exposure policy: only essential endpoints unauthenticated; others secured.
   7. [x] Externalize any new user-facing text into ResourceBundles (i18n) if applicable.
   8. [x] Use typed `@ConfigurationProperties` for any new configuration.

9. [ ] Definition of Done – Verification
   1. [x] Application starts cleanly; Flyway migration applies; schema contains `beer_order_shipment` with constraints.
   2. [x] Full CRUD API for shipments works under `/api/v1/beer-orders/{orderId}/shipments` with correct HTTP statuses.
   3. [x] Code adheres to project guidelines (constructor injection, DTO separation, transactions, exception handling, visibility).
   4. [ ] OpenAPI lint (`npm test` in `openapi`) passes; docs preview renders (optional check).
   5. [ ] All unit and integration tests pass locally and in CI; web tests use RANDOM_PORT.
