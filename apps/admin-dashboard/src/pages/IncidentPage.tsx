import { useState } from 'react'
import type { FormEvent } from 'react'
import { investigate } from '../api/client'

export function IncidentPage() {
  const [symptom, setSymptom] = useState('Investigate why order processing is failing')
  const [report, setReport] = useState<Record<string, unknown> | null>(null)
  const [loading, setLoading] = useState(false)

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    setLoading(true)
    try {
      setReport(await investigate(symptom))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="mx-auto max-w-4xl px-6 py-10">
      <h1 className="mb-2 text-3xl font-semibold">Incident Investigator</h1>
      <p className="mb-6 text-[var(--muted)]">Correlates Chaos flags, order metrics, and knowledge RAG into a report.</p>
      <form onSubmit={onSubmit} className="mb-6 flex gap-3">
        <input className="flex-1 rounded-lg border border-[var(--border)] bg-[#0d1524] px-3 py-2" value={symptom} onChange={(e) => setSymptom(e.target.value)} />
        <button className="rounded-lg bg-[var(--accent)] px-4 py-2 text-white" disabled={loading}>{loading ? '…' : 'Investigate'}</button>
      </form>
      {report && (
        <pre className="overflow-auto rounded-2xl border border-[var(--border)] bg-[var(--panel)] p-4 text-xs">
          {JSON.stringify(report, null, 2)}
        </pre>
      )}
    </div>
  )
}
