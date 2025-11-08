# Plan: Introduce DTOs and MapStruct to decouple Web and Persistence layers

Date: 2025-10-26 15:54

## 1. Objectives
- Decouple controllers/services from JPA entities by introducing `BeerDto` and MapStruct `BeerMapper`.
- Apply Spring Boot guidelines: constructor injection, package-private visibility where possible, clear transaction boundaries, OSIV disabled, centralized exception handling using `ProblemDetail`.
- Maintain backward-compatible API semantics and paths.
- Update tests to reflect the DTO-based API.

## 2. Current State (from repo)
- Entity: `guru.springframework.juniemvc.entities.Beer`
- Web: `BeerController` exposes/consumes `Beer` entity directly
- Service: `BeerService`, `BeerServiceImpl` operate on `Beer`
- Repo: `BeerRepository` (Spring Data JPA)
- Build: `pom.xml` already includes Lombok and MapStruct (compiler arg `-Amapstruct.defaultComponentModel=spring`).
- Tests exist for controller, service, repository.

## 3. Design Summary
- New DTO: `guru.springframework.juniemvc.models.BeerDto`
  - Fields: `id`, `version`, `beerName`, `beerStyle`, `upc`, `quantityOnHand`, `price`, `createdDate`, `updateDate`
  - Lombok: `@Getter`, `@Setter`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`
  - Validation: `@NotBlank` on name/style/upc, `@PositiveOrZero` on quantity, `@DecimalMin("0.0", inclusive = true)` on price
  - Treat `id`, `version`, `createdDate`, `updateDate` as server-managed (ignored on input)
- Mapper: `guru.springframework.juniemvc.mappers.BeerMapper`
  - `BeerDto toDto(Beer)`
  - `Beer toEntity(BeerDto)` — ignore server-managed fields
  - `void updateEntityFromDto(BeerDto, @MappingTarget Beer)` — ignore server-managed fields
- Service API (DTO-based):
  - `List<BeerDto> getAllBeers()`
  - `Optional<BeerDto> getBeerById(Integer id)`
  - `BeerDto createBeer(BeerDto beerDto)`
  - `Optional<BeerDto> updateBeer(Integer id, BeerDto beerDto)`
  - `boolean deleteBeerById(Integer id)`
  - Transactions: read methods `@Transactional(readOnly = true)`, writes `@Transactional`
- Controller uses DTOs only; validates request bodies; sets `Location` on POST.
- Global exception handling via `@RestControllerAdvice`, returning `ProblemDetail` for validation errors, not found.

## 4. Detailed Work Plan

### Phase A — Preparation
1. Create package structure if missing:
   - `guru.springframework.juniemvc.models`
   - `guru.springframework.juniemvc.mappers`
2. Verify `pom.xml`:
   - Lombok dependency present.
   - MapStruct processor and `-Amapstruct.defaultComponentModel=spring` compiler arg present.
   - `spring-boot-starter-validation` present.
3. Add `spring.jpa.open-in-view=false` in `src/main/resources/application.properties`.

### Phase B — Implement DTOs and Mapper
4. Implement `BeerDto` per Design Summary.
5. Implement `BeerMapper` interface with mappings:
   - `@Mapping(target = "id", ignore = true)`
   - `@Mapping(target = "version", ignore = true)`
   - `@Mapping(target = "createdDate", ignore = true)`
   - `@Mapping(target = "updateDate", ignore = true)`
   - Apply above ignores to both `toEntity` and `updateEntityFromDto`.

### Phase C — Refactor Service Layer to DTOs
6. Update `BeerService` signatures to DTO-based API.
7. Refactor `BeerServiceImpl`:
   - Inject `BeerRepository` and `BeerMapper` via constructor (final fields).
   - Map entities to DTOs in read methods.
   - For create: map DTO -> entity (mapper ignores server-managed fields), save, map saved entity -> DTO.
   - For update: fetch, if present use `updateEntityFromDto`, save, return DTO; otherwise return `Optional.empty()`.
   - For delete: check existence, delete if present; return boolean.
   - Add `@Transactional` annotations per method (readOnly for queries).
   - Keep class package-private if feasible; methods public per interface.

### Phase D — Refactor Controller to DTOs
8. Adjust `BeerController`:
   - Change method signatures and types to use `BeerDto`.
   - Annotate request payloads with `@Valid`.
   - For POST: return `201 Created` with `Location: /api/v1/beers/{id}` and body of created `BeerDto`.
   - For GET by id: return `200 OK` with body or `404 Not Found` if empty.
   - For PUT: `200 OK` with updated DTO or `404 Not Found`.
   - For DELETE: `204 No Content` or `404 Not Found`.
   - Keep mapping base `/api/v1/beers`.
   - Use constructor injection for `BeerService` (final field). Prefer package-private class visibility.

### Phase E — Centralized Exception Handling
9. Add `GlobalExceptionHandler` with `@RestControllerAdvice`:
   - Handle `MethodArgumentNotValidException` → `400 Bad Request` with `ProblemDetail` including field error details.
   - Handle not found scenarios: if using `Optional` in controller, may not throw; alternatively map an explicit `NoSuchElementException`/`NotFoundException` to 404 for future use.
   - Provide consistent error payload fields: `type`, `title`, `status`, `detail`, `instance`.

### Phase F — Logging and Hygiene
10. Ensure logging uses SLF4J; remove any `System.out.println` usage.
11. Guard expensive debug logs with `logger.isDebugEnabled()` or `atDebug()` style.
12. Do not log sensitive data.

### Phase G — Tests Migration and Additions
13. Update Service tests:
    - Adjust to DTO-based signatures and behaviors.
    - Verify create/update map values appropriately.
14. Update Controller tests:
    - Validate status codes, content type `application/json`.
    - Verify JSON structure uses DTO fields.
    - On POST, assert `Location` header.
15. Mapper tests (optional but recommended):
    - Simple unit test to ensure `toDto`, `toEntity`, and `updateEntityFromDto` work for typical and null cases.
16. Repository tests remain unchanged.
17. Run test suite; fix compile errors from signature changes.

### Phase H — Documentation and Cleanup
18. Update `README.md` (short section) to state that API uses DTOs and MapStruct.
19. Ensure package-private visibility where safe (controllers, configs, bean methods) per guidelines.
20. Re-run build; ensure no warnings from MapStruct/Lombok; ensure all tests pass.

## 5. File-by-File Change Outline
- src/main/java/guru/springframework/juniemvc/models/BeerDto.java (new)
- src/main/java/guru/springframework/juniemvc/mappers/BeerMapper.java (new)
- src/main/java/guru/springframework/juniemvc/services/BeerService.java (edit signatures)
- src/main/java/guru/springframework/juniemvc/services/BeerServiceImpl.java (refactor implementation)
- src/main/java/guru/springframework/juniemvc/controllers/BeerController.java (use DTOs; validation; status codes; Location header)
- src/main/java/guru/springframework/juniemvc/handlers/GlobalExceptionHandler.java (new)
- src/main/resources/application.properties (add `spring.jpa.open-in-view=false`)
- Tests under `src/test/java/...` adjusted accordingly.

## 6. API Behavior Details
- GET /api/v1/beers → 200 OK, body: `List<BeerDto>`
- GET /api/v1/beers/{id} → 200 OK with body, or 404 if not found
- POST /api/v1/beers → 201 Created; body: created `BeerDto`; header `Location: /api/v1/beers/{id}`
- PUT /api/v1/beers/{id} → 200 OK with updated DTO; 404 if id not found
- DELETE /api/v1/beers/{id} → 204 No Content; 404 if id not found

## 7. Validation Rules (Controller Input)
- `beerName`, `beerStyle`, `upc` → `@NotBlank`
- `quantityOnHand` → `@PositiveOrZero`
- `price` → `@DecimalMin("0.0", inclusive = true)`
- Server-managed fields in input are ignored; controller/service rely on mapper ignores.

## 8. Transaction Boundaries
- Service methods annotated:
  - `@Transactional(readOnly = true)` for reads
  - `@Transactional` for create/update/delete
- Keep transactional scope limited to service methods only.

## 9. Backward Compatibility & Data Considerations
- JSON field names remain same as entity fields.
- Input may include server-managed fields; they are ignored.
- No DB schema changes.

## 10. Risks and Mitigations
- Risk: Test failures due to signature changes → Mitigation: refactor tests in tandem; commit in a single PR.
- Risk: MapStruct configuration mismatch → Mitigation: confirm `componentModel=spring` and processor setup.
- Risk: Validation breaking existing clients → Mitigation: confirm constraints reflect current data; document expected errors.
- Risk: N+1 issues when serializing entities → Mitigation: OSIV disabled, ensure endpoints do not rely on lazy serialization.

## 11. Acceptance Criteria (from requirements)
- DTO, Mapper implemented as specified.
- Service and Controller use DTOs exclusively.
- Global exception handling with `ProblemDetail` for 400/404.
- OSIV disabled.
- All tests compile and pass; CRUD behavior intact.

## 12. Implementation Milestones & Estimated Effort
- A: Prep (1h)
- B: DTO + Mapper (1h)
- C: Service refactor (1.5h)
- D: Controller refactor (1h)
- E: Exception handling (1h)
- F: Logging/hygiene (0.5h)
- G: Tests migration (2–3h)
- H: Documentation & cleanup (0.5h)

## 13. Rollout Strategy
- Single feature branch `feature/add-beer-dto-mapstruct`.
- Commit sequence aligned with phases; ensure builds green after each phase.
- Merge via MR/PR with code review focusing on API changes and tests.

## 14. Out of Scope (this iteration)
- Pagination and filtering changes
- New endpoints or entity schema changes
- Advanced error codes taxonomy beyond `ProblemDetail`
- Internationalization of error messages

## 15. Appendix — Snippet Sketches

- BeerDto skeleton:
```java
package guru.springframework.juniemvc.models;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BeerDto {
    private Integer id;
    private Integer version;

    @NotBlank
    private String beerName;

    @NotBlank
    private String beerStyle;

    @NotBlank
    private String upc;

    @PositiveOrZero
    private Integer quantityOnHand;

    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal price;

    private LocalDateTime createdDate;
    private LocalDateTime updateDate;
}
```

- Mapper skeleton:
```java
@Mapper
public interface BeerMapper {
    BeerDto toDto(Beer source);

    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "version", ignore = true),
        @Mapping(target = "createdDate", ignore = true),
        @Mapping(target = "updateDate", ignore = true)
    })
    Beer toEntity(BeerDto source);

    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "version", ignore = true),
        @Mapping(target = "createdDate", ignore = true),
        @Mapping(target = "updateDate", ignore = true)
    })
    void updateEntityFromDto(BeerDto source, @MappingTarget Beer target);
}
```
