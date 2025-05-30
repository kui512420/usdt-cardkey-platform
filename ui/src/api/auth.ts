import axios from 'axios'

const BASE_URL = 'http://localhost:8080/api'

// 创建axios实例
const authApi = axios.create({
  baseURL: BASE_URL,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器 - 添加token
authApi.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器 - 处理token过期
authApi.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Token过期，清除本地存储并跳转到登录页
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export interface LoginRequest {
  username: string
  password: string
}

export interface User {
  id: number
  username: string
  email?: string
  nickname?: string
  phoneNumber?: string
  role: string
  lastLoginTime?: string
  createdAt: string
}

export interface AuthResponse {
  success: boolean
  message: string
  token?: string
  user?: User
}

// 用户登录
export const login = async (data: LoginRequest): Promise<AuthResponse> => {
  const response = await authApi.post('/auth/login', data)
  return response.data
}

// 验证token
export const validateToken = async (): Promise<AuthResponse> => {
  const response = await authApi.post('/auth/validate')
  return response.data
}

// 获取当前用户信息
export const getCurrentUser = async (): Promise<AuthResponse> => {
  const response = await authApi.get('/auth/me')
  return response.data
}

// 退出登录
export const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
}

export default authApi 