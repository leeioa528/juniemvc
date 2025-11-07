# Project Improvement Plan — Add Customer Domain and CRUD API

This plan converts the high‑level change requirements into a concrete, step‑by‑step roadmap. It follows the provided Spring Boot guidelines (constructor injection, package‑private components where possible, DTO separation, Flyway migrations, REST conventions, centralized exception handling, Testcontainers, random ports in tests, and OpenAPI authoring practices).

## 1. Domain and Data Model
1.1 Define the Customer aggregate (root entity) with fields and constraints per requirements:
- name (not null)
- email (nullable)
- phoneNumber (nullable)
- addressLine1 (not null)
- addressLine2 (nullable)
- city (not null)
- state (not null)
- postalCode (not null)

1.2 Establish association to orders:
- One Customer has many BeerOrders (OneToMany). Existing `BeerOrder` links back to `Customer` via `@ManyToOne`.
- Choose non‑cascading from Customer → BeerOrder unless business rules dictate otherwise (avoid accidental deletes of orders). Use `orphanRemoval = false`.

1.3 Keys and identity:
- Use `UUID` (or Long, matching existing project conventions) as primary key type. Prefer consistency with existing `BeerOrder` id type.

## 2. Database Schema with Flyway
2.1 Create versioned migration under `src/main/resources/db/migration`:
- `V<next>__add_customer.sql` (respect current numbering).

2.2 DDL contents:
- Create `customer` table with columns for all fields and standard auditing columns if used in project (e.g., `created_date`, `last_modified_date`).
- Add not‑null constraints on required fields.
- Add indexes as needed (e.g., unique or non‑unique on `email` if business requires lookup; otherwise a non‑unique index can help searching). Do not enforce email uniqueness unless specified.

2.3 Update `beer_order` table to include `customer_id` FK if not present:
- Add nullable/required constraint based on whether legacy orders can exist without a customer. Prefer NOT NULL for new systems; if existing data exists, add as nullable then backfill and set NOT NULL in a follow‑up migration.
- Add foreign key constraint with `ON DELETE RESTRICT`.

2.4 Data backfill strategy (if migrating existing data):
- Optional migration to backfill `customer_id` for existing `beer_order` rows (e.g., map to a placeholder customer) and then enforce NOT NULL.

## 3. JPA Entities and Mappings
3.1 Create `Customer` JPA entity in `guru.springframework.juniemvc.domain` (or the project’s entity package):
- Fields reflecting schema. Validation annotations can be on DTOs; entity constraints remain minimal (e.g., column nullability).
- Bidirectional mapping: `@OneToMany(mappedBy = "customer") private Set<BeerOrder> orders = new HashSet<>();`
- Ensure equals/hashCode use immutable business keys or identifier patterns consistent with existing entities.

3.2 Update `BeerOrder` entity:
- Add `@ManyToOne(fetch = LAZY)` to `Customer` with `@JoinColumn(name = "customer_id")`.
- Verify serialization protection (controllers must not leak entities; use DTOs only).

## 4. Repositories
4.1 Create Spring Data JPA repository interfaces:
- `CustomerRepository extends JpaRepository<Customer, IdType>`
- Useful queries (if needed): find by email, city, etc. Start with basics and add on demand.

## 5. DTOs and Validation (Separation of Web and Persistence)
5.1 Define request/response DTOs under `guru.springframework.juniemvc.models.customer` (or `dto`):
- `CustomerRequest` (for create/update) with Jakarta Validation:
  - `@NotBlank` name, addressLine1, city, state, postalCode
  - `@Email` email (optional when present)
  - Optional regex for phone if policy exists
- `CustomerResponse` for reads with id and full fields
- Optional `CustomerSummaryResponse` for list views (id, name, city/state)

5.2 Ensure JSON uses consistent naming (camelCase across the API).

## 6. Mapping
6.1 Use MapStruct mappers (preferred) in `guru.springframework.juniemvc.mappers`:
- `CustomerMapper` for `Customer <-> CustomerResponse` and `CustomerRequest -> Customer`.
- Handle partial updates: either a dedicated `@MappingTarget` method or service‑level merge logic.

## 7. Service Layer and Transactions
7.1 Create `CustomerService` interface and `CustomerServiceImpl`:
- Constructor injection for dependencies; mark fields as `final`.
- Methods as transactional boundaries:
  - `@Transactional` for create, update, delete
  - `@Transactional(readOnly = true)` for get/list
- Methods:
  - `CustomerResponse create(CustomerRequest)`
  - `CustomerResponse update(IdType id, CustomerRequest)`
  - `CustomerResponse get(IdType id)`
  - `Page<CustomerResponse> list(Pageable)` (or list with filters)
  - `void delete(IdType id)` (consider soft delete if policy applies)

