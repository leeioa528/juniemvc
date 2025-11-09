import { Link, NavLink, Outlet } from 'react-router-dom'

export default function AppLayout() {
  return (
    <div className="min-h-dvh grid grid-rows-[auto,1fr]">
      <header className="border-b bg-white/70 backdrop-blur supports-[backdrop-filter]:bg-white/50">
        <div className="container-max flex h-14 items-center justify-between">
          <div className="font-semibold">JunieMVC</div>
          <nav className="flex gap-3 text-sm">
            <NavLink to="/" className={({isActive}) => isActive ? 'underline' : 'hover:underline'}>
              Home
            </NavLink>
            <NavLink to="/beers" className={({isActive}) => isActive ? 'underline' : 'hover:underline'}>
              Beers
            </NavLink>
            <NavLink to="/customers" className={({isActive}) => isActive ? 'underline' : 'hover:underline'}>
              Customers
            </NavLink>
            <NavLink to="/orders" className={({isActive}) => isActive ? 'underline' : 'hover:underline'}>
              Orders
            </NavLink>
            <Link to="/sandbox" className="hover:underline">Sandbox</Link>
          </nav>
        </div>
      </header>
      <main className="container-max py-6">
        <Outlet />
      </main>
      {/* Global portals area (toasts, dialogs) could be mounted here */}
      <div id="portals" />
    </div>
  )
}
