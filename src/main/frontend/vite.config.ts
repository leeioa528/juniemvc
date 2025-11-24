// IMPORTANT: Polyfill Web Crypto before importing Vite or plugins.
// Some Vite internals may access globalThis.crypto at module init time.
const { webcrypto } = await import('node:crypto')
// @ts-expect-error assigning readonly global for test/build time
if (!globalThis.crypto || typeof (globalThis.crypto as any).getRandomValues !== 'function') {
  // @ts-expect-error assign webcrypto polyfill
  globalThis.crypto = webcrypto
}

// Defer imports that may evaluate Vite code until after crypto is set
const { defineConfig } = await import('vite')
const react = (await import('@vitejs/plugin-react-swc')).default

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    open: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      },
      '/actuator': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      },
    },
  },
  build: {
    outDir: '../resources/static',
    emptyOutDir: true,
  },
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: './vitest.setup.ts',
    css: true,
    coverage: {
      provider: 'v8',
      reporter: ['text', 'html'],
      reportsDirectory: './coverage',
    },
  },
})
