<template>
  <div class="payment-form">
    <h2>购买商品</h2>
    
    <div class="form-group">
      <label for="product">选择商品:</label>
      <select id="product" v-model="formData.product" @change="onProductChange">
        <option value="">请选择商品</option>
        <option v-for="product in products" :key="product.id" :value="product.name">
          {{ product.name }} - ¥{{ product.price }} (库存: {{ product.cardCount }})
        </option>
      </select>
    </div>
    
    <div v-if="selectedProduct" class="product-info">
      <h3>商品信息</h3>
      <p><strong>名称:</strong> {{ selectedProduct.name }}</p>
      <p><strong>价格:</strong> ¥{{ selectedProduct.price }}</p>
      <p><strong>描述:</strong> {{ selectedProduct.description }}</p>
      <p><strong>库存:</strong> {{ selectedProduct.cardCount }} 个</p>
    </div>
    
    <!-- 查询密钥输入 -->
    <div v-if="selectedProduct" class="query-key-section">
      <h3>设置查询密钥</h3>
      <div class="form-group">
        <label for="queryKey">查询密钥 (必填):</label>
        <input 
          id="queryKey" 
          v-model="formData.queryKey" 
          type="text" 
          placeholder="设置一个查询密钥，用于后续查询订单"
          maxlength="20"
          required
        >
        <small class="hint">查询密钥用于查询订单状态，请设置一个易记但安全的密钥（6-20位字符）</small>
      </div>
      
      <div class="info-notice">
        <p><strong>重要提示：</strong></p>
        <ul>
          <li>查询密钥是您获取卡密的唯一凭证，请务必妥善保管</li>
          <li>建议使用数字、字母组合，避免过于简单的密钥</li>
          <li>购买完成后，请记住您的查询密钥，用于后续查询订单</li>
          <li><strong>⚠️ 未付款订单将在24小时后自动删除，请及时完成支付</strong></li>
        </ul>
      </div>
    </div>
    
    <!-- 添加订单清理提醒 -->
    <div v-if="selectedProduct" class="cleanup-warning">
      <div class="warning-box">
        <h4>⚠️ 重要提醒</h4>
        <p>为保持系统整洁，<strong>未付款的订单将在创建后24小时自动删除</strong></p>
        <p>请在下单后及时完成支付，以免订单被自动清理</p>
      </div>
    </div>
    
    <button 
      @click="submitOrder" 
      :disabled="isSubmitting || !selectedProduct || selectedProduct.cardCount === 0 || !isQueryKeyValid"
      class="submit-btn"
    >
      {{ isSubmitting ? '处理中...' : '创建订单 (USDT支付)' }}
    </button>

    <div v-if="orderResult" class="result-panel">
      <h3>订单创建成功</h3>
      <p><strong>订单ID:</strong> {{ orderResult.orderId }}</p>
      <p><strong>查询密钥:</strong> {{ orderResult.queryKey }}</p>
      <p><strong>商品:</strong> {{ orderResult.product }}</p>
      <p><strong>金额:</strong> ¥{{ orderResult.amount }}</p>
      
      <div class="payment-info">
        <h4>扫码支付 (USDT)</h4>
        <div v-if="paymentQrCode" class="qr-code-container">
          <img :src="paymentQrCode" alt="支付二维码" class="qr-code" />
          <p class="qr-tip">请使用USDT钱包扫描上方二维码完成支付</p>
        </div>
      </div>
      
      <div class="query-reminder">
        <p><strong>请保存您的查询密钥：{{ orderResult.queryKey }}</strong></p>
        <p>支付完成后，您可以使用此密钥在"查询订单"页面查看卡密</p>
        <div class="query-guide">
          <h4>如何查询订单：</h4>
          <ol>
            <li>完成USDT支付后等待1-2分钟</li>
            <li>点击页面上方的"查询订单"菜单</li>
            <li>输入您的查询密钥：<strong>{{ orderResult.queryKey }}</strong></li>
            <li>查看您的卡密信息</li>
          </ol>
        </div>
      </div>
    </div>

    <div v-if="errorMessage" class="error-panel">
      <p>{{ errorMessage }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { createOrder } from '../api/payment'
import { getProducts } from '../api/product'

const products = ref([])
const selectedProduct = ref(null)
const formData = reactive({
  product: '',
  queryKey: ''
})

const isSubmitting = ref(false)
const orderResult = ref(null)
const errorMessage = ref('')
const paymentQrCode = ref('')

// 验证查询密钥是否有效
const isQueryKeyValid = computed(() => {
  return formData.queryKey.trim().length >= 6
})

// 加载商品列表
const loadProducts = async () => {
  try {
    const response = await getProducts()
    if (response.success) {
      products.value = response.data.filter(p => p.isActive)
    }
  } catch (error) {
    console.error('加载商品失败:', error)
    errorMessage.value = '加载商品失败'
  }
}

// 商品选择变化
const onProductChange = () => {
  selectedProduct.value = products.value.find(p => p.name === formData.product) || null
  orderResult.value = null
  errorMessage.value = ''
  paymentQrCode.value = ''  // 清空二维码
}

// 提交订单
const submitOrder = async () => {
  if (!selectedProduct.value) {
    errorMessage.value = '请选择商品'
    return
  }

  if (selectedProduct.value.cardCount === 0) {
    errorMessage.value = '商品暂时缺货'
    return
  }

  if (!isQueryKeyValid.value) {
    errorMessage.value = '请设置查询密钥（至少6位字符）'
    return
  }

  try {
    isSubmitting.value = true
    errorMessage.value = ''
    
    const orderData = {
      product: formData.product,
      type: 'usdt',  // 固定使用USDT支付
      queryKey: formData.queryKey
    }
    
    const result = await createOrder(orderData)
    
    if (result.success) {
      orderResult.value = result
      
      // 解析支付响应获取二维码URL
      try {
        const paymentData = JSON.parse(result.paymentResponse)
        paymentQrCode.value = paymentData.qrcodeurl || ''
        // 后端已经返回正确的oid作为orderId，无需额外解析
      } catch (error) {
        console.error('解析支付响应失败:', error)
        // 如果解析失败，二维码可能无法显示，但订单ID仍然有效
        paymentQrCode.value = ''
      }
    } else {
      errorMessage.value = result.error || '创建订单失败'
    }
  } catch (error) {
    console.error('提交订单失败:', error)
    errorMessage.value = '提交订单失败，请重试'
  } finally {
    isSubmitting.value = false
  }
}

onMounted(() => {
  loadProducts()
})
</script>

<style scoped>
.payment-form {
  max-width: 600px;
  margin: 0 auto;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  background-color: #fff;
}

.form-group {
  margin-bottom: 15px;
}

label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
}

