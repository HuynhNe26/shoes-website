<template>
  <main class="login-page">
    <RouterLink to="/" class="brand">SHOES STUDIO</RouterLink>
    <section class="login-card">
      <p>Member access</p>
      <h1>Welcome back</h1>
      <form @submit.prevent="submit">
        <input v-model="email" type="email" placeholder="Email" />
        <input v-model="password" type="password" placeholder="Password" />
        <button>Continue</button>
      </form>
      <div class="socials">
        <button @click="social('google')">Google</button>
        <button @click="social('apple')">Apple</button>
      </div>
    </section>
  </main>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { login, register, socialLogin } from '../../api/client'

const router = useRouter()
const email = ref('')
const password = ref('')

async function submit() {
  try {
    await login({ email: email.value, password: password.value })
  } catch {
    await register({ email: email.value, password: password.value, firstName: 'New', lastName: 'Member' })
  }
  await router.push('/')
}

async function social(provider: 'google' | 'apple') {
  const idToken = window.prompt(`${provider} id token`)
  if (!idToken) return
  await socialLogin(provider, idToken)
  await router.push('/')
}
</script>
