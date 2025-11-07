# Add Customer Domain & CRUD API — Detailed Task List

Note: Check off each task as you complete it. Use [x] to mark an item done.

1. Domain and Data Model
   1.1 Define Customer aggregate
   - [x] Confirm primary key type (match existing convention: UUID or Long) and document choice — Use Integer with IDENTITY to match existing entities (e.g., Beer, BeerOrder)
   - [x] Define Customer fields: name, email, phoneNumber, addressLine1, addressLine2, city, state, postalCode
   - [x] Decide and document nullability rules (required vs optional) for each field — Required: name, addressLine1, city, state, postalCode. Optional: email, phoneNumber, addressLine2
   - [x] Review additional auditing fields used in project (e.g., createdDate, lastModifiedDate) — Use createdDate/updateDate with @CreationTimestamp/@UpdateTimestamp consistent with existing entities
   
   1.2 Establish association to orders
   - [x] Decide cascade rules from Customer to BeerOrder (default: no cascading; orphanRemoval=false)
   - [x] Document business rationale for chosen cascade/orphanRemoval settings — Avoid accidental deletion/updates of orders via Customer lifecycle; orders are managed independently
   
   1.3 Identity consistency
   - [x] Verify consistency with existing BeerOrder id type and repository expectations — Integer IDs confirmed

2. Database Schema with Flyway
   2.1 Create migration file
   - [x] Determine next Flyway version number based on existing migrations — Next is V2
   - [x] Create `V2__add_customer.sql` under `src/main/resources/db/migration`
   
   2.2 Define Customer table DDL
   - [x] Create `customer` table with all columns and correct nullability
   - [x] Add indexes as needed (e.g., email non-unique; city/state if queried)
   - [x] Include auditing columns if used by project conventions
   
   2.3 Update BeerOrder → Customer relationship
   - [x] Add `customer_id` column to `beer_order` (nullable vs not null decision) — Added as nullable initially for backward compatibility
   - [x] Create foreign key constraint to `customer(id)` with `ON DELETE RESTRICT`
   
   2.4 Data backfill strategy (if existing data)
   - [ ] Plan/data migration to backfill `customer_id` for existing rows (placeholder customer if needed)
   - [ ] Follow-up migration to set `customer_id` NOT NULL after backfill (if required)

3. JPA Entities and Mappings
   3.1 Create Customer entity
   - [x] Create `Customer` entity (placed under `guru.springframework.juniemvc.entities` per project convention)
   - [x] Map all fields with appropriate `@Column(nullable = ...)`
   - [x] Implement `@OneToMany(mappedBy = "customer")` to orders
   - [x] Define `equals`/`hashCode` consistent with project strategy
   
   3.2 Update BeerOrder entity
   - [x] Add `@ManyToOne(fetch = LAZY)` to `Customer` with `@JoinColumn(name = "customer_id")`
   - [x] Verify no direct serialization of entities in web layer (DTOs only)

4. Repositories
   - [x] Create `CustomerRepository extends JpaRepository<Customer, IdType>`
   - [ ] Add useful derived queries only if needed later (e.g., `findByEmail`) — Not needed now

5. DTOs and Validation
   - [x] Create package `guru.springframework.juniemvc.models.customer` (or `dto`)
   - [x] Define `CustomerRequest` with validation: `@NotBlank` for required fields; `@Email` for email; optional phone regex if policy
   - [x] Define `CustomerResponse` with id + full fields
   - [ ] (Optional) Define `CustomerSummaryResponse` for list views
   - [x] Ensure JSON naming uses camelCase consistently

6. Mapping (MapStruct)
   - [x] Create `CustomerMapper` in `guru.springframework.juniemvc.mappers`
   - [x] Map `Customer` ↔ `CustomerResponse`
   - [x] Map `CustomerRequest` → `Customer`
   - [x] Provide partial update method using `@MappingTarget` or document service-level merge approach

7. Service Layer and Transactions
   - [x] Define `CustomerService` interface with CRUD methods
   - [x] Implement `CustomerServiceImpl` using constructor injection
   - [x] Annotate methods: `@Transactional` for create/update/delete, `@Transactional(readOnly = true)` for get/list
   - [x] Implement business rule: NotFound on missing id
   - [x] (Optional) Prevent deleting customers with existing orders (service check and/or DB constraint); define and throw conflict exception if applicable — Implemented delete guard with 409 mapping