7.2 Business rules:
- Validate existence; throw `NotFoundException` (or project’s standard) when missing.
- Optional: prevent deleting customers with orders (enforce at service and/or via DB constraints).

## 8. REST Controller (Versioned, Resource‑oriented)
8.1 Create `CustomerController` under `/api/v1/customers`:
- Package‑private controller and handler methods where possible.
- Endpoints returning `ResponseEntity<...>` with proper statuses:
  - POST `/api/v1/customers` → 201 Created + `Location` header
  - GET `/api/v1/customers/{id}` → 200 OK
  - GET `/api/v1/customers` (pagination) → 200 OK with page metadata headers or body fields
  - PUT `/api/v1/customers/{id}` → 200 OK (or 204 No Content)
  - PATCH `/api/v1/customers/{id}` (optional) → 200 OK
  - DELETE `/api/v1/customers/{id}` → 204 No Content (or 409 if has orders, based on rule)

8.2 Request validation:
- Annotate method params with `@Valid` for request DTOs; return validation errors through global exception handler using RFC 9457 ProblemDetails format if available in the project.

## 9. OpenAPI Documentation
9.1 Update `openapi/openapi/openapi.yaml`:
- Add path refs using flat underscore scheme under `openapi/openapi/paths`:
  - `'/api/v1/customers': $ref: 'paths/api_v1_customers.yaml'`
  - `'/api/v1/customers/{id}': $ref: 'paths/api_v1_customers_{id}.yaml'`

9.2 Add/reuse schemas under `openapi/openapi/components/schemas`:
- `CustomerRequest.yaml`, `CustomerResponse.yaml`, `CustomerSummaryResponse.yaml` (PascalCase filenames)
- Reference with `../components/schemas/*.yaml` from path files.

9.3 Define operations with responses:
- Success (200/201/204) and standardized error using existing `Problem.yaml`.
- Include pagination parameters and headers if applicable.

9.4 Validate with Redocly:
- From `openapi` folder: `npm ci` (first time) and `npm test`.

## 10. Testing Strategy
10.1 Unit tests:
- Mappers (fast MapStruct tests if logic present)
- Service layer: happy paths and error scenarios (NotFound, conflict on delete if enforced)

10.2 Web layer tests:
- `@WebMvcTest` for controller slice tests using mocked service; validate status codes, validation errors, and response shapes.

10.3 Integration tests:
- `@SpringBootTest(webEnvironment = RANDOM_PORT)`
- Use Testcontainers for the database to run Flyway migrations and verify repository + service + controller paths end‑to‑end.
- Seed sample data per test and verify CRUD, pagination, and constraints.

10.4 Repository tests (optional if covered by integration):
- Basic CRUD and query method behavior with Testcontainers.

## 11. Configuration and Cross‑cutting Concerns
11.1 Properties:
- Ensure `spring.jpa.open-in-view=false`.
- Add Flyway if not present in dependencies; ensure migrations are discovered under `classpath:db/migration`.

11.2 Exception handling:
- Reuse existing `@RestControllerAdvice` to map validation errors and `NotFoundException` to consistent ProblemDetails.
- Add new exception types only if necessary (e.g., `CustomerHasOrdersException` → 409 Conflict).

11.3 Logging:
- Use SLF4J; guard expensive debug logs; avoid sensitive data in logs (emails/phones masked if logged).

11.4 Internationalization (optional):
- Externalize validation messages to `messages.properties` and reference with `{key}` from annotations for future i18n.

## 12. Security and Actuator (Contextual)
- Confirm actuator exposure per guideline: only `/health`, `/info`, `/metrics` unauthenticated; secure others.
- If security is enabled in the project, add authorization rules for `/api/v1/customers/**` consistent with existing policies.

## 13. Migration and Backward Compatibility
- If existing `BeerOrder` records exist, plan a phased migration:
  1) Add `customer_id` nullable + FK
  2) Backfill values
  3) Enforce NOT NULL
- Communicate downtime/maintenance windows if required.

## 14. Performance and Pagination
- Implement pagination for list endpoint using `Pageable`.
- Consider indexes on `city`, `state`, `email` if queries will filter on these fields.

## 15. Delivery Checklist
- Flyway migration added and executed successfully
- JPA entity and repository created; relationships verified
- MapStruct mapper implemented and tested
- DTOs with validation annotations
- Service layer with transactions and rules
- REST controller with versioned URLs and proper status codes
- OpenAPI paths and schemas added; Redocly lint passes
- Unit and integration tests (Testcontainers, RANDOM_PORT) are green
- Application properties updated (OSIV disabled)
- Global exception handling covers new cases
- No direct entity exposure in controllers; only DTOs in/out
- Logging and i18n considerations addressed
