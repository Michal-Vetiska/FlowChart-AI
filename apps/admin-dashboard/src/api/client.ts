const tokenKey = 'flowcart_token'

export function getToken() {
  return localStorage.getItem(tokenKey)
}

export function setToken(token: string | null) {
  if (token) localStorage.setItem(tokenKey, token)
  else localStorage.removeItem(tokenKey)
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const headers: Record<string, string> = {
    ...(init?.headers as Record<string, string> | undefined),
  }
  if (!(init?.body instanceof FormData)) {
    headers['Content-Type'] = headers['Content-Type'] ?? 'application/json'
  }
  const token = getToken()
  if (token) headers.Authorization = `Bearer ${token}`

  const response = await fetch(path, { ...init, headers })
  if (!response.ok) {
    const text = await response.text()
    throw new Error(text || `Request failed: ${response.status}`)
  }
  if (response.status === 204) return undefined as T
  return response.json() as Promise<T>
}

export type Product = {
  id: string
  sku: string
  name: string
  description: string | null
  basePrice: number
  currency: string
  active: boolean
}

export type Order = {
  id: string
  status: string
  totalAmount: number
  currency: string
  customerId: string
  statusHistory: Array<{ toStatus: string; reason: string; createdAt: string }>
}

export type StockItem = {
  id: string
  warehouseId: string
  productId: string
  availableQty: number
  reservedQty: number
}

export async function login(username: string, password: string) {
  const res = await request<{ accessToken: string }>('/api/v1/auth/login', {
    method: 'POST',
    body: JSON.stringify({ username, password }),
  })
  setToken(res.accessToken)
  return res
}

export const listProducts = () => request<Product[]>('/api/v1/products')
export const createProduct = (body: object) =>
  request<Product>('/api/v1/products', { method: 'POST', body: JSON.stringify(body) })
export const listOrders = () => request<Order[]>('/api/v1/orders')
export const listStock = () => request<StockItem[]>('/api/v1/stock')
export const listWarehouses = () => request<Array<{ id: string; code: string; name: string }>>('/api/v1/warehouses')

export const askCopilot = (question: string) =>
  request<{
    summary: string
    confidence: string
    metrics: Record<string, unknown>
    tools: Array<{ name: string; result: Record<string, unknown> }>
    recommendations: string[]
  }>('/api/v1/ai/copilot/ask', { method: 'POST', body: JSON.stringify({ question }) })

export const investigate = (symptom: string) =>
  request<Record<string, unknown>>('/api/v1/ai/incidents/investigate', {
    method: 'POST',
    body: JSON.stringify({ symptom }),
  })

export const chaosStatus = () => request<Record<string, unknown>>('/api/v1/chaos/status')
export const chaosStart = (scenarioType: string, parameters: Record<string, unknown> = {}) =>
  request<Record<string, unknown>>('/api/v1/chaos/start', {
    method: 'POST',
    body: JSON.stringify({ scenarioType, parameters }),
  })
export const chaosReset = () => request<Record<string, unknown>>('/api/v1/chaos/reset', { method: 'POST' })

export const listDocuments = () =>
  request<Array<{ id: string; title: string; status: string; sourceName: string }>>('/api/v1/knowledge/documents')

export const queryKnowledge = (question: string) =>
  request<{ answer: string; confidence: string; citations: unknown[]; insufficientEvidence: boolean }>(
    '/api/v1/knowledge/query',
    { method: 'POST', body: JSON.stringify({ question, topK: 5 }) },
  )

export async function uploadDocument(file: File, title?: string) {
  const form = new FormData()
  form.append('file', file)
  if (title) form.append('title', title)
  return request('/api/v1/knowledge/documents', { method: 'POST', body: form })
}

export const concurrencyBurst = (body: object) =>
  request<Record<string, unknown>>('/api/v1/demo/concurrency-burst', {
    method: 'POST',
    body: JSON.stringify(body),
  })
