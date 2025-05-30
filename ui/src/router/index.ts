import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

// 路由组件
import Home from '@/views/Home.vue'
import Login from '@/views/Login.vue'
import Profile from '@/views/Profile.vue'
import Admin from '@/views/Admin.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home,
    meta: { title: '卡密发货系统' }
  },
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { title: '用户登录', guest: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: Profile,
    meta: { title: '个人中心', requiresAuth: true }
  },
  {
    path: '/admin',
    name: 'Admin',
    component: Admin,
    meta: { title: '管理后台', requiresAuth: true, requiresAdmin: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()
  
  // 确保认证状态已初始化（处理页面刷新的情况）
  if (!authStore.isLoggedIn && localStorage.getItem('token')) {
    authStore.initAuth()
  }
  
  // 设置页面标题
  document.title = to.meta.title as string || '卡密发货系统'
  
  // 如果已登录用户访问guest页面（登录/注册），重定向到首页
  if (to.meta.guest && authStore.isLoggedIn) {
    next('/')
    return
  }
  
  // 如果页面需要认证
  if (to.meta.requiresAuth) {
    if (!authStore.isLoggedIn) {
      // 未登录，重定向到登录页
      next('/login')
      return
    }
    
    // 验证token有效性
    const isValid = await authStore.checkAuth()
    if (!isValid) {
      next('/login')
      return
    }
    
    // 如果需要管理员权限
    if (to.meta.requiresAdmin && !authStore.isAdmin) {
      // 权限不足，重定向到首页
      next('/')
      return
    }
  }
  
  next()
})

export default router 