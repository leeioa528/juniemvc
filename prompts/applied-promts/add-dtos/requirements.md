# Add DTOs and Mappers for Beer API

## Goal
Refactor the Beer API to decouple the web layer from the JPA persistence model by introducing DTOs and MapStruct mappers. Controllers must no longer expose or accept JPA entities. Services should operate on DTOs and use mappers for conversions.

## Scope
- Introduce a Beer DTO and use it across controller and service layers.
- Add MapStruct mapper(s) to convert between entity and DTO.
- Update service and controller method signatures and implementations to use DTOs.
- Add validation for request payloads and centralized error handling.
- Keep existing API resource paths and semantics intact.

## Non‑Goals
- No database schema or entity field changes.
- No new endpoints beyond what already exists.
- No pagination or filtering changes in this iteration.

## Current State Summary
- Entity: `guru.springframework.juniemvc.entities.Beer`.
- Controller: `BeerController` exposes/consumes `Beer` directly.
- Service: `BeerService` operates on `Beer`.
- MapStruct and Lombok are already present in `pom.xml`. Default MapStruct component model is `spring` via compiler arg.

## Packages
- DTOs: `guru.springframework.juniemvc.models`
- Mappers: `guru.springframework.juniemvc.mappers`

Follow package-private visibility for Spring components where possible (controllers, configs, bean methods) per guidelines.

## DTO Design
Create `BeerDto` in `guru.springframework.juniemvc.models` with the following fields mirroring the entity schema.

Fields:
- `Integer id` (read-only for responses; must be ignored on create/update input)
- `Integer version` (read-only)
- `String beerName`
- `String beerStyle`
- `String upc`
- `Integer quantityOnHand`
- `BigDecimal price`
- `LocalDateTime createdDate` (read-only)
- `LocalDateTime updateDate` (read-only)

