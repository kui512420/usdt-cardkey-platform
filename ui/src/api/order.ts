import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

// 创建带有认证的axios实例
const orderApi = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json'
  }
});

// 请求拦截器 - 添加token
orderApi.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器 - 处理token过期
orderApi.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Token过期，清除本地存储并跳转到登录页
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

/**
 * 订单管理API服务
 */

/**
 * 获取所有订单列表（分页）
 */
export const getOrdersList = async (page: number = 1, size: number = 20, status?: string) => {
  const params: any = { page, size };
  if (status) {
    params.status = status;
  }
  const response = await orderApi.get('/orders/list', { params });
  return response.data;
};

/**
 * 根据状态获取订单列表
 */
export const getOrdersByStatus = async (status: string) => {
  const response = await orderApi.get(`/orders/status/${status}`);
  return response.data;
};

/**
 * 获取所有未发货的订单
 */
export const getUndeliveredOrders = async () => {
  const response = await orderApi.get('/orders/undelivered');
  return response.data;
};

/**
 * 手动清理未付款订单
 */
export const cleanupUnpaidOrders = async (hours: number = 24) => {
  const response = await orderApi.post('/orders/cleanup-unpaid', { hours });
  return response.data;
};

/**
 * 批量处理未发货订单
 */
export const processUndeliveredOrders = async () => {
  const response = await orderApi.post('/orders/process-undelivered');
  return response.data;
}; 