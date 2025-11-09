import { Link, Outlet, RouteObject } from 'react-router-dom'

export default function App() {
  return (
    <div className="min-h-dvh flex flex-col">
      <header className="border-b bg-white/70 backdrop-blur supports-[backdrop-filter]:bg-white/50">
        <div className="container-max flex h-14 items-center justify-between">
          <div className="font-semibold">JunieMVC</div>
          <nav className="flex gap-3 text-sm">
            <Link to="/" className="hover:underline">
              Home
            </Link>
            <Link to="/sandbox" className="hover:underline">
              Sandbox
            </Link>
          </nav>
        </div>
      </header>
      <main className="container-max py-6 grow">
        <h1 className="text-2xl font-bold mb-4">Welcome</h1>
        <p className="text-muted-foreground mb-4">
          This is the React frontend scaffold. Tailwind classes should style this
          text.
        </p>
        <Outlet />
      </main>
    </div>
  )
}
