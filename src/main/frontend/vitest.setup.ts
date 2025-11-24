// Vitest setup file
import '@testing-library/jest-dom'
import { webcrypto } from 'node:crypto'
import { setupServer } from 'msw/node'

// Polyfill Web Crypto for Node test environment
if (!globalThis.crypto || !(globalThis.crypto as any).getRandomValues) {
  // @ts-expect-error assign to global
  globalThis.crypto = webcrypto
}

// MSW server for mocking network requests in tests
export const server = setupServer()

beforeAll(() => server.listen({ onUnhandledRequest: 'bypass' }))
afterEach(() => server.resetHandlers())
afterAll(() => server.close())
