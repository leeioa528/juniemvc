import React, { Suspense } from 'react'
import ReactDOM from 'react-dom/client'
import { StrictMode, lazy } from 'react'
import { createBrowserRouter, RouterProvider } from 'react-router-dom'
import AppLayout from './layout/AppLayout'
import { ToasterProvider } from './components/Toaster'
import { ThemeProvider } from './components/theme-provider'
import ErrorBoundary from './components/ErrorBoundary'
import RouteError from './routes/RouteError'
import './styles/globals.css'
import './i18n'

const Home = lazy(() => import('./pages/Home'))
const Sandbox = lazy(() => import('./routes/Sandbox'))
const BeersList = lazy(() => import('./routes/BeersList'))
const BeerDetails = lazy(() => import('./routes/BeerDetails'))
const BeerUpsert = lazy(() => import('./routes/BeerUpsert'))
const CustomersList = lazy(() => import('./routes/CustomersList'))
const CustomersDetails = lazy(() => import('./routes/CustomersDetails'))
const OrdersList = lazy(() => import('./routes/OrdersList'))
const OrderDetails = lazy(() => import('./routes/OrderDetails'))
const NotFound = lazy(() => import('./routes/NotFound'))

const router = createBrowserRouter([
  {
    path: '/',
    element: <AppLayout />,
    errorElement: <RouteError />,
    children: [
      { index: true, element: <Home /> },
      { path: 'sandbox', element: <Sandbox /> },
      { path: 'beers', element: <BeersList /> },
      { path: 'beers/new', element: <BeerUpsert /> },
      { path: 'beers/:id', element: <BeerDetails /> },
      { path: 'beers/:id/edit', element: <BeerUpsert /> },
      { path: 'customers', element: <CustomersList /> },
      { path: 'customers/:id', element: <CustomersDetails /> },
      { path: 'orders', element: <OrdersList /> },
      { path: 'orders/:id', element: <OrderDetails /> },
      { path: '*', element: <NotFound /> },
    ],
  },
])

ReactDOM.createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <ThemeProvider>
      <ErrorBoundary>
        <ToasterProvider>
          <Suspense fallback={<div className="container-max py-8">Loading…</div>}>
            <RouterProvider router={router} />
          </Suspense>
        </ToasterProvider>
      </ErrorBoundary>
    </ThemeProvider>
  </StrictMode>,
)
