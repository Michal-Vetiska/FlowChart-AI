import { useQuery } from '@tanstack/react-query'
import { listStock, listWarehouses } from '../api/client'

export function InventoryPage() {
  const stock = useQuery({ queryKey: ['stock'], queryFn: listStock })
  const warehouses = useQuery({ queryKey: ['warehouses'], queryFn: listWarehouses })
  return (
    <div className="mx-auto max-w-6xl px-6 py-10">
      <h1 className="mb-6 text-3xl font-semibold">Inventory</h1>
      <p className="mb-4 text-sm text-[var(--muted)]">
        Warehouses: {warehouses.data?.map((w) => w.code).join(', ') || 'none'}
      </p>
      {stock.isLoading && <p className="text-[var(--muted)]">Loading…</p>}
      {stock.data && (
        <div className="overflow-hidden rounded-2xl border border-[var(--border)] bg-[var(--panel)]">
          <table className="w-full text-left text-sm">
            <thead className="bg-[#0d1524] text-[var(--muted)]">
              <tr>
                <th className="px-5 py-3">Product</th>
                <th className="px-5 py-3">Available</th>
                <th className="px-5 py-3">Reserved</th>
              </tr>
            </thead>
            <tbody>
              {stock.data.map((item) => (
                <tr key={item.id} className="border-t border-[var(--border)]">
                  <td className="px-5 py-3 font-mono text-xs">{item.productId.slice(0, 8)}</td>
                  <td className="px-5 py-3">{item.availableQty}</td>
                  <td className="px-5 py-3">{item.reservedQty}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
