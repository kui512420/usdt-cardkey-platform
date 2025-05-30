<template>
  <div class="admin-container">
    <!-- 顶部导航 -->
    <el-header class="admin-header">
      <div class="header-content">
        <div class="header-left">
          <el-icon><Setting /></el-icon>
          <span>管理后台</span>
        </div>
        <div class="header-right">
          <el-button type="info" @click="$router.push('/')">
            <el-icon><HomeFilled /></el-icon>
            返回首页
          </el-button>
          <el-button type="primary" @click="$router.push('/profile')">
            <el-icon><User /></el-icon>
            个人中心
          </el-button>
        </div>
      </div>
    </el-header>

    <!-- 主要内容 -->
    <el-container class="admin-main">
      <!-- 侧边栏 -->
      <el-aside width="250px" class="admin-sidebar">
        <el-menu 
          :default-active="activeMenu" 
          @select="handleMenuSelect"
          class="admin-menu"
        >
          <el-menu-item index="overview">
            <el-icon><DataAnalysis /></el-icon>
            <span>系统概览</span>
          </el-menu-item>
          <el-menu-item index="products">
            <el-icon><Box /></el-icon>
            <span>商品管理</span>
          </el-menu-item>
          <el-menu-item index="cardcodes">
            <el-icon><Key /></el-icon>
            <span>卡密管理</span>
          </el-menu-item>
          <el-menu-item index="orders">
            <el-icon><Document /></el-icon>
            <span>订单管理</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 内容区域 -->
      <el-main class="admin-content">
        <!-- 系统概览 -->
        <div v-if="activeMenu === 'overview'" class="overview-section">
          <h2>系统概览</h2>
          <el-row :gutter="20" class="stats-row">
            <el-col :xs="24" :sm="12" :lg="6">
              <el-card class="stats-card">
                <div class="stats-content">
                  <div class="stats-number">{{ statistics.totalProducts || 0 }}</div>
                  <div class="stats-label">总商品数</div>
                </div>
                <el-icon class="stats-icon"><Box /></el-icon>
              </el-card>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="6">
              <el-card class="stats-card">
                <div class="stats-content">
                  <div class="stats-number">{{ statistics.totalCardCodes || 0 }}</div>
                  <div class="stats-label">总卡密数</div>
                </div>
                <el-icon class="stats-icon"><Key /></el-icon>
              </el-card>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="6">
              <el-card class="stats-card">
                <div class="stats-content">
                  <div class="stats-number">{{ statistics.totalOrders || 0 }}</div>
                  <div class="stats-label">总订单数</div>
                </div>
                <el-icon class="stats-icon"><Document /></el-icon>
              </el-card>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="6">
              <el-card class="stats-card">
                <div class="stats-content">
                  <div class="stats-number">{{ statistics.totalUsers || 0 }}</div>
                  <div class="stats-label">总用户数</div>
                </div>
                <el-icon class="stats-icon"><UserFilled /></el-icon>
              </el-card>
            </el-col>
          </el-row>
          
          <!-- 详细统计 -->
          <el-row :gutter="20" v-if="detailedStats" class="detailed-stats-row">
            <el-col :xs="24" :sm="12" :lg="8">
              <el-card class="mini-stats-card">
                <div class="mini-stats-content">
                  <div class="mini-stats-number">{{ detailedStats.activeProducts || 0 }}</div>
                  <div class="mini-stats-label">活跃商品</div>
                </div>
              </el-card>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="8">
              <el-card class="mini-stats-card">
                <div class="mini-stats-content">
                  <div class="mini-stats-number">{{ detailedStats.availableCardCodes || 0 }}</div>
                  <div class="mini-stats-label">可用卡密</div>
                </div>
              </el-card>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="8">
              <el-card class="mini-stats-card">
                <div class="mini-stats-content">
                  <div class="mini-stats-number">{{ detailedStats.deliveredOrders || 0 }}</div>
                  <div class="mini-stats-label">已发货订单</div>
                </div>
              </el-card>
            </el-col>
          </el-row>
          
          <el-alert 
            title="欢迎使用管理后台" 
            type="info" 
            :closable="false"
            style="margin-top: 20px"
          >
            <template #default>
              <p>通过左侧菜单可以管理系统的各个模块。</p>
              <ul>
                <li>商品管理：添加、编辑、删除商品</li>
                <li>卡密管理：导入、查看、管理卡密库存</li>
                <li>订单管理：查看、处理订单状态</li>
              </ul>
            </template>
          </el-alert>
        </div>

        <!-- 商品管理 -->
        <div v-if="activeMenu === 'products'" class="products-section">
          <h2>商品管理</h2>
          <ProductForm />
        </div>

        <!-- 卡密管理 -->
        <div v-if="activeMenu === 'cardcodes'" class="cardcodes-section">
          <h2>卡密管理</h2>
          <CardCodeForm />
        </div>

        <!-- 订单管理 -->
        <div v-if="activeMenu === 'orders'" class="orders-section">
          <h2>订单管理</h2>
          <OrderManagement />
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Setting,
  HomeFilled,
  User,
  DataAnalysis,
  Box,
  Key,
  Document,
  UserFilled
} from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { getSystemStatistics, getDetailedStatistics, type SystemStatistics, type DetailedStatistics } from '@/api/statistics'

