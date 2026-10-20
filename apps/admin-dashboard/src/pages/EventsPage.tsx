import { useQuery } from '@tanstack/react-query'
import { listOrders } from '../api/client'

export function EventsPage() {
  const orders = useQuery({ queryKey: ['orders'], queryFn: listOrders })
  const events = (orders.data ?? []).flatMap((order) =>
    order.statusHistory.map((h) => ({
      orderId: order.id,
      status: h.toStatus,
      reason: h.reason,
      at: h.createdAt,
    })),
  ).sort((a, b) => b.at.localeCompare(a.at))

  return (
    <div className="mx-auto max-w-6xl px-6 py-10">
      <h1 className="mb-2 text-3xl font-semibold">Event Monitor</h1>
      <p className="mb-6 text-[var(--muted)]">
        Order status transitions as a lightweight event timeline (outbox/saga emits the same domain facts).
      </p>
      {orders.isLoading && <p className="text-[var(--muted)]">Loading…</p>}
      <div className="space-y-2">
        {events.map((e, idx) => (
          <div key={`${e.orderId}-${idx}`} className="rounded-xl border border-[var(--border)] bg-[var(--panel)] px-4 py-3 text-sm">
            <span className="font-mono text-xs text-[var(--muted)]">{e.orderId.slice(0, 8)}</span>
            <span className="mx-2 text-[var(--accent)]">{e.status}</span>
            <span className="text-[var(--muted)]">{e.reason}</span>
            <span className="float-right text-xs text-[var(--muted)]">{new Date(e.at).toLocaleString()}</span>
          </div>
        ))}
        {!orders.isLoading && events.length === 0 && (
          <p className="text-[var(--muted)]">No transitions yet. Create an order to populate the timeline.</p>
        )}
      </div>
    </div>
  )
}
