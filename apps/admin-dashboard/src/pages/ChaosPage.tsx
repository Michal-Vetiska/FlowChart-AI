import { useQuery, useQueryClient } from '@tanstack/react-query'
import { chaosReset, chaosStart, chaosStatus } from '../api/client'

export function ChaosPage() {
  const queryClient = useQueryClient()
  const status = useQuery({ queryKey: ['chaos'], queryFn: chaosStatus })

  async function run(scenarioType: string) {
    await chaosStart(scenarioType, {})
    await queryClient.invalidateQueries({ queryKey: ['chaos'] })
  }

  return (
    <div className="mx-auto max-w-4xl px-6 py-10">
      <h1 className="mb-2 text-3xl font-semibold">Chaos Lab</h1>
      <p className="mb-6 text-[var(--muted)]">Controlled failure injection for interview demos (non-prod).</p>
      <div className="mb-6 flex flex-wrap gap-3">
        <button className="rounded-lg border border-[var(--border)] px-4 py-2" onClick={() => run('PAYMENT_FAILURE')}>Payment failure</button>
        <button className="rounded-lg border border-[var(--border)] px-4 py-2" onClick={() => run('INVENTORY_TIMEOUT')}>Inventory timeout</button>
        <button className="rounded-lg border border-[var(--border)] px-4 py-2" onClick={() => run('DUPLICATE_EVENT')}>Duplicate event</button>
        <button className="rounded-lg bg-[var(--accent)] px-4 py-2 text-white" onClick={async () => { await chaosReset(); await queryClient.invalidateQueries({ queryKey: ['chaos'] }) }}>Reset</button>
      </div>
      <pre className="overflow-auto rounded-2xl border border-[var(--border)] bg-[var(--panel)] p-4 text-xs">
        {JSON.stringify(status.data, null, 2)}
      </pre>
    </div>
  )
}
