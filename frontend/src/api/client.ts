import type {
  AdminAnalytics,
  AiChatResponse,
  AuthUser,
  Cart,
  OrderResponse,
  ProductCard,
  ProductDetail,
  ShippingQuote,
  Voucher,
} from '../types'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'

async function api<T>(path: string, options: RequestInit = {}): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
      ...(options.headers ?? {}),
    },
    ...options,
  })

  if (!response.ok) {
    const text = await response.text()
    throw new Error(text || `HTTP ${response.status}`)
  }

  if (response.status === 204) {
    return undefined as T
  }

  return response.json() as Promise<T>
}

export function getMe() {
  return api<AuthUser>('/api/auth/me')
}

export function login(payload: { email: string; password: string }) {
  return api<AuthUser>('/api/auth/login', { method: 'POST', body: JSON.stringify(payload) })
}

export function register(payload: Record<string, unknown>) {
  return api<AuthUser>('/api/auth/register', { method: 'POST', body: JSON.stringify(payload) })
}

export function socialLogin(provider: 'google' | 'apple', idToken: string) {
  return api<AuthUser>(`/api/auth/${provider}`, { method: 'POST', body: JSON.stringify({ idToken }) })
}

export function logout() {
  return api<void>('/api/auth/logout', { method: 'POST' })
}

export function getProducts(keyword = '') {
  const query = keyword ? `?keyword=${encodeURIComponent(keyword)}` : ''
  return api<ProductCard[]>(`/api/catalog/products${query}`)
}

export function getSaleProducts() {
  return api<ProductCard[]>('/api/catalog/products/sale')
}

export function getLimitedProducts() {
  return api<ProductCard[]>('/api/catalog/products/limited')
}

export function getBestSellers() {
  return api<ProductCard[]>('/api/catalog/products/best-sellers?size=8')
}

export function getProduct(productId: string | number) {
  return api<ProductDetail>(`/api/catalog/products/${productId}`)
}

export function getRecommendations(gender = '', interests = '') {
  const params = new URLSearchParams()
  if (gender) params.set('gender', gender)
  if (interests) params.set('interests', interests)
  params.set('size', '8')
  return api<ProductCard[]>(`/api/catalog/products/recommendations?${params.toString()}`)
}

export function getCart() {
  return api<Cart>('/api/cart')
}

export function addCartItem(payload: { pvId: number; quantity: number; note?: string }) {
  return api<Cart>('/api/cart/items', { method: 'POST', body: JSON.stringify(payload) })
}

export function updateCartItem(itemId: number, payload: { quantity: number; note?: string }) {
  return api<Cart>(`/api/cart/items/${itemId}`, { method: 'PATCH', body: JSON.stringify(payload) })
}

export function removeCartItem(itemId: number) {
  return api<Cart>(`/api/cart/items/${itemId}`, { method: 'DELETE' })
}

export function getVouchers() {
  return api<Voucher[]>('/api/vouchers/active')
}

export function getShippingQuote(payload: { destinationLatitude: number; destinationLongitude: number }) {
  return api<ShippingQuote>('/api/shipping/quote', { method: 'POST', body: JSON.stringify(payload) })
}

export function checkout(payload: {
  voucherCode?: string
  destinationLatitude?: number
  destinationLongitude?: number
  note?: string
}) {
  return api<OrderResponse>('/api/orders/checkout', { method: 'POST', body: JSON.stringify(payload) })
}

export function aiChat(payload: { sessionId?: string; message: string; pvId?: number | null; quantity?: number | null }) {
  return api<AiChatResponse>('/api/ai/chat', { method: 'POST', body: JSON.stringify(payload) })
}

export function getAdminInsights() {
  return api<AdminAnalytics>('/api/admin/analytics/insights')
}

export function createAdminProduct(payload: Record<string, unknown>) {
  return api<ProductDetail>('/api/admin/products', { method: 'POST', body: JSON.stringify(payload) })
}

export function createAdminVoucher(payload: Record<string, unknown>) {
  return api<Voucher>('/api/admin/vouchers', { method: 'POST', body: JSON.stringify(payload) })
}
