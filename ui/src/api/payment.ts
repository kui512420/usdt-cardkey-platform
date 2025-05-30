import axios from 'axios';

/**
 * 支付API服务
 */
interface CreateOrderParams {
  name?: string;       // 钱包地址，非必需
  type?: string;       // 支付类型，默认为"usdt"
  value?: string;      // 金额，默认为"1"
  product?: string;    // 产品信息，默认为"test"
  queryKey?: string;   // 查询密钥
}

interface QueryKeyParams {
  queryKey: string;    // 查询密钥
}

const API_BASE_URL = 'http://localhost:8080/api';

/**
 * 创建支付订单
 */
export const createOrder = async (orderData: any) => {
  const response = await axios.post(`${API_BASE_URL}/payment/create`, orderData);
  return response.data;
};

/**
 * 根据查询密钥查询订单
 */
export const queryOrderByKey = async (keyData: QueryKeyParams) => {
  const response = await axios.post(`${API_BASE_URL}/payment/query-by-key`, keyData);
  return response.data;
};

/**
 * 根据手机号获取所有订单
 */
export const getOrdersByPhone = async (phone: string) => {
  const response = await axios.get(`${API_BASE_URL}/payment/orders-by-phone/${phone}`);
  return response.data;
};

export const getOrderDetails = async (orderId: string) => {
  const response = await axios.get(`${API_BASE_URL}/payment/order/${orderId}`);
  return response.data;
}; 