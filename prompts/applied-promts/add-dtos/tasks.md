# Tasks: Introduce DTOs and MapStruct to decouple Web and Persistence layers

Date: 2025-10-26 16:03

Instructions: Mark tasks as done by replacing [ ] with [x]. Complete items in order per phases. Keep commits aligned to phases/milestones.

## Phase A — Preparation
1. [x] Create/verify package structure:
   1.1. [x] Ensure package `guru.springframework.juniemvc.models` exists.
   1.2. [x] Ensure package `guru.springframework.juniemvc.mappers` exists.
2. [x] Verify `pom.xml` configuration:
   2.1. [x] Lombok dependency present.
   2.2. [x] MapStruct dependency and annotation processor present.
   2.3. [x] Compiler arg `-Amapstruct.defaultComponentModel=spring` configured.
   2.4. [x] `spring-boot-starter-validation` present.
3. [x] Disable OSIV:
   3.1. [x] Add `spring.jpa.open-in-view=false` to `src/main/resources/application.properties` (or verify already present).

## Phase B — Implement DTOs and Mapper
4. [x] Implement `BeerDto` in `guru.springframework.juniemvc.models`:
   4.1. [x] Add fields: `id`, `version`, `beerName`, `beerStyle`, `upc`, `quantityOnHand`, `price`, `createdDate`, `updateDate`.
   4.2. [x] Add Lombok annotations: `@Getter`, `@Setter`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`.
   4.3. [x] Add validation: `@NotBlank` on `beerName`, `beerStyle`, `upc`.
   4.4. [x] Add validation: `@PositiveOrZero` on `quantityOnHand`.
   4.5. [x] Add validation: `@DecimalMin("0.0", inclusive = true)` on `price`.
   4.6. [x] Treat `id`, `version`, `createdDate`, `updateDate` as server-managed (not relied upon from input).
5. [x] Implement `BeerMapper` in `guru.springframework.juniemvc.mappers`:
   5.1. [x] Define method `BeerDto toDto(Beer entity)`.
   5.2. [x] Define method `Beer toEntity(BeerDto dto)` with mappings to ignore server-managed fields.
   5.3. [x] Define method `void updateEntityFromDto(BeerDto dto, @MappingTarget Beer entity)` with ignores for server-managed fields.
   5.4. [x] Add `@Mapping(target = "id", ignore = true)` for entity creation/update.
   5.5. [x] Add `@Mapping(target = "version", ignore = true)`.
   5.6. [x] Add `@Mapping(target = "createdDate", ignore = true)`.
   5.7. [x] Add `@Mapping(target = "updateDate", ignore = true)`.

## Phase C — Refactor Service Layer to DTOs
6. [x] Update `BeerService` interface to DTO-based signatures:
   6.1. [x] `List<BeerDto> getAllBeers()`.
   6.2. [x] `Optional<BeerDto> getBeerById(Integer id)`.
   6.3. [x] `BeerDto createBeer(BeerDto beerDto)`.
   6.4. [x] `Optional<BeerDto> updateBeer(Integer id, BeerDto beerDto)`.
   6.5. [x] `boolean deleteBeerById(Integer id)`.
7. [x] Refactor `BeerServiceImpl` implementation:
   7.1. [x] Inject `BeerRepository` and `BeerMapper` via constructor into `final` fields (constructor injection).
   7.2. [x] Map entities to DTOs in read methods.
   7.3. [x] Implement create: DTO → entity (ignore server-managed), save, entity → DTO.
   7.4. [x] Implement update: fetch, `updateEntityFromDto`, save, map to DTO; return `Optional.empty()` if not found.
   7.5. [x] Implement delete: check existence, delete if present; return boolean result.
   7.6. [x] Add `@Transactional(readOnly = true)` to read methods.
   7.7. [x] Add `@Transactional` to write methods (create/update/delete).
   7.8. [x] Prefer package-private class visibility if feasible; keep method visibilities per interface.

## Phase D — Refactor Controller to DTOs
8. [x] Update `BeerController` to use DTOs only:
   8.1. [x] Change method signatures to accept/return `BeerDto`.
   8.2. [x] Annotate request bodies with `@Valid`.
   8.3. [x] POST: return `201 Created` with body and `Location: /api/v1/beers/{id}`.
   8.4. [x] GET by id: return `200 OK` with body or `404 Not Found` if absent.
   8.5. [x] PUT: return `200 OK` with updated body or `404 Not Found` if id not found.
   8.6. [x] DELETE: return `204 No Content` or `404 Not Found`.
   8.7. [x] Keep base mapping at `/api/v1/beers`.
   8.8. [x] Use constructor injection for `BeerService` (final field). Prefer package-private controller class.

## Phase E — Centralized Exception Handling
9. [x] Create `GlobalExceptionHandler` annotated with `@RestControllerAdvice`:
   9.1. [x] Handle `MethodArgumentNotValidException` → return `400 Bad Request` `ProblemDetail` with field error details.
   9.2. [x] Handle not-found scenarios (e.g., map `NoSuchElementException`/custom `NotFoundException`) → return `404 Not Found` `ProblemDetail`.
   9.3. [x] Ensure consistent error payload fields: `type`, `title`, `status`, `detail`, `instance`.

## Phase F — Logging and Hygiene
10. [x] Ensure logging best practices:
    10.1. [x] Use SLF4J (no `System.out.println`).
    10.2. [x] Guard expensive debug logs with `logger.isDebugEnabled()` or fluent `atDebug()`.
    10.3. [x] Avoid logging sensitive data.

## Phase G — Tests Migration and Additions
11. [x] Update Service tests to DTO API:
    11.1. [x] Adjust test method calls to new signatures.
    11.2. [x] Verify create/update mappings and values.
12. [x] Update Controller tests:
    12.1. [x] Validate status codes and `application/json` content type.
    12.2. [x] Verify JSON structure matches `BeerDto` fields.
    12.3. [x] POST: assert `Location` header set correctly.
13. [ ] Add Mapper unit tests (optional but recommended):
    13.1. [ ] Test `toDto`, `toEntity`, and `updateEntityFromDto` for typical and null cases.
14. [x] Keep Repository tests unchanged; ensure they still pass.
15. [x] Run full test suite; resolve compile errors from signature changes.

## Phase H — Documentation and Cleanup
16. [x] Update `README.md` to document DTO-based API and MapStruct usage.
17. [x] Review and adjust visibilities: prefer package-private for controllers/configs/`@Bean` methods where safe.
18. [x] Re-run full build; ensure no MapStruct/Lombok warnings; ensure all tests pass.

## File-by-File Checklist
19. [x] Create `src/main/java/guru/springframework/juniemvc/models/BeerDto.java`.
20. [x] Create `src/main/java/guru/springframework/juniemvc/mappers/BeerMapper.java`.
21. [x] Edit `src/main/java/guru/springframework/juniemvc/services/BeerService.java` (signatures).
22. [x] Refactor `src/main/java/guru/springframework/juniemvc/services/BeerServiceImpl.java`.
23. [x] Refactor `src/main/java/guru/springframework/juniemvc/controllers/BeerController.java`.
24. [x] Create `src/main/java/guru/springframework/juniemvc/handlers/GlobalExceptionHandler.java`.
25. [x] Edit `src/main/resources/application.properties` (add `spring.jpa.open-in-view=false`).
26. [x] Adjust tests under `src/test/java/...` accordingly.

## API Behavior Verification
27. [x] GET `/api/v1/beers` → `200 OK` with `List<BeerDto>`.
28. [x] GET `/api/v1/beers/{id}` → `200 OK` with body or `404` if not found.
29. [x] POST `/api/v1/beers` → `201 Created` with body and `Location` header.
30. [x] PUT `/api/v1/beers/{id}` → `200 OK` with updated body or `404` if not found.
31. [x] DELETE `/api/v1/beers/{id}` → `204 No Content` or `404` if not found.

## Acceptance Criteria Checklist
32. [x] DTO and Mapper implemented per design.
33. [x] Service and Controller use DTOs exclusively.
34. [x] Global exception handling with `ProblemDetail` for 400/404 in place.
35. [x] OSIV disabled via configuration.
36. [x] All tests compile and pass; CRUD behavior intact.

## Risks & Mitigations (Validation)
37. [x] Confirm MapStruct setup (`componentModel=spring`, processors) before heavy refactors.
38. [x] Review validation rules to avoid breaking existing data/clients; document expected errors.
39. [x] Ensure endpoints do not rely on lazy serialization (OSIV disabled).

## Rollout
40. [ ] Create branch `feature/add-beer-dto-mapstruct`.
41. [ ] Commit per phase ensuring green build after each.
42. [ ] Open PR/MR; request review focusing on API changes and tests.
