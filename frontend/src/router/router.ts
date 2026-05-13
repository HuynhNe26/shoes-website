import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import home from '../views/user/home.vue'

// Nên thêm kiểu RouteRecordRaw để TS hỗ trợ tốt hơn
const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    component: home, 
  } // Thiếu đóng ngoặc nhọn này
] // Thiếu đóng ngoặc vuông này

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router