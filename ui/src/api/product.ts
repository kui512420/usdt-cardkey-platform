import axios from 'axios'
import authApi from './auth'

const API_BASE_URL = 'http://localhost:8080/api'

// 公开的获取商品列表（不需要认证）
export const getProducts = async () => {
  const response = await axios.get(`${API_BASE_URL}/products`)
  return response.data
}

// 公开的获取单个商品（不需要认证）
export const getProduct = async (id: number) => {
  const response = await axios.get(`${API_BASE_URL}/products/${id}`)
  return response.data
}

// 需要管理员权限的操作
export const createProduct = async (product: any) => {
  const response = await authApi.post(`/products`, product)
  return response.data
}

export const updateProduct = async (id: number, product: any) => {
  const response = await authApi.put(`/products/${id}`, product)
  return response.data
}

export const deleteProduct = async (id: number) => {
  const response = await authApi.delete(`/products/${id}`)
  return response.data
} 