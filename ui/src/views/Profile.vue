<template>
  <div class="profile-container">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <el-icon><UserFilled /></el-icon>
          <span>个人中心</span>
        </div>
      </template>
      
      <div class="profile-content">
        <!-- 用户信息展示 -->
        <div class="user-info">
          <el-avatar :size="80" :icon="UserFilled" class="user-avatar" />
          
          <div class="user-details">
            <h2>{{ authStore.userDisplayName }}</h2>
            <p class="user-role">
              <el-tag :type="authStore.isAdmin ? 'danger' : 'primary'">
                {{ authStore.isAdmin ? '管理员' : '普通用户' }}
              </el-tag>
            </p>
          </div>
        </div>
        
        <!-- 详细信息 -->
        <el-descriptions title="账户详情" :column="1" border class="user-descriptions">
          <el-descriptions-item label="用户ID">
            {{ user?.id }}
          </el-descriptions-item>
          <el-descriptions-item label="用户名">
            {{ user?.username }}
          </el-descriptions-item>
          <el-descriptions-item label="邮箱">
            {{ user?.email || '未设置' }}
          </el-descriptions-item>
          <el-descriptions-item label="昵称">
            {{ user?.nickname || '未设置' }}
          </el-descriptions-item>
          <el-descriptions-item label="手机号">
            {{ user?.phoneNumber || '未设置' }}
          </el-descriptions-item>
          <el-descriptions-item label="注册时间">
            {{ formatDate(user?.createdAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="最后登录">
            {{ formatDate(user?.lastLoginTime) || '首次登录' }}
          </el-descriptions-item>
        </el-descriptions>
        
        <!-- 操作按钮 -->
        <div class="action-buttons">
          <el-button type="primary" @click="refreshUserInfo">
            <el-icon><Refresh /></el-icon>
            刷新信息
          </el-button>
          
          <el-button v-if="authStore.isAdmin" type="warning" @click="goToAdmin">
            <el-icon><Setting /></el-icon>
            管理后台
          </el-button>
          
          <el-button type="info" @click="goToHome">
            <el-icon><HomeFilled /></el-icon>
            返回首页
          </el-button>
          
          <el-button type="danger" @click="handleLogout">
            <el-icon><SwitchButton /></el-icon>
            退出登录
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  UserFilled, 
  Refresh, 
  Setting, 
  HomeFilled, 
  SwitchButton 
} from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

// 计算属性
const user = computed(() => authStore.user)

// 格式化日期
const formatDate = (dateString?: string) => {
  if (!dateString) return ''
  return new Date(dateString).toLocaleString('zh-CN')
}

// 刷新用户信息
const refreshUserInfo = async () => {
  const success = await authStore.refreshUser()
  if (success) {
    ElMessage.success('用户信息已刷新')
  } else {
    ElMessage.error('刷新失败')
  }
}

// 跳转到管理后台
const goToAdmin = () => {
  router.push('/admin')
}

// 返回首页
const goToHome = () => {
  router.push('/')
}

// 退出登录
const handleLogout = async () => {
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
    router.push('/login')
  } catch {
    // 用户取消
  }
}

// 组件挂载时检查认证状态
onMounted(async () => {
  await authStore.checkAuth()
})
</script>

<style scoped>
.profile-container {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  min-height: 100vh;
  padding: 40px 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.profile-card {
  width: 100%;
  max-width: 800px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border-radius: 16px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
}

.card-header .el-icon {
  margin-right: 8px;
  font-size: 28px;
}

.profile-content {
  padding: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  margin-bottom: 30px;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  color: white;
}

.user-avatar {
  margin-right: 20px;
  border: 3px solid rgba(255, 255, 255, 0.3);
}

.user-details h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
}

.user-role {
  margin: 0;
}

.user-descriptions {
  margin-bottom: 30px;
}

.action-buttons {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: center;
}

.action-buttons .el-button {
  border-radius: 8px;
  font-weight: 600;
}

:deep(.el-card__header) {
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
}

:deep(.el-descriptions__title) {
  font-size: 18px;
  font-weight: 600;
  color: #409EFF;
}

:deep(.el-descriptions__label) {
  font-weight: 600;
}

@media (max-width: 768px) {
  .user-info {
    flex-direction: column;
    text-align: center;
  }
  
  .user-avatar {
    margin-right: 0;
    margin-bottom: 15px;
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .action-buttons .el-button {
    width: 100%;
  }
}
</style> 