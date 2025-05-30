<template>
  <div class="order-query-container">
    <el-card class="order-query-card">
      <template #header>
        <div class="card-header">
          <el-icon><Search /></el-icon>
          <span>查询订单状态</span>
        </div>
      </template>
      
      <!-- 查询密钥查询 -->
      <el-form :model="queryKeyForm" label-position="top">
        <el-form-item label="查询密钥">
          <el-input 
            v-model="queryKeyForm.queryKey" 
            placeholder="请输入购买时设置的查询密钥" 
            clearable
            :prefix-icon="Key"
          />
        </el-form-item>
        
        <div class="query-hint">
          <el-alert
            title="查询提示"
            type="info"
            :closable="false"
            show-icon
          >
            <p>请输入购买时设置的查询密钥，这是您获取卡密的重要凭证。</p>
            <p>查询密钥是您在下单时自定义的密码，请仔细回忆。</p>
            <p><strong>⚠️ 重要提醒：未付款的订单将在创建后24小时自动删除</strong></p>
          </el-alert>
        </div>
        
        <div class="action-buttons">
          <el-button 
            type="primary" 
            @click="queryByKey" 
            :loading="isQuerying"
            :disabled="!queryKeyForm.queryKey.trim()"
            :icon="Search"
          >
            {{ isQuerying ? '查询中...' : '查询订单' }}
          </el-button>
        </div>
      </el-form>
      
      <el-collapse-transition>
        <div v-if="showStatus" class="status-result">
          <el-alert
            :title="statusMessage"
            :type="hasCardCode ? 'success' : errorMessage ? 'error' : 'info'"
            :closable="false"
            show-icon
          />
          
          <!-- 显示卡密信息 -->
          <div v-if="orderData && orderData.deliveredCards && orderData.deliveredCards.length > 0" class="card-code-result">
            <el-card shadow="never" class="card-code-card">
              <template #header>
                <div class="card-header">
                  <el-icon><Key /></el-icon>
                  <span>您的卡密 (共{{ orderData.deliveredCards.length }}个)</span>
                </div>
              </template>
              <div class="multiple-cards">
                <div v-for="(cardInfo, index) in orderData.deliveredCards" :key="cardInfo.orderId" class="card-item">
                  <el-card shadow="hover" class="single-card">
                    <div class="card-header-info">
                      <h4>{{ cardInfo.productName }} - ¥{{ cardInfo.amount }}</h4>
                      <p class="order-id">订单号: {{ cardInfo.orderId }}</p>
                    </div>
                    <div class="card-code-display">
                      <div class="code-text">{{ cardInfo.cardCode }}</div>
                      <div class="card-code-actions">
                        <el-button 
                          type="primary" 
                          size="small" 
                          @click="copyToClipboard(cardInfo.cardCode)"
                          :icon="DocumentCopy"
                        >
                          复制卡密
                        </el-button>
                      </div>
                      <p class="delivery-time">发货时间: {{ formatDateTime(cardInfo.deliveredAt) }}</p>
                    </div>
                  </el-card>
                </div>
              </div>
            </el-card>
          </div>
          
          <!-- 显示单个卡密信息（向后兼容） -->
          <div v-else-if="orderData && orderData.cardCode" class="card-code-result">
            <el-card shadow="never" class="card-code-card">
              <template #header>
                <div class="card-header">
                  <el-icon><Key /></el-icon>
                  <span>您的卡密</span>
                </div>
              </template>
              <div class="card-code-display">
                <div class="code-text">{{ orderData.cardCode }}</div>
                <div class="card-code-actions">
                  <el-button 
                    type="primary" 
                    size="small" 
                    @click="copyToClipboard(orderData.cardCode)"
                    :icon="DocumentCopy"
                  >
                    复制卡密
                  </el-button>
                </div>
                <p class="delivery-time">发货时间: {{ orderData.deliveredAt }}</p>
              </div>
            </el-card>
          </div>
          
          <!-- 显示等待发货信息 -->
          <div v-else-if="orderData && orderData.order && orderData.order.status === 'PAID' && !orderData.order.isDelivered" class="pending-delivery">
            <el-card shadow="never" class="pending-card">
              <template #header>
                <div class="card-header">
                  <el-icon><Loading /></el-icon>
                  <span>正在处理发货</span>
                </div>
              </template>
              <div class="pending-content">
                <p>您的订单已支付成功，系统正在为您分配卡密，请稍等...</p>
                <p>通常在支付完成后1-2分钟内自动发货。</p>
                <el-button type="primary" @click="queryByKey" :loading="isQuerying">
                  刷新状态
                </el-button>
              </div>
            </el-card>
          </div>
          
          <el-collapse v-if="orderDetails" v-model="activeCollapse" class="mt-3">
            <el-collapse-item name="details">
              <template #title>
                <div class="collapse-title">
                  <el-icon><Document /></el-icon>
                  订单详细信息 
                  <span v-if="orderData.orders && orderData.orders.length > 1" class="order-count">
                    (共{{ orderData.orders.length }}个订单)
                  </span>
                </div>
              </template>
              
              <!-- 显示所有订单 -->
              <div v-if="orderData.orders && orderData.orders.length > 0" class="all-orders">
                <div v-for="(order, index) in orderData.orders" :key="order.orderId" class="order-item">
                  <el-card shadow="never" class="details-card">
                    <template #header>
                      <div class="order-header">
                        <span class="order-title">订单 {{ index + 1 }}</span>
                        <el-tag :type="getStatusType(order.status)" size="small">
                          {{ getStatusText(order.status) }}
                        </el-tag>
                      </div>
                    </template>
                    <div class="order-details">
                      <div class="detail-row">
                        <span class="detail-label">订单编号：</span>
                        <span class="detail-value">{{ order.orderId }}</span>
                      </div>
                      <div class="detail-row">
                        <span class="detail-label">商品信息：</span>
                        <span class="detail-value">{{ order.product?.name || '未知商品' }}</span>
                      </div>
                      <div class="detail-row">
                        <span class="detail-label">订单金额：</span>
                        <span class="detail-value amount">¥{{ order.amount }}</span>
                      </div>
                      <div class="detail-row">
                        <span class="detail-label">支付方式：</span>
                        <span class="detail-value">{{ order.paymentType || 'USDT' }}</span>
                      </div>
                      <div class="detail-row">
                        <span class="detail-label">发货状态：</span>
                        <el-tag :type="order.isDelivered ? 'success' : 'info'" class="detail-value">
                          {{ order.isDelivered ? '已发货' : '未发货' }}
                        </el-tag>
                      </div>
                      <div class="detail-row">
                        <span class="detail-label">下单时间：</span>
                        <span class="detail-value">{{ formatDateTime(order.createdAt) }}</span>
                      </div>
                      <div v-if="order.deliveredAt" class="detail-row">
                        <span class="detail-label">发货时间：</span>
                        <span class="detail-value">{{ formatDateTime(order.deliveredAt) }}</span>
                      </div>
                      <div v-if="order.isDelivered && order.deliveredCardCode" class="detail-row">
                        <span class="detail-label">卡密：</span>
                        <span class="detail-value card-code">{{ order.deliveredCardCode }}</span>
                      </div>
                    </div>
                  </el-card>
                </div>
              </div>
              
              <!-- 单个订单显示（向后兼容） -->
              <el-card v-else-if="orderData.order" shadow="never" class="details-card">
                <div class="order-details">
                  <div class="detail-row">
                    <span class="detail-label">订单编号：</span>
                    <span class="detail-value">{{ orderData.order.orderId }}</span>
                  </div>
                  <div class="detail-row">
                    <span class="detail-label">订单状态：</span>
                    <el-tag :type="getStatusType(orderData.order.status)" class="detail-value">
                      {{ getStatusText(orderData.order.status) }}
                    </el-tag>
                  </div>
                  <div class="detail-row">
                    <span class="detail-label">商品信息：</span>
                    <span class="detail-value">{{ orderData.order.product?.name || '未知商品' }}</span>
                  </div>
                  <div class="detail-row">
                    <span class="detail-label">订单金额：</span>
                    <span class="detail-value amount">¥{{ orderData.order.amount }}</span>
                  </div>
                  <div class="detail-row">
                    <span class="detail-label">支付方式：</span>
                    <span class="detail-value">{{ orderData.order.paymentType || 'USDT' }}</span>
                  </div>
                  <div class="detail-row">
                    <span class="detail-label">发货状态：</span>
                    <el-tag :type="orderData.order.isDelivered ? 'success' : 'info'" class="detail-value">
                      {{ orderData.order.isDelivered ? '已发货' : '未发货' }}
                    </el-tag>
                  </div>
                  <div class="detail-row">
                    <span class="detail-label">下单时间：</span>
                    <span class="detail-value">{{ formatDateTime(orderData.order.createdAt) }}</span>
                  </div>
                  <div v-if="orderData.order.deliveredAt" class="detail-row">
                    <span class="detail-label">发货时间：</span>
                    <span class="detail-value">{{ formatDateTime(orderData.order.deliveredAt) }}</span>
                  </div>
                </div>
              </el-card>
            </el-collapse-item>
          </el-collapse>
        </div>
      </el-collapse-transition>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { 
  Search, 
  Document,
  Key,
  DocumentCopy,
  Loading
} from '@element-plus/icons-vue';
import { queryOrderByKey } from '../api/payment';
import { ElMessage } from 'element-plus';

