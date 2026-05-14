<template>
  <main class="cart-page">
    <RouterLink to="/" class="back-link">← Continue shopping</RouterLink>
    <h1>Your bag</h1>
    <section class="cart-layout">
      <div class="cart-list">
        <article v-for="item in cart.items" :key="item.id || item.itemId" class="bag-item large">
          <img :src="item.image || fallbackImage" :alt="item.productName" />
          <div>
            <strong>{{ item.productName }}</strong>
            <p>Size {{ item.size || '-' }} · {{ item.color || 'Colorway' }}</p>
            <span>{{ item.quantity }} × {{ money(item.unitPrice) }}</span>
          </div>
          <button @click="remove(item.id || item.itemId || 0)">Remove</button>
        </article>
      </div>
      <aside class="summary-panel">
        <label>
          Voucher
          <input v-model="voucherCode" placeholder="VIPMOVE" />
        </label>
        <strong>{{ money(cart.subTotal) }}</strong>
        <button @click="checkoutCart">Checkout</button>
      </aside>
    </section>
  </main>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { checkout, getCart, removeCartItem } from '../../api/client'
import type { Cart } from '../../types'

const fallbackImage = 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=1200&q=85'
const cart = ref<Cart>({ cartId: null, items: [], subTotal: 0 })
const voucherCode = ref('')

onMounted(load)

async function load() {
  try {
    cart.value = await getCart()
  } catch {
    cart.value = { cartId: null, items: [], subTotal: 0 }
  }
}

async function remove(itemId: number) {
  if (!itemId) return
  cart.value = await removeCartItem(itemId)
}

async function checkoutCart() {
  await checkout({ voucherCode: voucherCode.value, destinationLatitude: 10.7769, destinationLongitude: 106.7009 })
  await load()
}

function money(value: number) {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(value)
}
</script>
