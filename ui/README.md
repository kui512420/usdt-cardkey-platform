# USDT支付系统前端

这是USDT支付系统的前端部分，使用Vue 3 + TypeScript + Vite构建。

## 功能

- 创建USDT支付订单
- 查询支付订单状态

## 安装依赖

```bash
npm install
```

## 开发模式运行

```bash
npm run dev
```

## 构建生产版本

```bash
npm run build
```

## 预览生产构建

```bash
npm run preview
```

## 后端API

前端会通过Vite的代理功能连接到后端API：
- 创建订单: `/api/payment/create`
- 查询订单: `/api/payment/check`

确保后端服务运行在 `http://localhost:8080`.
