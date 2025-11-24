import React from 'react'

interface ErrorBoundaryState {
  hasError: boolean
  error?: unknown
}

/**
 * Global error boundary to catch render errors and show a user-friendly fallback.
 */
export class ErrorBoundary extends React.Component<React.PropsWithChildren, ErrorBoundaryState> {
  constructor(props: React.PropsWithChildren) {
    super(props)
    this.state = { hasError: false }
  }

  static getDerivedStateFromError(error: unknown): ErrorBoundaryState {
    return { hasError: true, error }
  }

  componentDidCatch(error: unknown, errorInfo: unknown) {
    // eslint-disable-next-line no-console
    console.error('[ErrorBoundary] Caught error', error, errorInfo)
  }

  private handleRetry = () => {
    this.setState({ hasError: false, error: undefined })
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="container-max py-10">
          <h1 className="text-2xl font-semibold mb-2">Something went wrong</h1>
          <p className="text-sm opacity-80 mb-4">An unexpected error occurred while rendering this page.</p>
          <div className="flex gap-3">
            <button className="px-3 py-2 rounded bg-black text-white" onClick={() => window.location.reload()}>
              Reload page
            </button>
            <button className="px-3 py-2 rounded border" onClick={this.handleRetry}>
              Try again
            </button>
          </div>
        </div>
      )
    }
    return this.props.children
  }
}

export default ErrorBoundary
