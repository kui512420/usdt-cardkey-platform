import authApi from './auth'

export interface SystemStatistics {
  totalProducts: number
  totalCardCodes: number
  totalOrders: number
  totalUsers: number
}

export interface DetailedStatistics extends SystemStatistics {
  activeProducts: number
  availableCardCodes: number
  usedCardCodes: number
  deliveredOrders: number
  adminUsers: number
}

export interface StatisticsResponse {
  success: boolean
  data?: SystemStatistics | DetailedStatistics
  error?: string
}

/**
 * 获取系统基础统计数据
 */
export const getSystemStatistics = async (): Promise<StatisticsResponse> => {
  const response = await authApi.get('/statistics/overview')
  return response.data
}

/**
 * 获取详细统计数据
 */
export const getDetailedStatistics = async (): Promise<StatisticsResponse> => {
  const response = await authApi.get('/statistics/detailed')
  return response.data
} 