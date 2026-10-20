import { useState } from 'react'
import type { FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { login } from '../api/client'

export function LoginPage() {
  const navigate = useNavigate()
  const [username, setUsername] = useState('admin')
  const [password, setPassword] = useState('admin')
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    setLoading(true)
    setError(null)
    try {
      await login(username, password)
      navigate('/')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Login failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="mx-auto flex min-h-screen max-w-md flex-col justify-center px-6">
      <h1 className="mb-2 text-3xl font-semibold">FlowCart AI</h1>
      <p className="mb-6 text-[var(--muted)]">Demo users: admin/admin, warehouse/warehouse</p>
      <form onSubmit={onSubmit} className="rounded-2xl border border-[var(--border)] bg-[var(--panel)] p-5">
        <label className="mb-3 flex flex-col gap-1 text-sm">
          <span className="text-[var(--muted)]">Username</span>
          <input className="rounded-lg border border-[var(--border)] bg-[#0d1524] px-3 py-2" value={username} onChange={(e) => setUsername(e.target.value)} />
        </label>
        <label className="mb-4 flex flex-col gap-1 text-sm">
          <span className="text-[var(--muted)]">Password</span>
          <input type="password" className="rounded-lg border border-[var(--border)] bg-[#0d1524] px-3 py-2" value={password} onChange={(e) => setPassword(e.target.value)} />
        </label>
        {error && <p className="mb-3 text-sm text-[var(--danger)]">{error}</p>}
        <button disabled={loading} className="w-full rounded-lg bg-[var(--accent)] px-4 py-2.5 font-medium text-white">
          {loading ? 'Signing in…' : 'Sign in'}
        </button>
      </form>
    </div>
  )
}
