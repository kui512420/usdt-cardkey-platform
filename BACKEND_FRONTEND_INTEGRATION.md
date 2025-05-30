# 🔗 后端前端集成配置指南

## 📋 后端配合前端的关键配置

### 🛡️ Spring Security 配置

#### 1. JWT 认证流程
```java
// JWT过滤器自动处理Authorization头
Authorization: Bearer <token>
```

#### 2. 权限配置
```java
// 公开接口 - 无需登录
/api/auth/**          - 认证相关
/api/payment/**       - 支付购买
GET /api/products     - 商品浏览

// 管理员接口 - 需要ADMIN权限
POST/PUT/DELETE /api/products/**  - 商品管理
/api/cardcodes/**                 - 卡密管理
/api/orders/**                    - 订单管理
```

#### 3. CORS 配置
```java
// 允许前端跨域访问
allowedOriginPatterns: ["*"]
allowedMethods: ["GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"]
allowedHeaders: ["*"]
allowCredentials: true
```

### 🔐 认证 API 端点

#### 注册接口
```
POST /api/auth/register
Content-Type: application/json

{
  "username": "admin2",
  "password": "password123",
  "email": "admin2@example.com",
  "nickname": "管理员2",
  "phoneNumber": "13800138000"
}

Response:
{
  "success": true,
  "message": "注册成功",
  "userId": 2
}
```

#### 登录接口
```
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}

Response:
{
  "success": true,
  "message": "登录成功",
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "user": {
    "id": 1,
    "username": "admin",
    "role": "ADMIN",
    "nickname": "系统管理员",
    "email": "admin@cardcode.com",
    "createdAt": "2024-01-01T00:00:00"
  }
}
```

#### Token验证接口
```
POST /api/auth/validate
Authorization: Bearer <token>

Response:
{
  "success": true,
  "user": { ... }
}
```

#### 获取当前用户
```
GET /api/auth/me
Authorization: Bearer <token>

Response:
{
  "success": true,
  "user": { ... }
}
```

### 🛒 商品 API 配置

#### 公开访问 (GET)
```
GET /api/products        - 获取商品列表
GET /api/products/{id}   - 获取商品详情
```

#### 管理员权限 (POST/PUT/DELETE)
```
POST /api/products       - 创建商品
PUT /api/products/{id}   - 更新商品
DELETE /api/products/{id} - 删除商品
```

### 🔑 卡密管理 API
**全部需要 ADMIN 权限**
```
GET /api/cardcodes                    - 获取卡密列表
POST /api/cardcodes/import/{productId} - 导入卡密
GET /api/cardcodes/product/{productId} - 按商品查看卡密
```

### 💳 支付 API
**公开访问 - 无需登录**
```
POST /api/payment/create    - 创建支付订单
GET /api/payment/status     - 查询支付状态
```

## 🔧 关键技术配置

### 1. 密码加密
```java
@Autowired
private PasswordEncoder passwordEncoder;

// 注册时加密
user.setPassword(passwordEncoder.encode(password));

// 登录时验证
passwordEncoder.matches(rawPassword, encodedPassword)
```

### 2. JWT Token 配置
```yaml
jwt:
  secret: mySecretKey12345678901234567890123456789012345678901234567890123456789012345678901234567890
  expiration: 86400000 # 24小时
```

### 3. 数据库配置
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/cardcode_system
    username: root
    password: password
    driver-class-name: com.mysql.cj.jdbc.Driver
```

### 4. MyBatis Plus 配置
```yaml
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: auto
```

## 🚦 前端对接要点

### 1. API请求拦截器配置
```typescript
// 请求拦截器 - 添加token
authApi.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器 - 处理401
authApi.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)
```

### 2. 路由权限守卫
```typescript
router.beforeEach(async (to, from, next) => {
  if (to.meta.requiresAuth) {
    if (!authStore.isLoggedIn) {
      next('/login')
      return
    }
    
    const isValid = await authStore.checkAuth()
    if (!isValid) {
      next('/login')
      return
    }
    
    if (to.meta.requiresAdmin && !authStore.isAdmin) {
      next('/')
      return
    }
  }
  next()
})
```

### 3. 状态管理
```typescript
// Pinia Store
export const useAuthStore = defineStore('auth', () => {
  const user = ref<User | null>(null)
  const token = ref<string | null>(null)
  
  const isLoggedIn = computed(() => !!user.value && !!token.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')
  
  return { user, token, isLoggedIn, isAdmin }
})
```

## 🔄 启动顺序

### 1. 后端启动
```bash
cd service
./mvnw spring-boot:run
```
- 自动创建数据库表
- 初始化管理员用户 (admin/admin123)
- 启动在 http://localhost:8080

### 2. 前端启动
```bash
cd ui
npm install
npm run dev
```
- 启动在 http://localhost:5173
- 自动代理 /api 请求到后端

### 3. 测试流程
1. 访问 http://localhost:5173
2. 点击"管理员登录"
3. 使用 admin/admin123 登录
4. 测试各项功能

## 🛠️ 调试要点

### 1. CORS 问题
如果出现跨域错误，检查：
- SecurityConfig中的CORS配置
- 前端请求的Content-Type
- 预检请求(OPTIONS)是否正常

### 2. JWT 问题
如果Token验证失败，检查：
- JWT密钥配置是否一致
- Token格式是否正确（Bearer前缀）
- Token是否过期

### 3. 权限问题
如果403权限错误，检查：
- 用户角色是否正确(ADMIN)
- 接口权限配置是否正确
- JWT中的角色信息是否正确

### 4. 数据库问题
如果连接失败，检查：
- MySQL服务是否启动
- 数据库配置是否正确
- 数据库是否已创建

## 🎯 最佳实践

1. **开发环境**: 使用不同端口避免冲突
2. **生产环境**: 使用反向代理(Nginx)统一域名
3. **安全性**: 定期更换JWT密钥
4. **性能**: 合理设置Token过期时间
5. **监控**: 记录关键操作日志

---

## ✅ 验证清单

- [ ] 后端启动成功，端口8080可访问
- [ ] 前端启动成功，端口5173可访问
- [ ] 管理员用户自动创建成功
- [ ] 登录功能正常工作
- [ ] JWT Token正常生成和验证
- [ ] 权限控制正常工作
- [ ] CORS配置正常
- [ ] 数据库连接正常
- [ ] API接口响应正常

🎉 **配置完成后，前后端应该可以完美协作！** 