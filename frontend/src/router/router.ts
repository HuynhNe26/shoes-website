import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import Storefront from '../views/user/Storefront.vue'
import ProductDetail from '../views/user/ProductDetail.vue'
import CartView from '../views/user/CartView.vue'
import LoginView from '../views/user/LoginView.vue'
import AdminDashboard from '../views/admin/AdminDashboard.vue'

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    name: 'storefront',
    component: Storefront,
  },
  {
    path: '/products/:id',
    name: 'product-detail',
    component: ProductDetail,
  },
  {
    path: '/cart',
    name: 'cart',
    component: CartView,
  },
  {
    path: '/login',
    name: 'login',
    component: LoginView,
  },
  {
    path: '/admin',
    name: 'admin',
    component: AdminDashboard,
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