const isQuerying = ref(false);
const orderData = ref<any>(null);
const showStatus = ref(false);
const errorMessage = ref('');
const activeCollapse = ref(['']);
const queryKeyForm = ref({
  queryKey: ''
});

// 计算属性，判断是否有卡密
const hasCardCode = computed(() => {
  return (orderData.value && orderData.value.deliveredCards && orderData.value.deliveredCards.length > 0) ||
         (orderData.value && orderData.value.cardCode);
});

// 计算属性，获取状态消息
const statusMessage = computed(() => {
  if (errorMessage.value) {
    return errorMessage.value;
  }
  
  if (!orderData.value) {
    return '';
  }
  
  if (hasCardCode.value) {
    if (orderData.value.deliveredCards && orderData.value.deliveredCards.length > 1) {
      return `查询成功！找到${orderData.value.deliveredCards.length}个已发货的订单`;
    } else {
      return '查询成功！您的订单已发货';
    }
  } else if (orderData.value.hasDeliveredOrder) {
    return '查询成功！部分订单已发货';
  } else if (orderData.value.order && orderData.value.order.status === 'PAID') {
    return '您的订单已支付，正在处理发货';
  } else if (orderData.value.orders && orderData.value.orders.length > 1) {
    const paidCount = orderData.value.orders.filter((order: any) => order.status === 'PAID').length;
    if (paidCount > 0) {
      return `查询成功！找到${orderData.value.orders.length}个订单，其中${paidCount}个已支付`;
    } else {
      return `查询成功！找到${orderData.value.orders.length}个订单`;
    }
  } else {
    return '订单查询成功';
  }
});

