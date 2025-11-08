# Prompt Variables
Apply the following variables to placeholders in the prompt. Placeholders are denoted by `${variable}` syntax.

# Placeholders Definitions.
The following key value pairs are used to replace placeholders in the prompt. Format variable defines the variable name and
value defines the value to replace the placeholder with. Defined as `variable name` = `value` pairs in the following list:

* entity_name = `[EntityName]`
* property_name = `[propertyName]`
* property_type = `[PropertyType]`
* column_definition = `[column_definition_for_sql]`

## Goal
The goal is to add a new property `${property_name}` of type `${property_type}` to the existing `${entity_name}` entity and its related components.

## Task Description
Your task is to add the new property to the entity, DTO, database schema, and API documentation, ensuring all related tests pass. Follow the guidelines from the file `.junie/guidelines.md`.

### Task Steps

1.  **Database Migration (Flyway):**
    *   Create a new Flyway migration script in `src/main/resources/db/migration/`.
    *   The script should follow the naming convention `V<next_version_number>__add_${property_name}_to_${entity_name_snake_case}.sql`.
    *   Add a new column to the `${entity_name_snake_case}` table for the property `${property_name}` with the appropriate SQL type: `${column_definition}`.

2.  **JPA Entity and DTO Update:**
    *   Locate the JPA entity class for `${entity_name}` in the `guru.springframework.juniemvc.entities` package.
    *   Add the new property: `private ${property_type} ${property_name};`.
    *   Locate the corresponding DTO(s) for `${entity_name}` (e.g., `${entity_name}Dto`, `${entity_name}Request`) in the `guru.springframework.juniemvc.models` package.
    *   Add the new property `${property_name}` to the DTO(s). If it's part of a request, add relevant validation annotations (e.g., `@Size`, `@NotBlank`).

3.  **MapStruct Mapper Update:**
    *   Inspect the MapStruct mapper for the `${entity_name}` in the `guru.springframework.juniemvc.mappers` package.
    *   Ensure the new property `${property_name}` is correctly mapped between the entity and the DTO(s). MapStruct should handle this automatically if the names match, but verify it.

4.  **API Documentation (OpenAPI):**
    *   Find the OpenAPI schema file(s) for the `${entity_name}` DTO(s) under `openapi/openapi/components/schemas/`.
    *   Add the new property `${property_name}` to the schema definition, including its `type` and an optional `description` and `example`.
    *   From the `openapi` directory, run `npm test` to validate the changes.

5.  **Testing:**
    *   Review existing tests for the `${entity_name}` controller, service, and repository.
    *   Ensure that integration tests (`@SpringBootTest`) that use the database continue to pass after the Flyway migration.
    *   Update tests that create or update the `${entity_name}` to include the new `${property_name}`.
    *   Verify all tests pass by running `mvn test`.