import { useQuery } from '@tanstack/react-query'
import { listOrders, listProducts, listStock } from '../api/client'

export function OverviewPage() {
  const products = useQuery({ queryKey: ['products'], queryFn: listProducts })
  const orders = useQuery({ queryKey: ['orders'], queryFn: listOrders })
  const stock = useQuery({ queryKey: ['stock'], queryFn: listStock })

  const cancelled = orders.data?.filter((o) => o.status === 'CANCELLED').length ?? 0
  const confirmed = orders.data?.filter((o) => o.status === 'CONFIRMED').length ?? 0
  const lowStock = stock.data?.filter((s) => s.availableQty <= 2).length ?? 0

  return (
    <div className="mx-auto max-w-6xl px-6 py-10">
      <h1 className="mb-2 text-3xl font-semibold">Overview</h1>
      <p className="mb-8 text-[var(--muted)]">Live commerce + operations signals for interview demos.</p>
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <Card label="Products" value={products.data?.length ?? '…'} />
        <Card label="Orders" value={orders.data?.length ?? '…'} />
        <Card label="Confirmed" value={confirmed} />
        <Card label="Cancelled" value={cancelled} />
        <Card label="Low stock items" value={lowStock} />
      </div>
    </div>
  )
}

function Card({ label, value }: { label: string; value: string | number }) {
  return (
    <div className="rounded-2xl border border-[var(--border)] bg-[var(--panel)] p-5">
      <p className="text-sm text-[var(--muted)]">{label}</p>
      <p className="mt-2 text-3xl font-semibold">{value}</p>
    </div>
  )
}
