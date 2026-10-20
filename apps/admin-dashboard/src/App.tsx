import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import type { ReactNode } from 'react'
import { BrowserRouter, Link, Navigate, Route, Routes, useLocation } from 'react-router-dom'
import { getToken, setToken } from './api/client'
import { ChaosPage } from './pages/ChaosPage'
import { CopilotPage } from './pages/CopilotPage'
import { EventsPage } from './pages/EventsPage'
import { IncidentPage } from './pages/IncidentPage'
import { InventoryPage } from './pages/InventoryPage'
import { KnowledgePage } from './pages/KnowledgePage'
import { LoginPage } from './pages/LoginPage'
import { OrdersPage } from './pages/OrdersPage'
import { OverviewPage } from './pages/OverviewPage'
import { ProductsPage } from './pages/ProductsPage'

const queryClient = new QueryClient()

function NavLink({ to, children }: { to: string; children: ReactNode }) {
  const location = useLocation()
  const active = location.pathname === to
  return (
    <Link
      to={to}
      className={active ? 'text-white' : 'text-[var(--muted)] hover:text-white'}
    >
      {children}
    </Link>
  )
}

function Shell({ children }: { children: ReactNode }) {
  return (
    <div className="min-h-screen">
      <nav className="border-b border-[var(--border)] bg-[#0a101c]/90 backdrop-blur">
        <div className="mx-auto flex max-w-6xl flex-wrap items-center justify-between gap-4 px-6 py-4">
          <Link to="/" className="text-lg font-semibold tracking-tight">
            FlowCart <span className="text-[var(--accent)]">AI</span>
          </Link>
          <div className="flex flex-wrap gap-4 text-sm">
            <NavLink to="/">Overview</NavLink>
            <NavLink to="/products">Products</NavLink>
            <NavLink to="/orders">Orders</NavLink>
            <NavLink to="/inventory">Inventory</NavLink>
            <NavLink to="/events">Events</NavLink>
            <NavLink to="/copilot">AI Copilot</NavLink>
            <NavLink to="/knowledge">Knowledge</NavLink>
            <NavLink to="/chaos">Chaos Lab</NavLink>
            <NavLink to="/incidents">Investigator</NavLink>
          </div>
          <button
            className="text-sm text-[var(--muted)] hover:text-white"
            onClick={() => {
              setToken(null)
              window.location.href = '/login'
            }}
          >
            Logout
          </button>
        </div>
      </nav>
      {children}
    </div>
  )
}

function RequireAuth({ children }: { children: ReactNode }) {
  if (!getToken()) return <Navigate to="/login" replace />
  return <Shell>{children}</Shell>
}

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/" element={<RequireAuth><OverviewPage /></RequireAuth>} />
          <Route path="/products" element={<RequireAuth><ProductsPage /></RequireAuth>} />
          <Route path="/orders" element={<RequireAuth><OrdersPage /></RequireAuth>} />
          <Route path="/inventory" element={<RequireAuth><InventoryPage /></RequireAuth>} />
          <Route path="/events" element={<RequireAuth><EventsPage /></RequireAuth>} />
          <Route path="/copilot" element={<RequireAuth><CopilotPage /></RequireAuth>} />
          <Route path="/knowledge" element={<RequireAuth><KnowledgePage /></RequireAuth>} />
          <Route path="/chaos" element={<RequireAuth><ChaosPage /></RequireAuth>} />
          <Route path="/incidents" element={<RequireAuth><IncidentPage /></RequireAuth>} />
        </Routes>
      </BrowserRouter>
    </QueryClientProvider>
  )
}
