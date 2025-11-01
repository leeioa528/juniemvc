# Add DTOs and MapStruct Mapping for Beer API

## 1. Objective
Replace direct exposure of the JPA entity `Beer` in the web layer with a dedicated DTO, and introduce a MapStruct mapper to convert between the entity and DTO. Update the controller and service layers to use the DTO end-to-end.

## 2. Scope
- Introduce `BeerDto` in package `guru.springframework.juniemvc.models`.
- Introduce `BeerMapper` in package `guru.springframework.juniemvc.mappers`.
- Update `BeerService` and `BeerServiceImpl` to accept/return `BeerDto` where appropriate and perform conversions via `BeerMapper`.
- Update `BeerController` to consume/produce `BeerDto`.
- Keep API URL structure the same: `/api/v1/beers`.

Non-goals (for this change set):
- Introducing new persistence fields or changing database schema.
- Implementing pagination or search (may be added later).
- Introducing command objects (acceptable future improvement; keep DTOs for now per draft).

## 3. Design & Conventions (aligned with the Spring Boot Guidelines)
- Constructor injection for all Spring components; fields should be `final` when possible.
- Prefer package-private visibility for Spring components and controller handler methods unless public visibility is required.
- Separate web and persistence layers: controllers must use DTOs, not entities.
- Use Jakarta Bean Validation annotations on request DTOs.
- Use `ResponseEntity<T>` and explicit HTTP status codes.
- Disable Open Session In View if not already disabled in application properties (recommended: `spring.jpa.open-in-view=false`).

## 4. DTO Definition
Package: `guru.springframework.juniemvc.models`

