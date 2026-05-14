<template>
  <div class="admin-shell">
    <aside class="admin-sidebar">
      <RouterLink class="brand" to="/">SoleMotion</RouterLink>
      <RouterLink to="/">Storefront</RouterLink>
      <a href="#analytics">Analytics</a>
      <a href="#products">Products</a>
      <a href="#vouchers">Vouchers</a>
    </aside>

    <main class="admin-main">
      <section id="analytics" class="admin-hero">
        <div>
          <p class="eyebrow">Admin AI</p>
          <h1>Operations cockpit</h1>
        </div>
        <button class="solid-button" type="button" @click="loadInsights">Refresh</button>
      </section>

      <section v-if="analytics" class="metric-grid">
        <div class="metric-card">
          <span>This month</span>
          <strong>{{ money(analytics.currentMonthRevenue) }}</strong>
        </div>
        <div class="metric-card">
          <span>Orders</span>
          <strong>{{ analytics.currentMonthOrders }}</strong>
        </div>
        <div class="metric-card">
          <span>Next month forecast</span>
          <strong>{{ money(analytics.forecastNextMonthRevenue) }}</strong>
        </div>
        <div class="metric-card wide">
          <span>AI insight</span>
          <p>{{ analytics.aiInsight }}</p>
        </div>
      </section>

      <section v-if="analytics?.lowStock.length" class="admin-section">
        <div class="section-heading">
          <p class="eyebrow">Inventory</p>
          <h2>Low stock alerts</h2>
        </div>
        <div class="table-like">
          <div v-for="item in analytics.lowStock" :key="item.pvId" class="table-row">
            <span>{{ item.productName }}</span>
            <span>EU {{ item.size ?? '-' }}</span>
            <span>{{ item.color ?? 'Default' }}</span>
            <strong>{{ item.stock ?? 0 }} left</strong>
          </div>
        </div>
      </section>

      <section id="products" class="admin-section form-section">
        <div class="section-heading">
          <p class="eyebrow">Catalog</p>
          <h2>Create product</h2>
        </div>
        <form class="admin-form" @submit.prevent="saveProduct">
          <input v-model="productName" placeholder="Product name" />
          <textarea v-model="description" placeholder="Description"></textarea>
          <div class="two-col">
            <input v-model.number="price" placeholder="Price" type="number" />
            <input v-model.number="priceDiscount" placeholder="Sale price" type="number" />
          </div>
          <div class="two-col">
            <input v-model.number="sizeId" placeholder="Size ID" type="number" />
            <input v-model.number="colorId" placeholder="Color ID" type="number" />
          </div>
          <input v-model="image" placeholder="Variant image URL" />
          <div class="two-col">
            <input v-model.number="stock" placeholder="Stock" type="number" />
            <label class="check-line">
              <input v-model="limited" type="checkbox" />
              Limited drop
            </label>
          </div>
          <div class="two-col">
            <input v-model="startTime" type="datetime-local" />
            <input v-model="endTime" type="datetime-local" />
          </div>
          <button class="solid-button full" type="submit">Save product</button>
        </form>
      </section>

      <section id="vouchers" class="admin-section form-section">
        <div class="section-heading">
          <p class="eyebrow">Campaign</p>
          <h2>Create voucher</h2>
        </div>
        <form class="admin-form" @submit.prevent="saveVoucher">
          <input v-model="voucherCode" placeholder="Voucher code" />
          <textarea v-model="voucherDescription" placeholder="Voucher description"></textarea>
          <div class="two-col">
            <input v-model.number="voucherDiscount" placeholder="Discount" type="number" />
            <input v-model.number="minOrderValue" placeholder="Min order value" type="number" />
          </div>
          <label class="check-line">
            <input v-model="notifyVipDiamond" type="checkbox" />
            Email VIP and DIAMOND customers
          </label>
          <button class="solid-button full" type="submit">Publish voucher</button>
        </form>
      </section>
      <p v-if="notice" class="success-box">{{ notice }}</p>
    </main>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { createAdminProduct, createAdminVoucher, getAdminInsights } from '../../api/client'
import type { AdminAnalytics } from '../../types'

const router = useRouter()
const analytics = ref<AdminAnalytics | null>(null)
const notice = ref('')
const productName = ref('')
const description = ref('')
const image = ref('')
const price = ref<number | null>(null)
const priceDiscount = ref<number | null>(null)
const sizeId = ref<number | null>(null)
const colorId = ref<number | null>(null)
const stock = ref<number | null>(0)
const limited = ref(false)
const startTime = ref('')
const endTime = ref('')
const voucherCode = ref('')
const voucherDescription = ref('')
const voucherDiscount = ref<number | null>(10)
const minOrderValue = ref<number | null>(0)
const notifyVipDiamond = ref(true)

onMounted(loadInsights)

async function loadInsights() {
  try {
    analytics.value = await getAdminInsights()
  } catch {
    await router.push('/login')
  }
}

async function saveProduct() {
  await createAdminProduct({
    productName: productName.value,
    description: description.value,
    image: image.value ? { main: image.value } : null,
    imageDescription: null,
    limited: limited.value,
    startTime: toIso(startTime.value),
    endTime: toIso(endTime.value),
    variants: [
      {
        price: price.value,
        priceDiscount: priceDiscount.value,
        sizeId: sizeId.value,
        colorId: colorId.value,
        image: image.value,
        stock: stock.value,
        status: true,
      },
    ],
  })
  notice.value = 'Product saved.'
  await loadInsights()
}

async function saveVoucher() {
  await createAdminVoucher({
    voucherCode: voucherCode.value,
    voucherType: true,
    voucherDiscount: voucherDiscount.value,
    minOrderValue: minOrderValue.value,
    maxReductionValue: null,
    quantity: 100,
    description: voucherDescription.value,
    contributor: 'SoleMotion',
    contributorImage: null,
    voucherStart: new Date().toISOString(),
    voucherEnd: new Date(Date.now() + 1000 * 60 * 60 * 24 * 14).toISOString(),
    status: true,
    notifyVipDiamond: notifyVipDiamond.value,
  })
  notice.value = 'Voucher published.'
}

function toIso(value: string) {
  return value ? new Date(value).toISOString() : null
}

function money(value: number | null | undefined) {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(value ?? 0)
}
</script>