Annotations and structure:
- Use Lombok: `@Getter`, `@Setter`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`.
- Prefer `record` is acceptable, but for alignment with Lombok builder use a class with the above annotations.
- Add Jakarta Validation annotations for input constraints on mutable business fields:
  - `@NotBlank` for `beerName`, `beerStyle`, `upc`.
  - `@PositiveOrZero` for `quantityOnHand`.
  - `@DecimalMin(value = "0.0", inclusive = true)` for `price`.
- Do not apply validation to id/version/createdDate/updateDate (they are server managed).

Note: We will use the same DTO type for requests and responses, but treat server-managed fields as read-only. Controllers must ignore client-provided values for these fields.

## MapStruct Mapper
Create `BeerMapper` in `guru.springframework.juniemvc.mappers`.

- Interface annotated with `@Mapper` (componentModel inherited from compiler arg is `spring`).
- Methods:
  - `BeerDto toDto(Beer source);`
  - `Beer toEntity(BeerDto source);` with mapping rules:
    - Ignore `id`, `version`, `createdDate`, `updateDate` when mapping from DTO to entity for create operations.
      Use `@Mapping(target="id", ignore=true)`, etc.
  - `void updateEntityFromDto(BeerDto source, @MappingTarget Beer target);`
    - Also ignore `id`, `version`, `createdDate`, `updateDate`.
- Null handling: rely on MapStruct defaults (null source leaves target as-is for update method).
- If needed, add type conversions for `BigDecimal` (none expected, JSON number maps to `BigDecimal`).

## Service Layer Changes
Update `BeerService` interface to operate on DTOs instead of entities.

Replace existing signatures with:
- `List<BeerDto> getAllBeers();`
- `Optional<BeerDto> getBeerById(Integer id);`
- `BeerDto createBeer(BeerDto beerDto);`  // create new beer; ignore client id/version/dates
- `Optional<BeerDto> updateBeer(Integer id, BeerDto beerDto);` // full update (PUT semantics)
- `boolean deleteBeerById(Integer id);` // return true if deleted, false if not found

Implementation (`BeerServiceImpl`):
- Inject `BeerRepository` and `BeerMapper` via constructor injection (final fields).
- `getAllBeers`: fetch entities, map to DTOs.
- `getBeerById`: fetch entity, map to DTO.
- `createBeer`: map DTO to new entity (mapper ignores id/version/dates), save, map saved entity to DTO.
- `updateBeer`:
  - Find entity by id; if absent, return `Optional.empty()`.
  - Use `updateEntityFromDto` to copy mutable fields; save; return mapped DTO.
- `deleteBeerById`: check existence, delete if present.
- Annotate service methods with `@Transactional`:
  - Read queries with `@Transactional(readOnly = true)`.
  - Writes with `@Transactional`.

## Controller Changes
Update `BeerController` to use `BeerDto` exclusively.

- Class-level:
  - Keep request mapping: `/api/v1/beers`.
  - Set `@RestController`.
  - Set consumes/produces at method-level as needed: `produces = MediaType.APPLICATION_JSON_VALUE` and for write methods `consumes = MediaType.APPLICATION_JSON_VALUE`.

- Endpoints:
  - `GET /api/v1/beers` → `List<BeerDto>` (200 OK)
  - `GET /api/v1/beers/{id}` → `ResponseEntity<BeerDto>` (200 OK or 404)
  - `POST /api/v1/beers` → `ResponseEntity<BeerDto>` (201 Created)
    - Validate request: add `@Valid` to `@RequestBody BeerDto`.
    - Ignore client-sent `id`, `version`, `createdDate`, `updateDate`.
    - Return `Location` header: `/api/v1/beers/{id}`.
  - `PUT /api/v1/beers/{id}` → `ResponseEntity<BeerDto>` (200 OK or 404)
    - Validate request body with `@Valid`.
  - `DELETE /api/v1/beers/{id}` → `ResponseEntity<Void>` (204 No Content or 404)

- Constructor injection for `BeerService` (final field). Prefer package-private class visibility.

## Validation
- Enable validation by keeping `spring-boot-starter-validation` (already present).
- In controller method parameters, annotate request DTOs with `@Valid`.
- For constraint violations, rely on global exception handling to return a structured error.

## Exception Handling
Add a global exception handler using `@RestControllerAdvice`:
- Handle `NoSuchElementException` or a custom `NotFoundException` for 404 responses.
- Handle `MethodArgumentNotValidException` to return 400 with a problem details body listing field errors.
- Use Spring 6 `ProblemDetail` as the response format to align with RFC 9457.
- Provide consistent error payloads including `type`, `title`, `status`, `detail`, and `instance`.

## Configuration
- Disable Open Session In View to avoid lazy loading during serialization:
  - In `application.properties`: `spring.jpa.open-in-view=false`.
- Keep existing H2 dev setup.

## Logging
- Use SLF4J for logging, avoid `System.out`.
- Guard expensive debug logs with `logger.isDebugEnabled()` or `atDebug()` style.
- Do not log sensitive data.

## Tests and Migration
- Update existing tests to use `BeerDto` in controller and service layers.
- Controller tests: assert response status codes, content type, and JSON body structure; verify `Location` header on POST.
- Service tests: verify mapping is applied (can be indirect via repository interactions) and business behavior remains correct.
- Mapper tests (optional but recommended): simple unit tests for `BeerMapper` conversions.
- No integration test changes to database configuration are required in this iteration.

## Backward Compatibility Notes
- JSON structure remains similar; read-only fields may now be present but ignored on input.
- Ensure API responses maintain the same field names so clients are unaffected.

## Acceptance Criteria
- A new `BeerDto` exists under `guru.springframework.juniemvc.models` with Lombok builder and validation annotations.
- A `BeerMapper` exists under `guru.springframework.juniemvc.mappers` with methods: `toDto`, `toEntity` (ignoring server-managed fields), and `updateEntityFromDto`.
- `BeerService` interface and `BeerServiceImpl` use DTOs in their signatures and use the mapper internally.
- `BeerController` exposes DTOs on all endpoints, validates request bodies, and sets `Location` on POST.
- Global exception handling returns ProblemDetail responses for validation errors and not found cases.
- `spring.jpa.open-in-view=false` is configured.
- All tests compile and pass after updates; existing functionality (CRUD) remains intact.

## Definition of Done
- Code follows constructor injection and package-private visibility where applicable.
- No controllers or services expose JPA entities in public APIs.
- DTOs are used consistently across the web and service layers.
- Clear transaction boundaries with `@Transactional` annotations are defined in service methods.
- Error responses are standardized using Spring `ProblemDetail`.
- Logging follows best practices; no sensitive data in logs.
