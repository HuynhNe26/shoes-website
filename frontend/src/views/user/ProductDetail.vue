<template>
  <main class="detail-shell">
    <RouterLink to="/" class="back-link">← Back</RouterLink>
    <section class="detail-grid">
      <div class="detail-media">
        <img :src="heroImage" :alt="product?.productName || 'Product'" />
      </div>
      <div class="detail-info">
        <span v-if="product?.limited" class="badge inline">Limited</span>
        <h1>{{ product?.productName || 'Aero Pulse Runner' }}</h1>
        <p>{{ product?.description || 'Responsive cushioning with sharp everyday shape.' }}</p>
        <div class="variant-list">
          <button
            v-for="variant in variants"
            :key="variant.variantId"
            :class="{ active: selectedVariant?.variantId === variant.variantId }"
            @click="selectedVariant = variant"
          >
            {{ variant.size || '-' }} · {{ variant.color || 'Color' }}
          </button>
        </div>
        <div class="detail-price">{{ money(selectedVariant?.finalPrice || selectedVariant?.price || 0) }}</div>
        <button class="primary-action detail-add" @click="addToCart">Add to bag</button>
      </div>
    </section>
  </main>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { addCartItem, getProduct } from '../../api/client'
import type { ProductDetail, Variant } from '../../types'

const route = useRoute()
const product = ref<ProductDetail>()
const selectedVariant = ref<Variant>()
const fallbackImage = 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=1200&q=85'

const variants = computed(() => product.value?.variants || [])
const heroImage = computed(() => selectedVariant.value?.image || firstImage(product.value?.image) || fallbackImage)

onMounted(async () => {
  try {
    product.value = await getProduct(String(route.params.id))
    selectedVariant.value = product.value.variants[0]
  } catch {
    product.value = {
      productId: Number(route.params.id),
      productName: 'Aero Pulse Runner',
      description: 'Responsive cushioning with sharp everyday shape.',
      image: null,
      imageDescription: null,
      limited: false,
      startTime: null,
      endTime: null,
      variants: [
        { variantId: 1, price: 3290000, priceDiscount: 2790000, finalPrice: 2790000, sizeId: 1, size: 42, colorId: 1, color: 'Crimson', image: fallbackImage, stock: 8, soldQuantity: 12, status: true },
      ],
      recommendations: [],
    }
    selectedVariant.value = product.value.variants[0]
  }
})

function firstImage(image?: Record<string, unknown> | null) {
  if (!image) return null
  const value = Object.values(image).find(Boolean)
  return typeof value === 'string' ? value : null
}

function money(value: number) {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(value)
}

async function addToCart() {
  if (!selectedVariant.value) return
  await addCartItem({ pvId: selectedVariant.value.variantId, quantity: 1 })
}
</script>