Class: `BeerDto`
- Lombok annotations: `@Getter`, `@Setter`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`.
- Fields (mirror the entity’s public-facing data):
  - `Integer id` (read-only from clients; server-generated)
  - `String beerName` — `@NotBlank`
  - `String beerStyle` — `@NotBlank`
  - `String upc` — `@NotBlank`
  - `Integer quantityOnHand` — `@PositiveOrZero`
  - `BigDecimal price` — `@DecimalMin("0.0")` (scale/format left to JSON serialization)
  - `LocalDateTime createdDate` (read-only)
  - `LocalDateTime updateDate` (read-only)

Notes:
- Clients must not set `id`, `createdDate`, or `updateDate` on create/update; these are generated/managed by the server.
- If the field `version` exists on the entity (it does), it is not part of the DTO contract and must not be set by clients.

## 5. MapStruct Mapper
Package: `guru.springframework.juniemvc.mappers`

Interface: `BeerMapper`
- Component model: Spring (already configured in pom via `-Amapstruct.defaultComponentModel=spring`).
- Methods:
  - `BeerDto toDto(Beer entity);`
  - `Beer toEntity(BeerDto dto);`
  - `void updateEntityFromDto(BeerDto dto, @MappingTarget Beer entity);`

Mapping rules:
- While mapping from DTO to Entity on create/update, ignore the following entity fields: `id`, `createdDate`, `updateDate`, and `version`.
  - Implementation: use `@Mapping(target = "id", ignore = true)`, `@Mapping(target = "createdDate", ignore = true)`, `@Mapping(target = "updateDate", ignore = true)`, `@Mapping(target = "version", ignore = true)` on the `toEntity` and `updateEntityFromDto` methods.
- For null handling during updates, prefer MapStruct’s `NullValuePropertyMappingStrategy.IGNORE` to avoid overwriting existing values with nulls when performing partial updates (if partial updates are introduced later). For now, for PUT, the controller will provide full DTOs.

## 6. Service Layer Changes
Interfaces and implementations in `guru.springframework.juniemvc.services` should migrate from entity types to DTOs at the boundaries used by the controller. Internally, the service may use entities for persistence and convert using the mapper.

Update `BeerService` to:
- `List<BeerDto> getAllBeers();`
- `Optional<BeerDto> getBeerById(Integer id);`
- `BeerDto createBeer(BeerDto beerDto);` — Ignores any client-provided `id`, sets it to null before persisting.
- `Optional<BeerDto> updateBeer(Integer id, BeerDto beerDto);` — Returns empty if entity not found; otherwise updates persistent entity with mapped fields and returns updated DTO.
- `boolean deleteBeerById(Integer id);` — Returns false if not found, true if deleted.

Implementation (`BeerServiceImpl`):
- Inject `BeerRepository` and `BeerMapper` via constructor injection.
- For create:
  - Map DTO to entity with ignored fields, ensure `id = null`.
  - Save via repository and map back to DTO.
- For update:
  - Find entity; if present, map fields from DTO to entity (respect ignore rules), save, and map back to DTO.
- For read methods: map entities to DTOs.

Transactional boundaries:
- Annotate write methods (`createBeer`, `updateBeer`, `deleteBeerById`) with `@Transactional`.
- Annotate read methods with `@Transactional(readOnly = true)`.

## 7. Controller Changes
Class: `guru.springframework.juniemvc.controllers.BeerController`
- Continue to expose `/api/v1/beers`.
- Accept and return `BeerDto` instead of `Beer`.
- Use `ResponseEntity` and appropriate status codes.
- Prefer package-private controller and handler methods where Spring allows.

Endpoints:
- `GET /api/v1/beers` → `200 OK` with `List<BeerDto>`
- `GET /api/v1/beers/{id}` → `200 OK` with `BeerDto` if found; `404 Not Found` otherwise
- `POST /api/v1/beers` → `201 Created` with created `BeerDto`; include `Location: /api/v1/beers/{id}` header
  - On validation errors, return `400 Bad Request` with details
- `PUT /api/v1/beers/{id}` → `200 OK` with updated `BeerDto` if found; `404 Not Found` otherwise
- `DELETE /api/v1/beers/{id}` → `204 No Content` if deleted; `404 Not Found` otherwise

Validation:
- Annotate controller methods with `@Valid` on `@RequestBody BeerDto` to trigger bean validation.

Sample JSON (request for create):
```
{
  "beerName": "Test Beer",
  "beerStyle": "IPA",
  "upc": "123456",
  "quantityOnHand": 100,
  "price": 12.99
}
```

Sample JSON (response for read):
```
{
  "id": 1,
  "beerName": "Test Beer",
  "beerStyle": "IPA",
  "upc": "123456",
  "quantityOnHand": 100,
  "price": 12.99,
  "createdDate": "2025-10-31T21:27:00",
  "updateDate": "2025-10-31T22:05:00"
}
```

## 8. Global Exception Handling
- If not already present, add `guru.springframework.juniemvc.handlers.GlobalExceptionHandler` annotated with `@RestControllerAdvice` to centralize validation and not-found handling.
- For validation errors, return a consistent error body (consider RFC 9457 Problem Details via Spring’s `ProblemDetail`).

## 9. Logging
- Use SLF4J (via Lombok `@Slf4j` if preferred). Do not use `System.out.println`.
- Avoid logging sensitive data.

## 10. Testing Requirements
- Update `BeerControllerTest` to work with `BeerDto` JSON payloads and responses.
  - Mock `BeerService` methods that now operate on DTOs.
  - Verify status codes and response shapes as above.
- Add focused unit tests for `BeerMapper` (optional but recommended) to verify ignore rules and field mappings.
- Service layer tests (optional): verify create/update flows map and persist correctly.

## 11. Build/Tooling Notes
- `pom.xml` already configures MapStruct and Lombok annotation processors and sets `defaultComponentModel=spring`.
- Ensure application disables OSIV if you adopt fetch strategies explicitly: `spring.jpa.open-in-view=false` (recommended, but not mandatory for this task).

## 12. Acceptance Criteria
- `BeerController` no longer exposes or accepts the JPA entity; it exclusively uses `BeerDto` in request/response bodies.
- `BeerService` interface and implementation updated to accept/return `BeerDto` at the controller boundary.
- `BeerMapper` exists with methods and ignore rules specified; component model is Spring.
- `BeerDto` exists with Lombok, validation annotations, and read-only semantics for `id`, `createdDate`, `updateDate` (ignored from client input).
- All controller endpoints compile and pass existing tests updated to use DTOs, with correct HTTP status codes and `Location` header on create.
- Project builds successfully with `mvn clean test`.

## 13. Migration Notes
- Replace imports and method signatures from `Beer` to `BeerDto` in controller and service interfaces.
- Use `BeerMapper` within `BeerServiceImpl` to convert between entity and DTO.
- Keep repository unchanged (`BeerRepository` still uses the entity).
- Update tests and any JSON samples in README if present.
