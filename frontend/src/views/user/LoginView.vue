<template>
  <div class="auth-screen">
    <RouterLink class="brand" to="/">SoleMotion</RouterLink>
    <section class="auth-panel">
      <div>
        <p class="eyebrow">Member access</p>
        <h1>{{ isRegister ? 'Create account' : 'Welcome back' }}</h1>
      </div>

      <form class="auth-form" @submit.prevent="submit">
        <div class="two-col" v-if="isRegister">
          <input v-model="firstName" placeholder="First name" />
          <input v-model="lastName" placeholder="Last name" />
        </div>
        <input v-model="email" autocomplete="email" placeholder="Email" type="email" />
        <input v-model="password" autocomplete="current-password" placeholder="Password" type="password" />
        <div class="two-col" v-if="isRegister">
          <input v-model="gender" placeholder="Gender" />
          <input v-model="phoneNumber" placeholder="Phone" />
        </div>
        <input v-if="isRegister" v-model="address" placeholder="Address" />
        <button class="solid-button full" type="submit">{{ isRegister ? 'Register' : 'Sign in' }}</button>
      </form>

      <div class="social-auth">
        <input v-model="socialToken" placeholder="Google or Apple identity token" />
        <div class="two-col">
          <button class="ghost-button" type="button" @click="loginSocial('google')">Google</button>
          <button class="ghost-button" type="button" @click="loginSocial('apple')">Apple</button>
        </div>
      </div>

      <button class="link-button" type="button" @click="isRegister = !isRegister">
        {{ isRegister ? 'I already have an account' : 'Create a new account' }}
      </button>
      <p v-if="error" class="form-error">{{ error }}</p>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { login, register, socialLogin } from '../../api/client'

const router = useRouter()
const isRegister = ref(false)
const email = ref('')
const password = ref('')
const firstName = ref('')
const lastName = ref('')
const gender = ref('')
const phoneNumber = ref('')
const address = ref('')
const socialToken = ref('')
const error = ref('')

async function submit() {
  error.value = ''
  try {
    if (isRegister.value) {
      await register({
        email: email.value,
        password: password.value,
        firstName: firstName.value,
        lastName: lastName.value,
        gender: gender.value,
        phoneNumber: phoneNumber.value,
        address: address.value,
      })
    } else {
      await login({ email: email.value, password: password.value })
    }
    await router.push('/')
  } catch (exception) {
    error.value = exception instanceof Error ? exception.message : 'Cannot sign in'
  }
}

async function loginSocial(provider: 'google' | 'apple') {
  if (!socialToken.value.trim()) {
    error.value = 'Identity token is required'
    return
  }
  await socialLogin(provider, socialToken.value.trim())
  await router.push('/')
}
</script>
