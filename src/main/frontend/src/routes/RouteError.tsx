import { isRouteErrorResponse, useRouteError, Link } from 'react-router-dom'

export default function RouteError() {
  const error = useRouteError()
  let title = 'Unexpected error'
  let message = 'Something went wrong while loading this page.'
  let statusText: string | undefined

  if (isRouteErrorResponse(error)) {
    title = `${error.status} ${error.statusText}`
    try {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const data = (error.data as any) || {}
      message = data.message || message
    } catch (_) {
      // ignore
    }
    statusText = error.statusText
  } else if (error instanceof Error) {
    message = error.message
    statusText = error.name
  }

  return (
    <div className="space-y-3">
      <h1 className="text-2xl font-semibold">{title}</h1>
      <p className="opacity-80">{message}</p>
      {statusText && <p className="text-sm opacity-60">{statusText}</p>}
      <div className="flex gap-3 pt-2">
        <button className="px-3 py-2 rounded bg-black text-white" onClick={() => window.location.reload()}>
          Reload
        </button>
        <Link to="/" className="px-3 py-2 rounded border">
          Go home
        </Link>
      </div>
    </div>
  )
}
