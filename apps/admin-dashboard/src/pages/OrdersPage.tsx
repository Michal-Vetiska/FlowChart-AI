import { useQuery } from '@tanstack/react-query'
import { listOrders } from '../api/client'

export function OrdersPage() {
  const orders = useQuery({ queryKey: ['orders'], queryFn: listOrders })
  return (
    <div className="mx-auto max-w-6xl px-6 py-10">
      <h1 className="mb-6 text-3xl font-semibold">Orders</h1>
      {orders.isLoading && <p className="text-[var(--muted)]">Loading…</p>}
      {orders.isError && <p className="text-[var(--danger)]">Failed to load orders</p>}
      {orders.data && (
        <div className="overflow-hidden rounded-2xl border border-[var(--border)] bg-[var(--panel)]">
          <table className="w-full text-left text-sm">
            <thead className="bg-[#0d1524] text-[var(--muted)]">
              <tr>
                <th className="px-5 py-3">ID</th>
                <th className="px-5 py-3">Status</th>
                <th className="px-5 py-3">Total</th>
                <th className="px-5 py-3">Last transition</th>
              </tr>
            </thead>
            <tbody>
              {orders.data.map((order) => (
                <tr key={order.id} className="border-t border-[var(--border)]">
                  <td className="px-5 py-3 font-mono text-xs">{order.id.slice(0, 8)}</td>
                  <td className="px-5 py-3">{order.status}</td>
                  <td className="px-5 py-3">{order.totalAmount} {order.currency}</td>
                  <td className="px-5 py-3 text-[var(--muted)]">
                    {order.statusHistory.at(-1)?.reason ?? '—'}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
