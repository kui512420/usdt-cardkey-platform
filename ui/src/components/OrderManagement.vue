<template>
  <div class="order-management">
    <!-- 功能按钮区域 -->
    <el-row :gutter="20" class="action-buttons">
      <el-col :span="4">
        <el-button type="primary" @click="loadAllOrders" :loading="loading">
          <el-icon><Document /></el-icon>
          查看所有订单
        </el-button>
      </el-col>
      <el-col :span="4">
        <el-button type="warning" @click="loadUnpaidOrders" :loading="loading">
          <el-icon><Document /></el-icon>
          查看未付款订单
        </el-button>
      </el-col>
      <el-col :span="4">
        <el-button type="success" @click="loadPaidOrders" :loading="loading">
          <el-icon><SuccessFilled /></el-icon>
          查看已付款订单
        </el-button>
      </el-col>
      <el-col :span="4">
        <el-button type="info" @click="loadUndeliveredOrders" :loading="loading">
          <el-icon><Van /></el-icon>
          查看未发货订单
        </el-button>
      </el-col>
      <el-col :span="4">
        <el-button type="primary" @click="showProcessModal = true" :loading="loading">
          <el-icon><Setting /></el-icon>
          批量处理
        </el-button>
      </el-col>
      <el-col :span="4">
        <el-button type="danger" @click="showCleanupDialog" :loading="loading">
          <el-icon><Delete /></el-icon>
          清理未付款订单
        </el-button>
      </el-col>
    </el-row>

    <!-- 订单清理提醒 -->
    <el-alert
      title="自动清理提醒"
      type="info"
      :closable="false"
      show-icon
      class="cleanup-reminder"
    >
      <p>系统已配置每天凌晨2点自动清理超过24小时的未付款订单</p>
      <p>您也可以手动清理过期的未付款订单，以保持系统整洁</p>
    </el-alert>

    <!-- 订单列表 -->
    <el-card v-if="orders.length > 0 || showPagination" class="orders-card">
      <template #header>
        <div class="card-header">
          <span>{{ currentTitle }} (共{{ totalOrders }}个)</span>
          <el-button type="info" size="small" @click="refreshOrders">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>
      
      <el-table :data="orders" style="width: 100%" v-loading="loading">
        <el-table-column prop="orderId" label="订单ID" width="150">
          <template #default="scope">
            <el-tooltip :content="scope.row.orderId" placement="top">
              <span class="order-id">{{ scope.row.orderId.substring(0, 12) }}...</span>
            </el-tooltip>
          </template>
        </el-table-column>
        
        <el-table-column label="商品信息" width="180">
          <template #default="scope">
            <div>
              <div class="product-name">{{ scope.row.product?.name || '未知商品' }}</div>
              <div class="product-amount">¥{{ scope.row.amount }}</div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="paymentType" label="支付方式" width="100">
          <template #default="scope">
            <el-tag size="small">{{ scope.row.paymentType?.toUpperCase() || 'USDT' }}</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="订单状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)" size="small">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="发货状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.isDelivered ? 'success' : 'info'" size="small">
              {{ scope.row.isDelivered ? '已发货' : '未发货' }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="queryKey" label="查询密钥" width="120">
          <template #default="scope">
            <span class="query-key">{{ scope.row.queryKey || '无' }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="创建时间" width="160">
          <template #default="scope">
            <span>{{ formatDateTime(scope.row.createdAt) }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="发货时间" width="160">
          <template #default="scope">
            <span>{{ scope.row.deliveredAt ? formatDateTime(scope.row.deliveredAt) : '-' }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="卡密" min-width="200">
          <template #default="scope">
            <div v-if="scope.row.isDelivered && scope.row.deliveredCardCode" class="card-code-cell">
              <span class="card-code">{{ scope.row.deliveredCardCode }}</span>
              <el-button 
                type="primary" 
                size="small" 
                @click="copyToClipboard(scope.row.deliveredCardCode)"
              >
                复制
              </el-button>
            </div>
            <span v-else class="no-card-code">-</span>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页组件 -->
      <el-pagination
        v-if="showPagination"
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="totalOrders"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        class="pagination"
      />
    </el-card>

    <!-- 空状态 -->
    <el-empty v-if="!loading && orders.length === 0 && !showPagination" description="暂无订单数据" />

    <!-- 清理订单对话框 -->
    <el-dialog v-model="showCleanupModal" title="清理未付款订单" width="500px">
      <div class="cleanup-dialog-content">
        <el-alert
          title="警告"
          type="warning"
          :closable="false"
          show-icon
        >
          此操作将删除指定时间之前创建的所有未付款订单，且无法恢复！
        </el-alert>
        
        <el-form :model="cleanupForm" label-width="120px" style="margin-top: 20px;">
          <el-form-item label="清理条件">
            <el-input-number 
              v-model="cleanupForm.hours" 
              :min="1" 
              :max="168"
              placeholder="小时数"
            />
            <span style="margin-left: 10px;">小时前创建的未付款订单</span>
          </el-form-item>
        </el-form>
        
        <p class="cleanup-tip">
          例如：设置为24小时，将删除24小时前创建且仍未付款的订单
        </p>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showCleanupModal = false">取消</el-button>
          <el-button type="danger" @click="confirmCleanup" :loading="cleanupLoading">
            确认清理
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 批量处理对话框 -->
    <el-dialog v-model="showProcessModal" title="批量处理未发货订单" width="400px">
      <div class="process-dialog-content">
        <el-alert
          title="提示"
          type="info"
          :closable="false"
          show-icon
        >
          此操作将处理所有已支付但未发货的订单，为它们分配卡密并标记为已发货。
        </el-alert>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showProcessModal = false">取消</el-button>
          <el-button type="primary" @click="confirmProcessUndelivered" :loading="processLoading">
            开始处理
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Document,
  SuccessFilled,
  Van,
  Delete,
  Refresh,
  DocumentCopy,
  Setting
} from '@element-plus/icons-vue'
import { 
  getOrdersByStatus, 
  getUndeliveredOrders, 
  cleanupUnpaidOrders, 
  processUndeliveredOrders,
  getOrdersList
} from '../api/order'

// 响应式数据
const orders = ref([])
const loading = ref(false)
const currentTitle = ref('订单列表')
const showCleanupModal = ref(false)
const showProcessModal = ref(false)
const cleanupLoading = ref(false)
const processLoading = ref(false)
const showPagination = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const totalOrders = ref(0)
const currentViewType = ref('') // 记录当前查看的类型

// 清理表单
const cleanupForm = reactive({
  hours: 24
})

// 加载所有订单
const loadAllOrders = async () => {
  loading.value = true
  currentTitle.value = '所有订单'
  currentViewType.value = 'all'
  showPagination.value = true
  try {
    const response = await getOrdersList(currentPage.value, pageSize.value)
    if (response.success) {
      orders.value = response.data || []
      totalOrders.value = response.total || 0
      ElMessage.success(`加载成功，共${totalOrders.value}个订单`)
    } else {
      ElMessage.error(response.error || '加载失败')
    }
  } catch (error) {
    console.error('加载所有订单失败:', error)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

// 加载未付款订单
const loadUnpaidOrders = async () => {
  loading.value = true
  currentTitle.value = '未付款订单'
  currentViewType.value = 'unpaid'
  showPagination.value = false
  try {
    const response = await getOrdersByStatus('pending')
    if (response.success) {
      orders.value = response.data || []
      totalOrders.value = response.count || orders.value.length
      ElMessage.success(`加载成功，共${orders.value.length}个未付款订单`)
    } else {
      ElMessage.error(response.error || '加载失败')
    }
  } catch (error) {
    console.error('加载未付款订单失败:', error)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

// 加载已付款订单
const loadPaidOrders = async () => {
  loading.value = true
  currentTitle.value = '已付款订单'
  currentViewType.value = 'paid'
  showPagination.value = false
  try {
    const response = await getOrdersByStatus('paid')
    if (response.success) {
      orders.value = response.data || []
      totalOrders.value = response.count || orders.value.length
      ElMessage.success(`加载成功，共${orders.value.length}个已付款订单`)
    } else {
      ElMessage.error(response.error || '加载失败')
    }
  } catch (error) {
    console.error('加载已付款订单失败:', error)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

// 加载未发货订单
const loadUndeliveredOrders = async () => {
  loading.value = true
  currentTitle.value = '未发货订单'
  currentViewType.value = 'undelivered'
  showPagination.value = false
  try {
    const response = await getUndeliveredOrders()
    if (response.success) {
      orders.value = response.data || []
      totalOrders.value = response.count || orders.value.length
      ElMessage.success(`加载成功，共${orders.value.length}个未发货订单`)
    } else {
      ElMessage.error(response.error || '加载失败')
    }
  } catch (error) {
    console.error('加载未发货订单失败:', error)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

// 刷新当前订单列表
const refreshOrders = async () => {
  switch (currentViewType.value) {
    case 'all':
      await loadAllOrders()
      break
    case 'unpaid':
      await loadUnpaidOrders()
      break
    case 'paid':
      await loadPaidOrders()
      break
    case 'undelivered':
      await loadUndeliveredOrders()
      break
    default:
      await loadAllOrders()
  }
}

// 显示清理对话框
const showCleanupDialog = () => {
  showCleanupModal.value = true
}

// 确认清理
const confirmCleanup = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要清理${cleanupForm.hours}小时前创建的所有未付款订单吗？此操作无法撤销！`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    cleanupLoading.value = true
    
    const response = await cleanupUnpaidOrders(cleanupForm.hours)
    if (response.success) {
      ElMessage.success(response.message || `成功清理了${response.deletedCount}个订单`)
      showCleanupModal.value = false
      // 如果当前显示的是未付款订单，则刷新列表
      if (currentTitle.value.includes('未付款')) {
        await loadUnpaidOrders()
      }
    } else {
      ElMessage.error(response.error || '清理失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('清理订单失败:', error)
      ElMessage.error('清理失败')
    }
  } finally {
    cleanupLoading.value = false
  }
}

// 确认处理未发货订单
const confirmProcessUndelivered = async () => {
  try {
    processLoading.value = true
    
    const response = await processUndeliveredOrders()
    if (response.success) {
      ElMessage.success(response.message || '处理完成')
      showProcessModal.value = false
      // 刷新未发货订单列表
      if (currentTitle.value.includes('未发货')) {
        await loadUndeliveredOrders()
      }
    } else {
      ElMessage.error(response.error || '处理失败')
    }
  } catch (error) {
    console.error('处理订单失败:', error)
    ElMessage.error('处理失败')
  } finally {
    processLoading.value = false
  }
}

// 辅助函数
const getStatusType = (status: string) => {
  switch (status) {
    case 'PAID':
      return 'success'
    case 'PENDING':
      return 'warning'
    case 'CANCELLED':
      return 'danger'
    default:
      return 'info'
  }
}

const getStatusText = (status: string) => {
  switch (status) {
    case 'PAID':
      return '已支付'
    case 'PENDING':
      return '待支付'
    case 'CANCELLED':
      return '已取消'
    case 'DELIVERED':
      return '已发货'
    default:
      return status
  }
}

const formatDateTime = (timestamp: string) => {
  return new Date(timestamp).toLocaleString()
}

const copyToClipboard = (text: string) => {
  navigator.clipboard.writeText(text).then(() => {
    ElMessage.success('卡密已复制到剪贴板')
  }).catch(() => {
    // 降级方案
    const input = document.createElement('input')
    input.value = text
    document.body.appendChild(input)
    input.select()
    document.execCommand('copy')
    document.body.removeChild(input)
    ElMessage.success('卡密已复制到剪贴板')
  })
}

const handleSizeChange = (newSize: number) => {
  pageSize.value = newSize
  currentPage.value = 1
  if (currentViewType.value === 'all') {
    loadAllOrders()
  }
}

const handleCurrentChange = (newPage: number) => {
  currentPage.value = newPage
  if (currentViewType.value === 'all') {
    loadAllOrders()
  }
}

// 组件挂载时自动加载所有订单
onMounted(() => {
  loadAllOrders()
})
</script>

<style scoped>
.order-management {
  padding: 20px;
}

.action-buttons {
  margin-bottom: 20px;
}

.cleanup-reminder {
  margin-bottom: 20px;
}

.orders-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-id {
  font-family: monospace;
  font-size: 12px;
  cursor: pointer;
}

.product-name {
  font-weight: bold;
  margin-bottom: 4px;
}

.product-amount {
  color: #f39c12;
  font-weight: bold;
}

.query-key {
  font-family: monospace;
  font-size: 12px;
  background-color: #f5f5f5;
  padding: 2px 4px;
  border-radius: 3px;
}

.card-code-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-code {
  font-family: monospace;
  font-size: 12px;
  background-color: #f0f9ff;
  padding: 4px 8px;
  border-radius: 4px;
  border: 1px solid #b3d8ff;
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.no-card-code {
  color: #999;
}

.cleanup-dialog-content {
  padding: 10px 0;
}

.cleanup-tip {
  margin-top: 15px;
  color: #666;
  font-size: 14px;
}

.process-dialog-content {
  padding: 10px 0;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .action-buttons .el-col {
    margin-bottom: 10px;
  }
  
  .pagination {
    margin-top: 15px;
  }
}
</style> 