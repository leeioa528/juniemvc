import React from 'react'
import ReactDOM from 'react-dom/client'
import { StrictMode } from 'react'
import { createBrowserRouter, RouterProvider } from 'react-router-dom'
import App from './App'
import Sandbox from './routes/Sandbox'
import './styles/index.css'

const router = createBrowserRouter([
  { path: '/', element: <App /> },
  { path: '/sandbox', element: <Sandbox /> },
])

ReactDOM.createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <RouterProvider router={router} />
  </StrictMode>,
)
