<template>
  <div class="site-shell">
    <header class="topbar">
      <RouterLink class="brand" to="/">SoleMotion</RouterLink>
      <nav class="nav-links">
        <RouterLink to="/">Shop</RouterLink>
        <RouterLink to="/login">Account</RouterLink>
      </nav>
    </header>

    <main class="checkout-layout">
      <section class="cart-list">
        <div class="section-heading">
          <p class="eyebrow">Bag</p>
          <h1>Your order</h1>
        </div>
        <article v-for="item in cart.items" :key="item.id" class="cart-item">
          <img :src="item.image || fallbackImage" :alt="item.productName" />
          <div>
            <h3>{{ item.productName }}</h3>
            <p>EU {{ item.size ?? '-' }} / {{ item.color ?? 'Default' }}</p>
            <p>{{ item.limited ? 'Limited max 3' : 'Regular max 5' }}</p>
          </div>
          <input v-model.number="item.quantity" min="1" type="number" @change="changeQuantity(item.id, item.quantity)" />
          <strong>{{ money(item.totalPrice) }}</strong>
          <button class="icon-button" type="button" @click="removeItem(item.id)">Remove</button>
        </article>
        <p v-if="cart.items.length === 0" class="empty-state">Your cart is empty.</p>
      </section>

      <aside class="summary-panel">
        <h2>Summary</h2>
        <label>
          Voucher
          <select v-model="voucherCode">
            <option value="">No voucher</option>
            <option v-for="voucher in vouchers" :key="voucher.voucherId" :value="voucher.voucherCode">
              {{ voucher.voucherCode }}
            </option>
          </select>
        </label>
        <div class="coordinate-grid">
          <label>
            Latitude
            <input v-model.number="latitude" type="number" step="0.000001" />
          </label>
          <label>
            Longitude
            <input v-model.number="longitude" type="number" step="0.000001" />
          </label>
        </div>
        <button class="ghost-button" type="button" @click="quoteShipping">Quote shipping</button>
        <div v-if="quote" class="quote-box">
          <span>{{ quote.distanceKm }} km</span>
          <strong>{{ quote.freeShip ? 'Freeship' : money(quote.transferPrice) }}</strong>
          <small>{{ quote.membership }} discount {{ quote.membershipDiscountPercent }}%</small>
        </div>
        <div class="summary-lines">
          <span>Subtotal</span>
          <strong>{{ money(cart.subTotal) }}</strong>
          <span>Shipping</span>
          <strong>{{ quote ? money(quote.transferPrice) : '--' }}</strong>
        </div>
        <button class="solid-button full" type="button" :disabled="cart.items.length === 0" @click="placeOrder">Checkout</button>
        <div v-if="lastOrder" class="success-box">
          <strong>{{ lastOrder.orderCode }}</strong>
          <span>{{ money(lastOrder.totalPrice) }}</span>
        </div>
      </aside>
    </main>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { checkout, getCart, getShippingQuote, getVouchers, removeCartItem, updateCartItem } from '../../api/client'
import type { Cart, OrderResponse, ShippingQuote, Voucher } from '../../types'

const router = useRouter()
const cart = ref<Cart>({ cartId: null, items: [], subTotal: 0 })
const vouchers = ref<Voucher[]>([])
const voucherCode = ref('')
const latitude = ref(10.7769)
const longitude = ref(106.7009)
const quote = ref<ShippingQuote | null>(null)
const lastOrder = ref<OrderResponse | null>(null)
const fallbackImage = 'https://images.unsplash.com/photo-1600185365926-3a2ce3cdb9eb?auto=format&fit=crop&w=800&q=80'

onMounted(load)

async function load() {
  try {
    const [cartResponse, voucherResponse] = await Promise.all([getCart(), getVouchers()])
    cart.value = cartResponse
    vouchers.value = voucherResponse
  } catch {
    await router.push('/login')
  }
}

async function changeQuantity(itemId: number, quantity: number) {
  cart.value = await updateCartItem(itemId, { quantity })
}

async function removeItem(itemId: number) {
  cart.value = await removeCartItem(itemId)
}

async function quoteShipping() {
  quote.value = await getShippingQuote({ destinationLatitude: latitude.value, destinationLongitude: longitude.value })
}

async function placeOrder() {
  if (!quote.value) {
    await quoteShipping()
  }
  lastOrder.value = await checkout({
    voucherCode: voucherCode.value || undefined,
    destinationLatitude: latitude.value,
    destinationLongitude: longitude.value,
  })
  cart.value = { cartId: null, items: [], subTotal: 0 }
}

function money(value: number | null | undefined) {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(value ?? 0)
}
</script>
