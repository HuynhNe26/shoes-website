<template>
  <main class="admin-shell">
    <header class="admin-topbar">
      <RouterLink to="/" class="brand">SHOES STUDIO</RouterLink>
      <strong>Operations</strong>
    </header>

    <section class="admin-hero">
      <div>
        <p>Revenue forecast</p>
        <h1>{{ money(analytics?.forecastNextMonthRevenue || 0) }}</h1>
      </div>
      <div class="admin-kpis">
        <article>
          <span>This month</span>
          <strong>{{ money(analytics?.currentMonthRevenue || 0) }}</strong>
        </article>
        <article>
          <span>Orders</span>
          <strong>{{ analytics?.currentMonthOrders || 0 }}</strong>
        </article>
        <article>
          <span>Last month</span>
          <strong>{{ money(analytics?.previousMonthRevenue || 0) }}</strong>
        </article>
      </div>
    </section>

    <section class="admin-grid">
      <article class="ops-panel wide">
        <div class="panel-head">
          <h2>AI insight</h2>
          <button @click="loadAnalytics">Refresh</button>
        </div>
        <p>{{ analytics?.aiInsight || 'Login as admin to load live insight.' }}</p>
      </article>

      <article class="ops-panel">
        <div class="panel-head">
          <h2>Low stock</h2>
          <span>{{ analytics?.lowStock?.length || 0 }}</span>
        </div>
        <div v-for="item in analytics?.lowStock || fallbackLowStock" :key="item.pvId" class="stock-row">
          <div>
            <strong>{{ item.productName }}</strong>
            <p>Size {{ item.size || '-' }} · {{ item.color || '-' }}</p>
          </div>
          <span>{{ item.stock }}</span>
        </div>
      </article>

      <article class="ops-panel">
        <div class="panel-head">
          <h2>Voucher</h2>
          <button @click="createVoucher">Create</button>
        </div>
        <label>
          Code
          <input v-model="voucher.voucherCode" />
        </label>
        <label>
          Discount %
          <input v-model.number="voucher.voucherDiscount" type="number" />
        </label>
        <label class="toggle-line">
          <input v-model="voucher.notifyVipDiamond" type="checkbox" />
          Notify VIP/DIAMOND
        </label>
      </article>

      <article class="ops-panel">
        <div class="panel-head">
          <h2>Limited product</h2>
          <button @click="createProduct">Create</button>
        </div>
        <label>
          Product name
          <input v-model="product.productName" />
        </label>
        <label>
          Price
          <input v-model.number="variant.price" type="number" />
        </label>
        <label class="toggle-line">
          <input v-model="product.limited" type="checkbox" />
          Limited launch
        </label>
      </article>
    </section>
  </main>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { createAdminProduct, createAdminVoucher, getAdminInsights } from '../../api/client'
import type { AdminAnalytics } from '../../types'

const fallbackLowStock = [
  { pvId: 1, productId: 101, productName: 'Aero Pulse Runner', size: 42, color: 'Crimson', stock: 3 },
  { pvId: 2, productId: 103, productName: 'Limited Night Fly', size: 41, color: 'Volt', stock: 2 },
]
const analytics = ref<AdminAnalytics>()
const voucher = ref({
  voucherCode: 'DIAMONDDROP',
  voucherType: true,
  voucherDiscount: 15,
  minOrderValue: 2000000,
  status: true,
  notifyVipDiamond: true,
})
const product = ref({
  productName: 'Limited Night Fly',
  description: 'Limited launch with reflective overlays.',
  image: {},
  imageDescription: {},
  limited: true,
  startTime: new Date().toISOString(),
  endTime: new Date(Date.now() + 1000 * 60 * 60 * 24 * 7).toISOString(),
})
const variant = ref({ price: 4990000, priceDiscount: 4590000, stock: 12, status: true })

onMounted(loadAnalytics)

async function loadAnalytics() {
  try {
    analytics.value = await getAdminInsights()
  } catch {
    analytics.value = {
      currentMonthRevenue: 186000000,
      previousMonthRevenue: 158000000,
      currentMonthOrders: 92,
      forecastNextMonthRevenue: 211000000,
      lowStock: fallbackLowStock,
      aiInsight: 'Revenue is trending up. Refill limited variants first and keep VIP voucher pressure below margin loss.',
    }
  }
}

async function createVoucher() {
  await createAdminVoucher(voucher.value)
}

async function createProduct() {
  await createAdminProduct({ ...product.value, variants: [variant.value] })
}

function money(value: number) {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(value)
}
</script>
