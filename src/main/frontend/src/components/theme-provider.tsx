import * as React from 'react'

/**
 * Minimal ThemeProvider using prefers-color-scheme.
 * You can extend this later to support user toggles and persistence.
 */
export function ThemeProvider({ children }: { children: React.ReactNode }) {
  return <>{children}</>
}
