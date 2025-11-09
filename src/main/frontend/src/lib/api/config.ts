import { OpenAPI } from './core/OpenAPI'

// Configure generated OpenAPI client base URL at runtime.
// Uses relative '/api' by default (works with Vite proxy in dev and Boot in prod)
// but allows override via VITE_API_BASE if needed.
OpenAPI.BASE = (import.meta as any).env?.VITE_API_BASE ?? '/api'
