import { Link } from 'react-router-dom'

export default function NotFound() {
  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-bold">404 - Not Found</h1>
      <p className="text-muted-foreground">The page you requested does not exist.</p>
      <Link to="/" className="underline">Go back home</Link>
    </div>
  )
}
