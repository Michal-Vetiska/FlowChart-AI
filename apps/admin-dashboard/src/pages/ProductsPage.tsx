import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import type { FormEvent } from 'react'
import { createProduct, listProducts } from '../api/client'

export function ProductsPage() {
  const queryClient = useQueryClient()
  const productsQuery = useQuery({ queryKey: ['products'], queryFn: listProducts })
  const [sku, setSku] = useState('')
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [basePrice, setBasePrice] = useState('99.90')
  const [currency, setCurrency] = useState('CZK')
  const [formError, setFormError] = useState<string | null>(null)

  const createMutation = useMutation({
    mutationFn: createProduct,
    onSuccess: async () => {
      setSku('')
      setName('')
      setDescription('')
      setBasePrice('99.90')
      setFormError(null)
      await queryClient.invalidateQueries({ queryKey: ['products'] })
    },
    onError: (error: Error) => setFormError(error.message),
  })

  function onSubmit(event: FormEvent) {
    event.preventDefault()
    createMutation.mutate({
      sku,
      name,
      description: description || undefined,
      basePrice: Number(basePrice),
      currency,
    })
  }

  return (
    <div className="mx-auto flex w-full max-w-6xl flex-col gap-8 px-6 py-10">
      <header className="flex flex-col gap-2">
        <p className="text-sm tracking-[0.2em] text-[var(--muted)] uppercase">Commerce</p>
        <h1 className="text-3xl font-semibold tracking-tight">Products</h1>
        <p className="max-w-2xl text-[var(--muted)]">
          Vertical slice Milestone 0/1 — create a product through the hexagonal Spring backend and
          see it appear in the admin list.
        </p>
      </header>

      <section className="grid gap-6 lg:grid-cols-[360px_1fr]">
        <form
          onSubmit={onSubmit}
          className="rounded-2xl border border-[var(--border)] bg-[var(--panel)] p-5 shadow-[0_20px_60px_rgba(0,0,0,0.25)]"
        >
          <h2 className="mb-4 text-lg font-medium">Create product</h2>
          <div className="flex flex-col gap-3">
            <label className="flex flex-col gap-1 text-sm">
              <span className="text-[var(--muted)]">SKU</span>
              <input
                required
                value={sku}
                onChange={(e) => setSku(e.target.value)}
                className="rounded-lg border border-[var(--border)] bg-[#0d1524] px-3 py-2 outline-none focus:border-[var(--accent)]"
                placeholder="FC-WIDGET-1"
              />
            </label>
            <label className="flex flex-col gap-1 text-sm">
              <span className="text-[var(--muted)]">Name</span>
              <input
                required
                value={name}
                onChange={(e) => setName(e.target.value)}
                className="rounded-lg border border-[var(--border)] bg-[#0d1524] px-3 py-2 outline-none focus:border-[var(--accent)]"
                placeholder="Flow Widget"
              />
            </label>
            <label className="flex flex-col gap-1 text-sm">
              <span className="text-[var(--muted)]">Description</span>
              <textarea
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                className="min-h-20 rounded-lg border border-[var(--border)] bg-[#0d1524] px-3 py-2 outline-none focus:border-[var(--accent)]"
              />
            </label>
            <div className="grid grid-cols-2 gap-3">
              <label className="flex flex-col gap-1 text-sm">
                <span className="text-[var(--muted)]">Price</span>
                <input
                  required
                  type="number"
                  min="0.01"
                  step="0.01"
                  value={basePrice}
                  onChange={(e) => setBasePrice(e.target.value)}
                  className="rounded-lg border border-[var(--border)] bg-[#0d1524] px-3 py-2 outline-none focus:border-[var(--accent)]"
                />
              </label>
              <label className="flex flex-col gap-1 text-sm">
                <span className="text-[var(--muted)]">Currency</span>
                <input
                  required
                  maxLength={3}
                  value={currency}
                  onChange={(e) => setCurrency(e.target.value.toUpperCase())}
                  className="rounded-lg border border-[var(--border)] bg-[#0d1524] px-3 py-2 outline-none focus:border-[var(--accent)]"
                />
              </label>
            </div>
            {formError && (
              <p className="rounded-lg border border-[var(--danger)]/40 bg-[var(--danger)]/10 px-3 py-2 text-sm text-[var(--danger)]">
                {formError}
              </p>
            )}
            <button
              type="submit"
              disabled={createMutation.isPending}
              className="mt-2 rounded-lg bg-[var(--accent)] px-4 py-2.5 font-medium text-white transition hover:bg-[var(--accent-2)] disabled:opacity-60"
            >
              {createMutation.isPending ? 'Saving…' : 'Create product'}
            </button>
          </div>
        </form>

        <div className="overflow-hidden rounded-2xl border border-[var(--border)] bg-[var(--panel)] shadow-[0_20px_60px_rgba(0,0,0,0.25)]">
          <div className="border-b border-[var(--border)] px-5 py-4">
            <h2 className="text-lg font-medium">Catalog</h2>
          </div>
          {productsQuery.isLoading && (
            <p className="px-5 py-8 text-[var(--muted)]">Loading products…</p>
          )}
          {productsQuery.isError && (
            <p className="px-5 py-8 text-[var(--danger)]">
              Failed to load products. Is the backend running on :8080?
            </p>
          )}
          {productsQuery.data && productsQuery.data.length === 0 && (
            <p className="px-5 py-8 text-[var(--muted)]">No products yet. Create the first one.</p>
          )}
          {productsQuery.data && productsQuery.data.length > 0 && (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm">
                <thead className="bg-[#0d1524] text-[var(--muted)]">
                  <tr>
                    <th className="px-5 py-3 font-medium">SKU</th>
                    <th className="px-5 py-3 font-medium">Name</th>
                    <th className="px-5 py-3 font-medium">Price</th>
                    <th className="px-5 py-3 font-medium">Status</th>
                  </tr>
                </thead>
                <tbody>
                  {productsQuery.data.map((product) => (
                    <tr key={product.id} className="border-t border-[var(--border)]">
                      <td className="px-5 py-3 font-mono text-xs">{product.sku}</td>
                      <td className="px-5 py-3">{product.name}</td>
                      <td className="px-5 py-3">
                        {product.basePrice} {product.currency}
                      </td>
                      <td className="px-5 py-3">
                        <span
                          className={
                            product.active
                              ? 'text-[var(--ok)]'
                              : 'text-[var(--muted)]'
                          }
                        >
                          {product.active ? 'Active' : 'Inactive'}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </section>
    </div>
  )
}
