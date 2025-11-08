# Prompt Variables
Apply the following variables to placeholders in the prompt. Placeholders are denoted by `${variable}` syntax.

# Placeholders Definitions.
The following key value pairs are used to replace placeholders in the prompt. Format variable defines the variable name and
value defines the value to replace the placeholder with. Defined as `variable name` = `value` pairs in the following list:

* EntityName = `[EntityName]`
# Prompt: Add PATCH Operation for ${EntityName}

## Goal
The goal is to add a `PATCH` operation for the `${EntityName}` resource to allow for partial updates of its properties. This involves creating a dedicated DTO for patch requests, updating the MapStruct mapper to ignore null values, and implementing the corresponding service and controller logic.

## Task Description
Your task is to implement a `PATCH` endpoint for the `${EntityName}` resource, following all project guidelines.

### Task Steps

1.  **Create a Patch Request DTO:**
    *   Create a new Java class `src/main/java/guru/springframework/juniemvc/models/${EntityName}PatchRequest.java`.
    *   This class should contain the mutable properties of the `${EntityName}` entity.
    *   All properties in this DTO must be optional. Do NOT use `@NotNull` or `@NotBlank` annotations on the fields themselves, as any field can be omitted in a patch request. You can keep validation annotations that check the value *if* it is provided (e.g., `@Positive`, `@Size`).
    *   Use Lombok annotations (`@Getter`, `@Setter`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`).

2.  **Update MapStruct Mapper:**
    *   Modify `src/main/java/guru/springframework/juniemvc/mappers/${EntityName}Mapper.java`.
    *   Add a new method for partial updates:
        ```java
        @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
        @Mapping(target = "id", ignore = true)
        @Mapping(target = "version", ignore = true)
        @Mapping(target = "createdDate", ignore = true)
        @Mapping(target = "updateDate", ignore = true)
        // Add other ignores for non-updatable fields if necessary
        void patchEntityFromRequest(${EntityName}PatchRequest request, @MappingTarget ${EntityName} entity);
        ```
    *   Ensure the necessary imports for `BeanMapping`, `NullValuePropertyMappingStrategy`, and `${EntityName}PatchRequest` are present.

3.  **Update Service Layer:**
    *   Add a new method signature to the `src/main/java/guru/springframework/juniemvc/services/${EntityName}Service.java` interface:
        ```java
        Optional<${EntityName}Dto> patch${EntityName}(Integer id, ${EntityName}PatchRequest request);
        ```
    *   Implement this method in `src/main/java/guru/springframework/juniemvc/services/${EntityName}ServiceImpl.java`.
        *   The method should be annotated with `@Transactional`.
        *   Fetch the existing `${EntityName}` entity by its `id`.
        *   If the entity exists, use the new mapper method (`patchEntityFromRequest`) to apply the changes from the request DTO.
        *   Save the updated entity using the repository.
        *   Map the saved entity back to a `${EntityName}Dto` and return it.
        *   If the entity does not exist, return `Optional.empty()`.

4.  **Update Controller Layer:**
    *   Add a new `PATCH` endpoint to `src/main/java/guru/springframework/juniemvc/controllers/${EntityName}Controller.java`:
        ```java
        @PatchMapping("/{id}")
        ResponseEntity<${EntityName}Dto> patch${EntityName}(@PathVariable Integer id, @RequestBody ${EntityName}PatchRequest request) {
            return service.patch${EntityName}(id, request)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        }
        ```

5.  **Update OpenAPI Documentation:**
    *   Create a new schema file for the patch request: `openapi/openapi/components/schemas/${EntityName}PatchRequest.yaml`.
        *   This schema should define all the optional properties from the `${EntityName}PatchRequest` DTO.
        *   Do NOT include a `required` section.
    *   Add a `patch` operation to the path item file (e.g., `openapi/openapi/paths/${entityName}s_{id}.yaml`).
        *   The operation should reference the new `${EntityName}PatchRequest.yaml` as its request body schema.
        *   Define responses for `200 OK` (returning `${EntityName}.yaml`), `400 Bad Request`, and `404 Not Found`.

6.  **Add/Update Tests:**
    *   **Mapper Test:** Add a unit test for the `patchEntityFromRequest` method to ensure it correctly ignores `null` properties and updates non-`null` ones.
    *   **Service Test:** Add a unit test for the `patch${EntityName}` service method.
    *   **Controller/Integration Test:** Add a test for the new `PATCH` endpoint to verify the end-to-end flow, including status codes and response bodies.

7.  **Final Verification:**
    *   Run `mvn test` to ensure all existing and new tests pass.
    *   Run `npm test` from the `openapi` directory to validate the API specification.