// 计算属性，格式化订单详情
const orderDetails = computed(() => {
  if (!orderData.value) {
    return '';
  }
  
  return JSON.stringify(orderData.value, null, 2);
});

// 根据查询密钥查询订单
const queryByKey = async () => {
  const queryKey = queryKeyForm.value.queryKey.trim();
  if (!queryKey) {
    ElMessage.warning('请输入查询密钥');
    return;
  }
  
  try {
    isQuerying.value = true;
    errorMessage.value = '';
    
    const result = await queryOrderByKey({ queryKey });
    
    if (result.success && (result.order || result.orders)) {
      orderData.value = result;
      showStatus.value = true;
      
      // 处理多个已发货的卡密
      if (result.deliveredCards && result.deliveredCards.length > 0) {
        ElMessage.success(`查询成功！找到${result.deliveredCards.length}个已发货的订单`);
      }
      // 处理单个卡密（向后兼容）
      else if (result.cardCode) {
        orderData.value.cardCode = result.cardCode;
        orderData.value.deliveredAt = result.deliveredAt;
        ElMessage.success('查询成功！订单已发货');
      }
      // 处理多个订单但没有发货的情况
      else if (result.orders && result.orders.length > 1) {
        const paidCount = result.orders.filter((order: any) => order.status === 'PAID').length;
        if (paidCount > 0) {
          ElMessage.info(`查询成功！找到${result.orders.length}个订单，其中${paidCount}个已支付`);
        } else {
          ElMessage.info(`查询成功！找到${result.orders.length}个订单`);
        }
      }
      // 处理单个订单
      else if (result.order && result.order.status === 'PAID') {
        ElMessage.info('订单已支付，正在处理发货');
      } else {
        ElMessage.info('订单查询成功');
      }
    } else {
      errorMessage.value = result.error || '未找到匹配的订单';
      showStatus.value = true;
      ElMessage.warning(errorMessage.value);
    }
  } catch (error) {
    console.error('查询订单失败:', error);
    errorMessage.value = '查询订单失败，请重试';
    showStatus.value = true;
    ElMessage.error('查询订单失败，请重试');
  } finally {
    isQuerying.value = false;
  }
};

