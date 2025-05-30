<template>
  <div class="home-container">
    <!-- 顶部导航栏 -->
    <el-header class="header">
      <div class="header-content">
        <div class="logo">
          <el-icon><Shop /></el-icon>
          <span>卡密发货系统</span>
        </div>
        
        <div class="user-nav">
          <template v-if="authStore.isLoggedIn">
            <el-dropdown @command="handleCommand">
              <span class="user-info">
                <el-avatar :size="32" :icon="UserFilled" />
                <span class="username">{{ authStore.userDisplayName }}</span>
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">
                    <el-icon><User /></el-icon>
                    个人中心
                  </el-dropdown-item>
                  <el-dropdown-item v-if="authStore.isAdmin" command="admin">
                    <el-icon><Setting /></el-icon>
                    管理后台
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout">
                    <el-icon><SwitchButton /></el-icon>
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button type="primary" @click="$router.push('/login')">
              管理员登录
            </el-button>
          </template>
        </div>
      </div>
    </el-header>

    <!-- 主要内容区域 -->
    <el-main class="main-content">
      <div class="content-wrapper">
        <!-- 欢迎区域 -->
        <el-card class="welcome-card" v-if="authStore.isLoggedIn">
          <div class="welcome-content">
            <h2>欢迎回来，{{ authStore.userDisplayName }}！</h2>
            <p>
              <el-tag :type="authStore.isAdmin ? 'danger' : 'primary'">
                {{ authStore.isAdmin ? '管理员' : '普通用户' }}
              </el-tag>
            </p>
          </div>
        </el-card>

        <!-- 未登录用户的欢迎信息 -->
        <el-card class="welcome-card" v-else>
          <div class="welcome-content">
            <h2>欢迎使用卡密发货系统</h2>
            <p>选择您需要的商品，即可快速购买获取卡密</p>
          </div>
        </el-card>

        <!-- 功能区域 -->
        <el-row :gutter="20" class="function-area">
          <!-- 商品购买 - 所有人可用 -->
          <el-col :xs="24" :sm="12" :lg="6">
            <el-card class="function-card" shadow="hover">
              <template #header>
                <div class="card-header">
                  <el-icon><ShoppingCart /></el-icon>
                  <span>商品购买</span>
                </div>
              </template>
              <div class="card-content">
                <p>浏览和购买商品</p>
                <el-button type="primary" @click="showPaymentForm = true">
                  立即购买
                </el-button>
              </div>
            </el-card>
          </el-col>

          <!-- 订单查询 - 所有人可用 -->
          <el-col :xs="24" :sm="12" :lg="6">
            <el-card class="function-card" shadow="hover">
              <template #header>
                <div class="card-header">
                  <el-icon><Search /></el-icon>
                  <span>订单查询</span>
                </div>
              </template>
              <div class="card-content">
                <p>查询订单状态</p>
                <el-button type="success" @click="showOrderQuery = true">
                  查询订单
                </el-button>
              </div>
            </el-card>
          </el-col>

          <!-- 管理后台入口 (仅管理员) -->
          <el-col v-if="authStore.isAdmin" :xs="24" :sm="12" :lg="6">
            <el-card class="function-card" shadow="hover">
              <template #header>
                <div class="card-header">
                  <el-icon><Setting /></el-icon>
                  <span>管理后台</span>
                </div>
              </template>
              <div class="card-content">
                <p>系统管理中心</p>
                <el-button type="info" @click="$router.push('/admin')">
                  进入后台
                </el-button>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 功能组件弹窗 -->
        <el-dialog 
          v-model="showPaymentForm" 
          title="商品购买" 
          width="90%" 
          :max-width="800"
        >
          <PaymentForm />
        </el-dialog>

        <el-dialog 
          v-model="showOrderQuery" 
          title="订单查询" 
          width="90%" 
          :max-width="800"
        >
          <OrderQuery />
        </el-dialog>
      </div>
    </el-main>

    <!-- 底部 -->
    <el-footer class="footer">
      <div class="footer-content">
        <p>&copy; 2024 卡密发货系统. All rights reserved.</p>
        <p>
          <el-link @click="$router.push('/login')" type="primary">管理员入口</el-link>
        </p>
      </div>
    </el-footer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Shop,
  UserFilled,
  User,
  ArrowDown,
  Setting,
  SwitchButton,
  ShoppingCart,
  Search
} from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

// 导入功能组件
import PaymentForm from '@/components/PaymentForm.vue'
import OrderQuery from '@/components/OrderQuery.vue'

const router = useRouter()
const authStore = useAuthStore()

// 弹窗控制
const showPaymentForm = ref(false)
const showOrderQuery = ref(false)

// 处理用户菜单命令
const handleCommand = async (command: string) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'admin':
      router.push('/admin')
      break
    case 'logout':
      try {
        await ElMessageBox.confirm(
          '确定要退出登录吗？',
          '提示',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
          }
        )
        
        authStore.userLogout()
        ElMessage.success('已退出登录')
      } catch {
        // 用户取消
      }
      break
  }
}

// 组件挂载时初始化认证状态（如果有）
onMounted(() => {
  authStore.initAuth()
})
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 0;
  height: 60px;
}

.header-content {
  height: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo {
  display: flex;
  align-items: center;
  font-size: 20px;
  font-weight: bold;
}

.logo .el-icon {
  margin-right: 8px;
  font-size: 24px;
}

.user-nav {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 8px;
  transition: background-color 0.3s ease;
}

.user-info:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.username {
  font-weight: 500;
}

.main-content {
  flex: 1;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 20px;
}

.content-wrapper {
  max-width: 1200px;
  margin: 0 auto;
}

.welcome-card {
  margin-bottom: 20px;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.welcome-content {
  text-align: center;
  padding: 20px;
}

.welcome-content h2 {
  margin: 0 0 10px 0;
  color: #409EFF;
}

.function-area {
  margin-bottom: 20px;
}

.function-card {
  margin-bottom: 20px;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
}

.function-card:hover {
  transform: translateY(-4px);
}

.card-header {
  display: flex;
  align-items: center;
  font-size: 16px;
  font-weight: bold;
  color: #409EFF;
}

.card-header .el-icon {
  margin-right: 8px;
  font-size: 20px;
}

.card-content {
  text-align: center;
  padding: 10px 0;
}

.card-content p {
  margin: 0 0 15px 0;
  color: #666;
}

.footer {
  background-color: #f8f9fa;
  border-top: 1px solid #e9ecef;
  padding: 20px;
  height: auto;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  text-align: center;
  color: #666;
}

@media (max-width: 768px) {
  .header-content {
    padding: 0 15px;
  }
  
  .logo span {
    display: none;
  }
  
  .main-content {
    padding: 15px;
  }
  
  .welcome-content h2 {
    font-size: 18px;
  }
}
</style> 