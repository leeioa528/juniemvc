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
  if (axios.isAxiosError(error)) {
    const err = error as AxiosError<any>
    const status = err.response?.status ?? 0
    const data = err.response?.data

    // Try to map RFC 9457 ProblemDetails-style responses first if backend uses it
    if (data && typeof data === 'object') {
      const message = (data.title as string) || (data.message as string) || err.message
      return {
        status,
        message,
        path: (data.path as string) || undefined,
        timestamp: (data.timestamp as string) || undefined,
        details: (data.detail as unknown) ?? data,
        raw: data,
      }
    }

    return {
      status,
      message: err.message,
      raw: data,
    }
  }

  return { status: 0, message: 'Unknown error', raw: error }
}

export const api: AxiosInstance = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
    Accept: 'application/json',
  },
  // withCredentials can be toggled based on auth strategy
  withCredentials: false,
})

// Request interceptor — can add auth headers here later
api.interceptors.request.use((config) => {
  // Ensure JSON headers are present; users can override per request
  config.headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    ...(config.headers || {}),
  }
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
