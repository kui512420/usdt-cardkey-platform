<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <el-icon><User /></el-icon>
          <span>用户登录</span>
        </div>
      </template>
      
      <el-form 
        ref="loginFormRef" 
        :model="loginForm" 
        :rules="loginRules" 
        label-width="80px"
        @submit.prevent="handleLogin"
      >
        <el-form-item label="用户名" prop="username">
          <el-input 
            v-model="loginForm.username" 
            placeholder="请输入用户名"
            :prefix-icon="User"
            clearable
          />
        </el-form-item>
        
        <el-form-item label="密码" prop="password">
          <el-input 
            v-model="loginForm.password" 
            type="password" 
            placeholder="请输入密码"
            :prefix-icon="Lock"
            show-password
            clearable
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button 
            type="primary" 
            :loading="authStore.loading" 
            @click="handleLogin"
            style="width: 100%"
          >
            登录
          </el-button>
        </el-form-item>
        
        <el-form-item>
          <div class="login-links">
            <router-link to="/" class="link">
              返回首页
            </router-link>
          </div>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import type { LoginRequest } from '@/api/auth'

const router = useRouter()
const authStore = useAuthStore()

// 表单引用
const loginFormRef = ref<FormInstance>()

// 登录表单数据
const loginForm = reactive<LoginRequest>({
  username: '',
  password: ''
})

// 表单验证规则
const loginRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度在 3 到 50 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少 6 个字符', trigger: 'blur' }
  ]
}

// 处理登录
const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  try {
    await loginFormRef.value.validate()
    
    const result = await authStore.userLogin(loginForm)
    
    if (result.success) {
      ElMessage.success(result.message || '登录成功')
      
      // 根据用户角色跳转到不同页面
      if (authStore.isAdmin) {
        router.push('/admin')
      } else {
        router.push('/')
      }
    } else {
      ElMessage.error(result.message || '登录失败')
    }
  } catch (error) {
    console.error('Login validation failed:', error)
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100vh;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
  box-sizing: border-box;
}

.login-card {
  width: 100%;
  max-width: 400px;
  margin: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border-radius: 16px;
  backdrop-filter: blur(10px);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: bold;
  color: #409EFF;
}

.card-header .el-icon {
  margin-right: 8px;
  font-size: 24px;
}

.login-links {
  text-align: center;
  width: 100%;
}

.link {
  color: #409EFF;
  text-decoration: none;
  transition: color 0.3s ease;
}

.link:hover {
  color: #66b1ff;
  text-decoration: underline;
}

.divider {
  margin: 0 10px;
  color: #999;
}

:deep(.el-card__header) {
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
}

:deep(.el-card__body) {
  padding: 30px;
}

:deep(.el-form-item__label) {
  font-weight: 600;
}

:deep(.el-input__inner) {
  border-radius: 8px;
}

:deep(.el-button) {
  border-radius: 8px;
  font-weight: 600;
}
</style> 