input, select {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 16px;
}

.product-info {
  margin: 20px 0;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 4px;
  border-left: 4px solid #42b883;
}

.product-info h3 {
  margin-top: 0;
  color: #42b883;
}

.query-key-section {
  margin: 20px 0;
  padding: 15px;
  background-color: #f0f8ff;
  border-radius: 4px;
  border-left: 4px solid #2196F3;
}

.query-key-section h3 {
  margin-top: 0;
  color: #2196F3;
}

.hint {
  display: block;
  margin-top: 5px;
  font-size: 12px;
  color: #666;
  font-style: italic;
}

.info-notice {
  margin-top: 15px;
  padding: 10px;
  background-color: #fff3cd;
  border: 1px solid #ffeaa7;
  border-radius: 4px;
  font-size: 14px;
}

.info-notice p {
  margin: 0 0 8px 0;
  color: #856404;
  font-weight: bold;
}

.info-notice ul {
  margin: 0;
  padding-left: 20px;
  color: #856404;
}

.info-notice li {
  margin-bottom: 5px;
}

.submit-btn, .check-btn {
  background-color: #4CAF50;
  color: white;
  border: none;
  padding: 10px 15px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  margin-top: 10px;
  width: 100%;
}

.submit-btn:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}

.check-btn {
  background-color: #2196F3;
  margin-top: 10px;
}

.result-panel, .status-panel, .error-panel {
  margin-top: 20px;
  padding: 15px;
  border-radius: 4px;
}

.result-panel {
  background-color: #e8f5e8;
  border: 1px solid #4CAF50;
}

.status-panel {
  background-color: #f0f8ff;
  border: 1px solid #2196F3;
}

.error-panel {
  background-color: #ffe6e6;
  border: 1px solid #ff4444;
  color: #cc0000;
}

.payment-info {
  margin-top: 15px;
}

.payment-info h4 {
  margin-bottom: 10px;
  color: #333;
}

.query-reminder {
  margin: 15px 0;
  padding: 10px;
  background-color: #fff3cd;
  border: 1px solid #ffeaa7;
  border-radius: 4px;
  font-size: 14px;
  color: #856404;
}

.query-reminder p {
  margin: 5px 0;
}

.success {
  color: #4CAF50;
  font-weight: bold;
}

.pending {
  color: #ff9800;
  font-weight: bold;
}

.card-code {
  margin-top: 15px;
  padding: 15px;
  background-color: #fff;
  border: 2px solid #4CAF50;
  border-radius: 8px;
}

.card-code h4 {
  margin-top: 0;
  color: #4CAF50;
}

.code-display {
  font-family: 'Courier New', monospace;
  font-size: 18px;
  font-weight: bold;
  background-color: #f0f8f0;
  padding: 10px;
  border-radius: 4px;
  text-align: center;
  letter-spacing: 2px;
  color: #2e7d32;
  border: 1px solid #4CAF50;
}

.qr-code-container {
  margin-bottom: 15px;
  text-align: center;
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 8px;
  border: 2px dashed #4CAF50;
}

.qr-code {
  max-width: 200px;
  max-height: 200px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.qr-tip {
  margin-top: 15px;
  text-align: center;
  color: #4CAF50;
  font-weight: bold;
  font-size: 14px;
}

.query-guide {
  margin-top: 15px;
  padding: 10px;
  background-color: #fff;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.query-guide h4 {
  margin-top: 0;
  margin-bottom: 10px;
  color: #333;
}

.query-guide ol {
  margin: 0;
  padding-left: 20px;
}

.query-guide li {
  margin-bottom: 5px;
}

.cleanup-warning {
  margin-top: 20px;
  margin-bottom: 20px;
}

.warning-box {
  padding: 15px;
  background-color: #fff3cd;
  border: 1px solid #ffeaa7;
  border-radius: 8px;
  border-left: 4px solid #f39c12;
}

.warning-box h4 {
  margin: 0 0 10px 0;
  color: #856404;
  font-size: 16px;
  font-weight: bold;
}

.warning-box p {
  margin: 5px 0;
  color: #856404;
  line-height: 1.5;
}

.warning-box strong {
  color: #d63031;
}
</style> 