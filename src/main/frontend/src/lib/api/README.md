# API Client Generation

This folder contains the generated TypeScript API client and types.

Regenerate the client from the OpenAPI spec using:

```
npm run api:gen
```

Notes:
- Source OpenAPI file: `openapi/openapi/openapi.yaml` (relative to repo root)
- Output directory: this folder (`src/main/frontend/src/lib/api`)
- Generator: `openapi-typescript-codegen` with `--client axios --useOptions`
- Commit policy: Check generated code into VCS to avoid requiring the generator in runtime builds. Re-generate on backend API changes.
