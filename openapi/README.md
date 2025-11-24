# OpenAPI Tooling

This directory contains the OpenAPI 3.1 specification and Redocly CLI tooling used to lint, preview, and bundle the API docs.

- Entrypoint spec: `openapi/openapi/openapi.yaml`
- Components: `openapi/openapi/components/**`
- Paths: `openapi/openapi/paths/**` (flat file-per-path layout using `_` as `/` separator)

## Prerequisites
- Node.js 22.11.0 and npm 10.9.0 (matching the project toolchain)

## Install
Run once (from repo root or this folder):

```bash
cd openapi
npm ci
```

## Commands
- Preview docs locally (auto reload):
  ```bash
  npm start
  # opens Redoc preview server for openapi/openapi/openapi.yaml
  ```
- Lint / validate the spec (required before commits/PRs):
  ```bash
  npm test
  # runs: redocly lint
  ```
- Produce a single bundled file for distribution:
  ```bash
  npm run build
  # outputs to: openapi/dist/bundle.yaml
  ```

## Authoring Tips
- Keep one file per path item in `openapi/openapi/paths` and reference from `openapi.yaml` using `$ref`.
- Put reusable schemas, headers, and responses in `openapi/openapi/components/**` and reference with relative paths.
- Validate changes with `npm test` before committing.

## Frontend Client Generation
From the frontend app (`src/main/frontend`), run:

```bash
npm run api:gen
```

This generates TypeScript types and axios-based clients to `src/main/frontend/src/lib/api` using `openapi-typescript-codegen` against the entrypoint `openapi/openapi/openapi.yaml`.


## Contract-First Workflow (Source of Truth)

Follow this sequence for any API change to keep backend, frontend, and documentation in sync:

1) Update the spec
- Edit `openapi/openapi/openapi.yaml` and any referenced files under `openapi/openapi/paths` and `openapi/openapi/components`.
- Keep one file per path item; use `_` in filenames to represent `/` (e.g., `/users/{id}` → `paths/users_{id}.yaml`).

2) Lint/validate the spec
```bash
cd openapi
npm test
# runs: redocly lint against openapi/openapi/openapi.yaml
```

3) Generate frontend API clients and types
```bash
cd ../src/main/frontend
npm run api:gen
# outputs to: src/lib/api (axios-based clients + types via openapi-typescript-codegen)
```

4) Implement backend and UI against the generated types
- Backend: implement/adjust controllers and services to match the contract.
- Frontend: import clients/types from `src/lib/api` and build features without redefining shapes.

5) Build integration ensures freshness
- The frontend `build` script runs `api:gen` before `vite build` (see `package.json`), ensuring generated code matches the latest spec when producing artifacts.

PR checklist
- [ ] Spec updated and `openapi/npm test` passes
- [ ] Clients regenerated (`npm run api:gen`) and TypeScript compiles
- [ ] Backend and UI changes align with the spec (no drift)