// 导入组件
import ProductForm from '@/components/ProductForm.vue'
import CardCodeForm from '@/components/CardCodeForm.vue'
import OrderQuery from '@/components/OrderQuery.vue'
import OrderManagement from '@/components/OrderManagement.vue'

const router = useRouter()
const authStore = useAuthStore()

// 当前激活的菜单
const activeMenu = ref('overview')

// 统计数据
const statistics = ref<SystemStatistics>({
  totalProducts: 0,
  totalCardCodes: 0,
  totalOrders: 0,
  totalUsers: 0
})

const detailedStats = ref<DetailedStatistics | null>(null)

// 处理菜单选择
const handleMenuSelect = (index: string) => {
  activeMenu.value = index
}

// 加载统计数据
const loadStatistics = async () => {
  try {
    // 加载基础统计
    const basicResponse = await getSystemStatistics()
    if (basicResponse.success && basicResponse.data) {
      statistics.value = basicResponse.data as SystemStatistics
    }

    // 加载详细统计
    const detailedResponse = await getDetailedStatistics()
    if (detailedResponse.success && detailedResponse.data) {
      detailedStats.value = detailedResponse.data as DetailedStatistics
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
    ElMessage.error('加载统计数据失败')
  }
}

// 检查管理员权限
const checkAdminPermission = () => {
  if (!authStore.isLoggedIn) {
    ElMessage.error('请先登录')
    router.push('/login')
    return false
  }
  
  if (!authStore.isAdmin) {
    ElMessage.error('权限不足，仅管理员可访问')
    router.push('/')
    return false
  }
  
  return true
}

// 组件挂载时检查权限和加载数据
onMounted(async () => {
  await authStore.checkAuth()
  if (checkAdminPermission()) {
    loadStatistics()
  }
})
</script>

<style scoped>
.admin-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.admin-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 0;
  height: 60px;
}

.header-content {
  height: 100%;
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-left {
  display: flex;
  align-items: center;
  font-size: 20px;
  font-weight: bold;
}

.header-left .el-icon {
  margin-right: 8px;
  font-size: 24px;
}

.header-right {
  display: flex;
  gap: 12px;
}

.admin-main {
  flex: 1;
  background: #f5f7fa;
}

.admin-sidebar {
  background: white;
  border-right: 1px solid #e4e7ed;
}

.admin-menu {
  border: none;
  height: 100%;
}

.admin-content {
  padding: 20px;
  overflow-y: auto;
}

.overview-section h2,
.products-section h2,
.cardcodes-section h2,
.orders-section h2 {
  margin: 0 0 20px 0;
  color: #409EFF;
  font-size: 24px;
  font-weight: 600;
}

.stats-row {
  margin-bottom: 20px;
}

.detailed-stats-row {
  margin-bottom: 20px;
}

.stats-card {
  position: relative;
  overflow: hidden;
  border-radius: 12px;
  margin-bottom: 20px;
}

.mini-stats-card {
  border-radius: 8px;
  margin-bottom: 15px;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
}

.stats-content {
  position: relative;
  z-index: 2;
}

.mini-stats-content {
  text-align: center;
  padding: 10px;
}

.stats-number {
  font-size: 36px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 8px;
}

.mini-stats-number {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 5px;
}

.stats-label {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.mini-stats-label {
  font-size: 12px;
  color: #666;
  font-weight: 500;
}

.stats-icon {
  position: absolute;
  top: 50%;
  right: 20px;
  transform: translateY(-50%);
  font-size: 48px;
  color: rgba(64, 158, 255, 0.1);
  z-index: 1;
}

:deep(.el-card__body) {
  padding: 20px;
}

:deep(.el-menu-item) {
  height: 50px;
  line-height: 50px;
  margin: 4px 8px;
  border-radius: 8px;
}

:deep(.el-menu-item.is-active) {
  background-color: #409EFF;
  color: white;
}

:deep(.el-menu-item:hover) {
  background-color: #ecf5ff;
}

:deep(.el-menu-item.is-active:hover) {
  background-color: #409EFF;
}

@media (max-width: 768px) {
  .admin-main {
    flex-direction: column;
  }
  
  .admin-sidebar {
    width: 100% !important;
    height: auto;
  }
  
  .admin-menu {
    display: flex;
    overflow-x: auto;
  }
  
  :deep(.el-menu-item) {
    white-space: nowrap;
    margin: 4px;
  }
  
  .header-content {
    padding: 0 15px;
  }
  
  .header-right {
    flex-direction: column;
    gap: 8px;
  }
}
</style> 