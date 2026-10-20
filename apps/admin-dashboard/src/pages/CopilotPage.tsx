import { useState } from 'react'
import type { FormEvent } from 'react'
import { askCopilot } from '../api/client'

export function CopilotPage() {
  const [question, setQuestion] = useState('Why did cancelled orders increase?')
  const [loading, setLoading] = useState(false)
  const [result, setResult] = useState<Awaited<ReturnType<typeof askCopilot>> | null>(null)
  const [error, setError] = useState<string | null>(null)

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    setLoading(true)
    setError(null)
    try {
      setResult(await askCopilot(question))
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="mx-auto max-w-3xl px-6 py-10">
      <h1 className="mb-2 text-3xl font-semibold">AI Copilot</h1>
      <p className="mb-6 text-[var(--muted)]">Tool-calling assistant over live order/inventory/knowledge APIs.</p>
      <form onSubmit={onSubmit} className="mb-6 flex gap-3">
        <input className="flex-1 rounded-lg border border-[var(--border)] bg-[#0d1524] px-3 py-2" value={question} onChange={(e) => setQuestion(e.target.value)} />
        <button className="rounded-lg bg-[var(--accent)] px-4 py-2 text-white" disabled={loading}>{loading ? '…' : 'Ask'}</button>
      </form>
      {error && <p className="text-[var(--danger)]">{error}</p>}
      {result && (
        <div className="space-y-4 rounded-2xl border border-[var(--border)] bg-[var(--panel)] p-5">
          <p>{result.summary}</p>
          <p className="text-sm text-[var(--muted)]">Confidence: {result.confidence}</p>
          <div>
            <h2 className="mb-2 font-medium">Tool steps</h2>
            <ul className="space-y-2 text-sm text-[var(--muted)]">
              {result.tools.map((t) => (
                <li key={t.name} className="rounded-lg border border-[var(--border)] px-3 py-2 font-mono text-xs">
                  {t.name}: {JSON.stringify(t.result)}
                </li>
              ))}
            </ul>
          </div>
        </div>
      )}
    </div>
  )
}