// 辅助函数，获取状态类型
const getStatusType = (status: string) => {
  switch (status) {
    case 'PAID':
      return 'success';
    case 'UNPAID':
      return 'info';
    case 'CANCELED':
      return 'danger';
    default:
      return 'info';
  }
};

// 辅助函数，获取状态文本
const getStatusText = (status: string) => {
  switch (status) {
    case 'PAID':
      return '已支付';
    case 'UNPAID':
      return '未支付';
    case 'CANCELED':
      return '已取消';
    default:
      return status;
  }
};

// 辅助函数，格式化日期时间
const formatDateTime = (timestamp: string) => {
  const date = new Date(timestamp);
  return date.toLocaleString();
};

// 辅助函数，复制到剪贴板
const copyToClipboard = (text: string) => {
  const input = document.createElement('input');
  input.value = text;
  document.body.appendChild(input);
  input.select();
  document.execCommand('copy');
  document.body.removeChild(input);
  ElMessage.success('卡密已复制到剪贴板');
};
</script>

<style scoped>
.order-query-container {
  max-width: 600px;
  margin: 0 auto;
  padding: 20px;
}

.order-query-card {
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}

.card-header .el-icon {
  margin-right: 8px;
  font-size: 20px;
}

.action-buttons {
  display: flex;
  gap: 10px;
  margin-top: 15px;
  flex-wrap: wrap;
}

.status-result {
  margin-top: 20px;
}

.auto-hint {
  display: flex;
  align-items: center;
  margin-top: 8px;
  font-size: 14px;
  color: #606266;
}

.auto-hint .el-icon {
  margin-right: 5px;
}

.collapse-title {
  display: flex;
  align-items: center;
}

.collapse-title .el-icon {
  margin-right: 5px;
}

.details-card {
  background-color: #f8f9fa;
  border: none;
}

.mt-3 {
  margin-top: 15px;
}

pre {
  white-space: pre-wrap;
  word-break: break-all;
  font-family: Consolas, Monaco, monospace;
  font-size: 13px;
  line-height: 1.5;
  padding: 5px;
  color: #333;
  overflow-x: auto;
}

.card-code-result {
  margin-top: 20px;
}

.card-code-card {
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.card-code-display {
  padding: 15px;
}

.code-text {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 10px;
}

.delivery-time {
  font-size: 14px;
  color: #606266;
}

.query-hint {
  margin-top: 10px;
  margin-bottom: 15px;
}

.order-details {
  display: flex;
  flex-wrap: wrap;
}

.detail-row {
  width: 50%;
  margin-bottom: 10px;
  display: flex;
  flex-wrap: wrap;
}

@media (max-width: 768px) {
  .detail-row {
    width: 100%;
  }
}

.detail-label {
  font-weight: bold;
  color: #606266;
  min-width: 90px;
}

.detail-value {
  margin-left: 10px;
  color: #303133;
}

.amount {
  font-weight: bold;
  color: #E6A23C;
  font-size: 16px;
}

.card-code-display {
  text-align: center;
  padding: 20px;
}

.code-text {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
  background: #f0f9ff;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 10px;
  word-break: break-all;
  user-select: all;
}

.delivery-time {
  color: #67C23A;
  font-size: 14px;
  margin-bottom: 0;
}

.card-code-card {
  margin-top: 15px;
  border: 2px solid #67C23A;
}

.card-code-result .card-header {
  color: #67C23A;
  font-weight: bold;
}

.card-code-actions {
  margin-bottom: 10px;
}

.pending-delivery {
  margin-top: 20px;
}

.pending-card {
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.pending-content {
  padding: 15px;
}

/* 多卡密显示样式 */
.multiple-cards {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.card-item {
  width: 100%;
}

.single-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
}

.card-header-info {
  margin-bottom: 10px;
}

.card-header-info h4 {
  margin: 0 0 5px 0;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
}

.order-id {
  margin: 0;
  color: #909399;
  font-size: 12px;
}

/* 多订单显示样式 */
.all-orders {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.order-item {
  width: 100%;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-title {
  font-weight: 600;
  color: #303133;
}

.order-count {
  color: #909399;
  font-size: 14px;
  margin-left: 5px;
}

.card-code {
  font-family: 'Courier New', monospace;
  background-color: #f5f5f5;
  padding: 4px 8px;
  border-radius: 4px;
  font-weight: bold;
}
</style> 