8. REST Controller (Versioned, Resource-Oriented)
   - [x] Create `CustomerController` under path `/api/v1/customers`
   - [x] Make controller and handler methods package-private where possible
   - [x] Implement endpoints with proper `ResponseEntity` statuses:
     - [x] POST `/api/v1/customers` → 201 Created + Location header
     - [x] GET `/api/v1/customers/{id}` → 200 OK
     - [x] GET `/api/v1/customers` (pagination) → 200 OK with page metadata
     - [x] PUT `/api/v1/customers/{id}` → 200 OK (or 204 No Content)
     - [x] PATCH `/api/v1/customers/{id}` (optional) → 200 OK
     - [x] DELETE `/api/v1/customers/{id}` → 204 No Content (or 409 if has orders)
   - [x] Add `@Valid` on request DTOs and rely on global exception handling for validation errors

9. OpenAPI Documentation
   - [x] Update `openapi/openapi/openapi.yaml` to add path refs:
     - [x] `'/customers': $ref: 'paths/customers.yaml'` (paths are relative to server base `/api/v1` per repo convention)
     - [x] `'/customers/{id}': $ref: 'paths/customers_{id}.yaml'`
   - [x] Create/reuse component schemas:
     - [x] `CustomerRequest.yaml`
     - [x] `CustomerResponse.yaml`
     - [ ] (Optional) `CustomerSummaryResponse.yaml`
   - [x] Define operations with success responses and reuse `Problem.yaml` for errors
   - [x] Include pagination parameters/headers where applicable
   - [ ] Validate with Redocly: from `openapi` dir run `npm ci` (first time) and `npm test`

10. Testing Strategy
   10.1 Unit tests
   - [ ] Mapper tests (if custom logic present)
   - [ ] Service tests for happy paths and error scenarios (NotFound, conflict on delete)
   
   10.2 Web layer tests
   - [ ] `@WebMvcTest` controller tests with mocked service; verify status codes, validation, response shapes
   
   10.3 Integration tests
   - [ ] `@SpringBootTest(webEnvironment = RANDOM_PORT)`
   - [ ] Configure Testcontainers database; ensure Flyway migrations run
   - [ ] Seed sample data; verify CRUD, pagination, and constraints end-to-end
   
   10.4 Repository tests (optional)
   - [ ] Basic CRUD and derived queries with Testcontainers (if not covered by integration tests)

11. Configuration and Cross-cutting Concerns
   - [x] Ensure `spring.jpa.open-in-view=false` in application properties
   - [x] Verify Flyway dependency and that migrations are discovered at `classpath:db/migration`
   - [x] Confirm global `@RestControllerAdvice` maps validation errors and NotFound to ProblemDetails (also maps 409 Conflict)
   - [ ] Add new exception types only if necessary (e.g., `CustomerHasOrdersException` → 409) — Using `IllegalStateException` currently
   - [ ] Review logging to avoid sensitive data; guard expensive debug logs
   - [ ] (Optional) Externalize validation messages to `messages.properties`

12. Security and Actuator (Contextual)
   - [ ] Confirm actuator exposure: only `/health`, `/info`, `/metrics` unauthenticated; others secured
   - [ ] If security is enabled, add authorization rules for `/api/v1/customers/**`

13. Migration and Backward Compatibility
   - [ ] If existing `beer_order` data exists, follow phased approach:
     - [ ] Step 1: Add `customer_id` nullable + FK
     - [ ] Step 2: Backfill values
     - [ ] Step 3: Enforce NOT NULL
   - [ ] Communicate downtime/maintenance window if required

14. Performance and Pagination
   - [x] Implement pagination using `Pageable` in list endpoint
   - [x] Add indexes on `city`, `state`, `email` if filtering requires

15. Delivery Checklist (Final Verification)
   - [ ] Flyway migration added and executed successfully
   - [x] JPA entity and repository created; relationships verified
   - [ ] MapStruct mapper implemented and tested — Implemented; tests pending
   - [x] DTOs with validation annotations
   - [x] Service layer with transactions and business rules
   - [x] REST controller with versioned URLs and correct status codes
   - [ ] OpenAPI paths and schemas added; Redocly lint passes — Added; lint pending
   - [ ] Unit and integration tests (Testcontainers, RANDOM_PORT) are green
   - [x] Application properties updated (OSIV disabled)
   - [x] Global exception handling covers new cases
   - [x] Controllers do not expose entities directly; only DTOs
   - [ ] Logging and i18n considerations addressed
