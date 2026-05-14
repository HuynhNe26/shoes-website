<template>
  <main class="storefront">
    <header class="topbar">
      <RouterLink to="/" class="brand">SHOES STUDIO</RouterLink>
      <nav class="navlinks">
        <a href="#drops">Drops</a>
        <a href="#sale">Sale</a>
        <a href="#vouchers">Vouchers</a>
        <RouterLink to="/cart">Bag</RouterLink>
        <RouterLink to="/admin">Admin</RouterLink>
      </nav>
      <div class="top-actions">
        <button class="icon-button" title="Search" @click="focusSearch">⌕</button>
        <RouterLink class="cart-link" to="/login">Login</RouterLink>
      </div>
    </header>

    <section class="hero-shell">
      <div class="hero-copy">
        <span class="eyebrow">Limited running drop</span>
        <h1>Aero Pulse Runner</h1>
        <p>Responsive cushioning, fast color, clean street shape. Built for people who move every day.</p>
        <div class="hero-actions">
          <a class="primary-action" href="#drops">Shop now</a>
          <a class="ghost-action" href="#ai">Ask AI</a>
        </div>
      </div>
      <div class="hero-media">
        <img :src="fallbackProducts[0].displayImage || ''" alt="Aero Pulse Runner sneaker" />
      </div>
    </section>

    <section class="filters">
      <input ref="searchInput" v-model="keyword" type="search" placeholder="Search running, court, limited" @keyup.enter="loadProducts" />
      <div class="segments">
        <button :class="{ active: feed === 'all' }" @click="feed = 'all'">All</button>
        <button :class="{ active: feed === 'sale' }" @click="feed = 'sale'">Sale</button>
        <button :class="{ active: feed === 'limited' }" @click="feed = 'limited'">Limited</button>
      </div>
    </section>

    <section id="drops" class="product-band">
      <div class="section-heading">
        <p>New season</p>
        <h2>Built for motion</h2>
      </div>
      <div class="product-grid">
        <article v-for="product in visibleProducts" :key="product.productId" class="product-card">
          <RouterLink class="product-image" :to="`/products/${product.productId}`">
            <img :src="imageFor(product)" :alt="product.productName" />
            <span v-if="product.limited" class="badge">Limited</span>
          </RouterLink>
          <div class="product-info">
            <div>
              <h3>{{ product.productName }}</h3>
              <p>{{ product.colors?.join(' / ') || 'Seasonal colorway' }}</p>
            </div>
            <div class="price">
              <span v-if="product.salePrice">{{ money(product.salePrice) }}</span>
              <span :class="{ strike: product.salePrice }">{{ money(product.minPrice || product.salePrice || 0) }}</span>
            </div>
          </div>
          <div class="product-meta">
            <span>{{ product.sizes?.slice(0, 5).join(', ') || '38-44' }}</span>
            <button @click="primeAi(product)">Ask fit</button>
          </div>
        </article>
      </div>
    </section>

    <section id="sale" class="promo-band">
      <div>
        <p>Today only</p>
        <h2>Sale rotation</h2>
      </div>
      <div class="sale-row">
        <article v-for="product in saleProducts" :key="`sale-${product.productId}`">
          <img :src="imageFor(product)" :alt="product.productName" />
          <span>{{ product.productName }}</span>
          <strong>{{ money(product.salePrice || product.minPrice || 0) }}</strong>
        </article>
      </div>
    </section>

    <section id="vouchers" class="voucher-band">
      <article v-for="voucher in vouchers" :key="voucher.voucherId" class="voucher-card">
        <span>{{ voucher.voucherCode }}</span>
        <strong>{{ voucher.voucherType ? `${voucher.voucherDiscount}%` : money(voucher.voucherDiscount || 0) }}</strong>
        <p>{{ voucher.description || 'Member reward' }}</p>
      </article>
    </section>

    <section id="ai" class="ai-panel">
      <div class="ai-header">
        <div>
          <p>AI stylist</p>
          <h2>Find the right pair</h2>
        </div>
        <span>{{ aiSession ? 'Live' : 'Ready' }}</span>
      </div>
      <div class="messages">
        <div v-for="message in messages" :key="message.id" :class="['message', message.role]">
          {{ message.text }}
        </div>
      </div>
      <div v-if="lastAi?.suggestedProducts?.length" class="ai-suggestions">
        <button v-for="product in lastAi.suggestedProducts" :key="product.productId" @click="primeAi(product)">
          {{ product.productName }}
        </button>
      </div>
      <form class="chat-form" @submit.prevent="sendAi">
        <input v-model="aiInput" placeholder="Ask for size, style, or add to cart" />
        <button>Send</button>
      </form>
      <button v-if="lastAi?.canAddToCart && lastAi.pvId && lastAi.quantity" class="save-cart" @click="saveAiCart">
        Save to bag
      </button>
    </section>
  </main>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { addCartItem, aiChat, getLimitedProducts, getProducts, getSaleProducts, getVouchers } from '../../api/client'
