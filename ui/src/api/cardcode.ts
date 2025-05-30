import authApi from './auth'

const API_BASE_URL = 'http://localhost:8080/api'

export const generateCardCodes = async (data: any) => {
  const response = await authApi.post(`/cardcodes/generate`, data)
  return response.data
}

export const importCardCodes = async (data: any) => {
  const response = await authApi.post(`/cardcodes/import`, data)
  return response.data
}

export const getCardCodesByProduct = async (productId: string) => {
  const response = await authApi.get(`/cardcodes/product/${productId}`)
  return response.data
}

export const deleteCardCode = async (id: number) => {
  const response = await authApi.delete(`/cardcodes/${id}`)
  return response.data
} 