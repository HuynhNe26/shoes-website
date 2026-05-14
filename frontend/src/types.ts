export interface AuthUser {
  id: number
  email: string
  firstName: string | null
  lastName: string | null
  role: string
  membership: string | null
  point: number | null
}

export interface ProductCard {
  productId: number
  productName: string
  description: string | null
  image: Record<string, unknown> | null
  displayImage: string | null
  limited: boolean | null
  startTime: string | null
  endTime: string | null
  minPrice: number | null
  salePrice: number | null
  stock: number | null
  soldQuantity: number | null
  colors: string[]
  sizes: number[]
}

export interface Variant {
  variantId: number
  price: number | null
  priceDiscount: number | null
  finalPrice: number | null
  sizeId: number | null
  size: number | null
  colorId: number | null
  color: string | null
  image: string | null
  stock: number | null
  soldQuantity: number | null
  status: boolean | null
}

export interface ProductDetail {
  productId: number
  productName: string
  description: string | null
  image: Record<string, unknown> | null
  imageDescription: Record<string, unknown> | null
  limited: boolean | null
  startTime: string | null
  endTime: string | null
  variants: Variant[]
  recommendations: ProductCard[]
}

export interface CartItem {
  id: number
  pvId: number
  productId: number
  productName: string
  image: string | null
  size: number | null
  color: string | null
  quantity: number
  unitPrice: number
  totalPrice: number
  note: string | null
  limited: boolean | null
}

export interface Cart {
  cartId: number | null
  items: CartItem[]
  subTotal: number
}

export interface Voucher {
  voucherId: number
  voucherCode: string
  voucherType: boolean | null
  voucherDiscount: number | null
  minOrderValue: number | null
  maxReductionValue: number | null
  quantity: number | null
  usedQuantity: number | null
  description: string | null
  contributor: string | null
  contributorImage: string | null
  voucherStart: string | null
  voucherEnd: string | null
  status: boolean | null
}

export interface ShippingQuote {
  distanceKm: number
  transferPrice: number
  membership: string
  membershipDiscountPercent: number
  freeShip: boolean
}

export interface OrderResponse {
  orderId: number
  orderCode: string
  status: string
  createdAt: string
  subTotal: number
  voucherDiscount: number
  membershipDiscount: number
  taxPrice: number
  transferPrice: number
  totalPrice: number
}

export interface AiChatResponse {
  sessionId: string
  answer: string
  intent: string
  canAddToCart: boolean
  pvId: number | null
  quantity: number | null
  cskhRoomId: string | null
  suggestedProducts: ProductCard[]
}

export interface LowStock {
  pvId: number
  productId: number
  productName: string
  size: number | null
  color: string | null
  stock: number | null
}

export interface AdminAnalytics {
  currentMonthRevenue: number
  previousMonthRevenue: number
  currentMonthOrders: number
  forecastNextMonthRevenue: number
  lowStock: LowStock[]
  aiInsight: string
}