import type { AiChatResponse, ProductCard, Voucher } from '../../types'

const fallbackProducts: ProductCard[] = [
  {
    productId: 101,
    productName: 'Aero Pulse Runner',
    description: 'Lightweight knit upper, sculpted foam, everyday speed.',
    image: null,
    displayImage: 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=1200&q=85',
    limited: false,
    startTime: null,
    endTime: null,
    minPrice: 3290000,
    salePrice: 2790000,
    stock: 42,
    soldQuantity: 218,
    colors: ['Crimson', 'Black', 'White'],
    sizes: [39, 40, 41, 42, 43],
  },
  {
    productId: 102,
    productName: 'Court Nova Low',
    description: 'Clean leather panels and sharp court grip.',
    image: null,
    displayImage: 'https://images.unsplash.com/photo-1608231387042-66d1773070a5?auto=format&fit=crop&w=1200&q=85',
    limited: false,
    startTime: null,
    endTime: null,
    minPrice: 2490000,
    salePrice: null,
    stock: 28,
    soldQuantity: 146,
    colors: ['White', 'Green'],
    sizes: [38, 39, 40, 41, 42],
  },
  {
    productId: 103,
    productName: 'Limited Night Fly',
    description: 'Carbon snap with reflective overlays.',
    image: null,
    displayImage: 'https://images.unsplash.com/photo-1600185365483-26d7a4cc7519?auto=format&fit=crop&w=1200&q=85',
    limited: true,
    startTime: null,
    endTime: null,
    minPrice: 4990000,
    salePrice: 4590000,
    stock: 9,
    soldQuantity: 304,
    colors: ['Black', 'Volt'],
    sizes: [40, 41, 42, 43],
  },
]

const products = ref<ProductCard[]>(fallbackProducts)
const saleProducts = ref<ProductCard[]>(fallbackProducts.filter((product) => product.salePrice))
const limitedProducts = ref<ProductCard[]>(fallbackProducts.filter((product) => product.limited))
const vouchers = ref<Voucher[]>([])
const feed = ref<'all' | 'sale' | 'limited'>('all')
const keyword = ref('')
const searchInput = ref<HTMLInputElement | null>(null)
const aiInput = ref('')
const aiSession = ref<string>()
const lastAi = ref<AiChatResponse>()
const messages = ref<Array<{ id: number; role: 'user' | 'ai'; text: string }>>([
  { id: 1, role: 'ai', text: 'Tell me your size, color, and how you move. I will narrow the field.' },
])

const visibleProducts = computed(() => {
  const list = feed.value === 'sale' ? saleProducts.value : feed.value === 'limited' ? limitedProducts.value : products.value
  const term = keyword.value.trim().toLowerCase()
  if (!term) return list
  return list.filter((product) => `${product.productName} ${product.description}`.toLowerCase().includes(term))
})

onMounted(async () => {
  await loadProducts()
  try {
    saleProducts.value = await getSaleProducts()
    limitedProducts.value = await getLimitedProducts()
    vouchers.value = await getVouchers()
  } catch {
    vouchers.value = [
      {
        voucherId: 1,
        voucherCode: 'VIPMOVE',
        voucherType: true,
        voucherDiscount: 12,
        minOrderValue: 1500000,
        maxReductionValue: null,
        quantity: null,
        usedQuantity: null,
        description: 'Member release reward',
        contributor: null,
        contributorImage: null,
        voucherStart: null,
        voucherEnd: null,
        status: true,
      },
    ]
  }
})

async function loadProducts() {
  try {
    products.value = await getProducts(keyword.value)
  } catch {
    products.value = fallbackProducts
  }
}

function imageFor(product: ProductCard) {
  return product.displayImage || fallbackProducts[0].displayImage || ''
}

function money(value: number) {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(value)
}

function focusSearch() {
  searchInput.value?.focus()
}

function primeAi(product: ProductCard) {
  aiInput.value = `Tu van cho toi ${product.productName}, size ${product.sizes?.[0] || 40}, mau ${product.colors?.[0] || 'den'}`
  document.getElementById('ai')?.scrollIntoView({ behavior: 'smooth', block: 'center' })
}

async function sendAi() {
  const text = aiInput.value.trim()
  if (!text) return
  messages.value.push({ id: Date.now(), role: 'user', text })
  aiInput.value = ''
  try {
    const response = await aiChat({ sessionId: aiSession.value, message: text })
    aiSession.value = response.sessionId
    lastAi.value = response
    messages.value.push({ id: Date.now() + 1, role: 'ai', text: response.answer })
  } catch {
    messages.value.push({ id: Date.now() + 1, role: 'ai', text: 'Pick a pair and tell me your size and color. I can prepare the cart draft after login.' })
  }
}

async function saveAiCart() {
  if (!lastAi.value?.pvId || !lastAi.value.quantity) return
  await addCartItem({ pvId: lastAi.value.pvId, quantity: lastAi.value.quantity })
}
</script>
