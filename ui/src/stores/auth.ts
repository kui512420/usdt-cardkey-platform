import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User, LoginRequest } from '@/api/auth'
import { login, logout, getCurrentUser, validateToken } from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  // 状态
  const user = ref<User | null>(null)
  const token = ref<string | null>(null)
  const loading = ref(false)

  // 计算属性
  const isLoggedIn = computed(() => !!user.value && !!token.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')
  const userDisplayName = computed(() => {
    if (!user.value) return ''
    return user.value.nickname || user.value.username
  })

  // 初始化 - 从localStorage恢复状态
  const initAuth = () => {
    const savedToken = localStorage.getItem('token')
    const savedUser = localStorage.getItem('user')
    
    if (savedToken && savedUser) {
      token.value = savedToken
      try {
        user.value = JSON.parse(savedUser)
      } catch (e) {
        // 如果解析失败，清除无效数据
        clearAuth()
      }
    }
  }

  // 清除认证状态
  const clearAuth = () => {
    user.value = null
    token.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  // 保存认证状态
  const saveAuth = (userInfo: User, userToken: string) => {
    user.value = userInfo
    token.value = userToken
    localStorage.setItem('token', userToken)
    localStorage.setItem('user', JSON.stringify(userInfo))
  }

  // 用户登录
  const userLogin = async (loginData: LoginRequest) => {
    loading.value = true
    try {
      const response = await login(loginData)
      if (response.success && response.user && response.token) {
        saveAuth(response.user, response.token)
        return { success: true, message: response.message }
      } else {
        return { success: false, message: response.message || '登录失败' }
      }
    } catch (error: any) {
      const message = error.response?.data?.message || '登录失败，请检查网络连接'
      return { success: false, message }
    } finally {
      loading.value = false
    }
  }

  // 用户退出
  const userLogout = () => {
    logout()
    clearAuth()
  }

  // 验证token有效性
  const checkAuth = async () => {
    if (!token.value) return false
    
    try {
      const response = await validateToken()
      if (response.success && response.user) {
        user.value = response.user
        localStorage.setItem('user', JSON.stringify(response.user))
        return true
      } else {
        clearAuth()
        return false
      }
    } catch (error) {
      clearAuth()
      return false
    }
  }

  // 刷新用户信息
  const refreshUser = async () => {
    if (!token.value) return false
    
    try {
      const response = await getCurrentUser()
      if (response.success && response.user) {
        user.value = response.user
        localStorage.setItem('user', JSON.stringify(response.user))
        return true
      }
      return false
    } catch (error) {
      return false
    }
  }

  return {
    // 状态
    user,
    token,
    loading,
    
    // 计算属性
    isLoggedIn,
    isAdmin,
    userDisplayName,
    
    // 方法
    initAuth,
    clearAuth,
    userLogin,
    userLogout,
    checkAuth,
    refreshUser
  }
}) 