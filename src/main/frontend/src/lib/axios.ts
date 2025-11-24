import axios, { AxiosError, AxiosInstance } from 'axios'

// Central Axios instance for the app
// - Base URL is relative so it works with Vite dev proxy and prod reverse proxy
// - JSON defaults
// - Lightweight error normalization so UI code can rely on a consistent shape

export type NormalizedApiError = {
  status: number
  message: string
  path?: string
  timestamp?: string
  details?: unknown
  raw?: unknown
}

function normalizeError(error: unknown): NormalizedApiError {
  // Handle AxiosError first
  if (axios.isAxiosError(error)) {
    const err = error as AxiosError<any>
    const status = err.response?.status ?? 0
    const data = err.response?.data

    // Try to map RFC 9457 ProblemDetails-style responses first if backend uses it
    if (data && typeof data === 'object') {
      const message = (data.title as string) || (data.message as string) || err.message
      const result: NormalizedApiError = {
        status,
        message,
      }
      const pathVal = (data.path as string) || undefined
      if (pathVal !== undefined) result.path = pathVal
      const tsVal = (data.timestamp as string) || undefined
      if (tsVal !== undefined) result.timestamp = tsVal
      const detailsVal = (data.detail as unknown) ?? data
      if (detailsVal !== undefined) result.details = detailsVal
      if (data !== undefined) result.raw = data
      return result
    }

    const base: NormalizedApiError = {
      status,
      message: err.message,
    }
    if (data !== undefined) base.raw = data
    return base
  }

  // Handle generated ApiError from openapi-typescript-codegen
  if (typeof error === 'object' && error !== null && (error as any).name === 'ApiError') {
    const e = error as any
    const status = (e.status as number) ?? 0
    const body = e.body
    const message = (body?.title as string) || (body?.message as string) || (e.message as string) || 'API error'
    const result: NormalizedApiError = {
      status,
      message,
      raw: body ?? e,
    }
    const pathVal = (body?.path as string) || undefined
    if (pathVal !== undefined) result.path = pathVal
    const tsVal = (body?.timestamp as string) || undefined
    if (tsVal !== undefined) result.timestamp = tsVal
    const detailsVal = (body?.detail as unknown) ?? body
    if (detailsVal !== undefined) result.details = detailsVal
    return result
  }

  const fallback: NormalizedApiError = { status: 0, message: 'Unknown error' }
  if (error !== undefined) fallback.raw = error
  return fallback
}

export const api: AxiosInstance = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
    Accept: 'application/json',
  },
  // withCredentials can be toggled based on auth strategy
  withCredentials: false,
  // Reasonable default timeout to avoid hanging requests
  timeout: 15000,
})

// Request interceptor — can add auth headers here later
api.interceptors.request.use((config) => {
  // Ensure JSON headers are present; users can override per request
  if (!config.headers) {
    config.headers = {} as any
  }
  const h = config.headers as any
  if (h.Accept === undefined) h.Accept = 'application/json'
  if (h['Content-Type'] === undefined) h['Content-Type'] = 'application/json'
  return config
})

// Response interceptor to normalize errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    return Promise.reject(normalizeError(error))
  },
)

export { normalizeError }
