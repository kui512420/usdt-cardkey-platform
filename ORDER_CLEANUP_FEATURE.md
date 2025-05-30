# 订单自动清理功能说明

## 功能概述

为了保持系统整洁和数据库性能，系统现已支持自动清理过期的未付款订单功能。

## 功能特性

### 1. 自动定时清理
- **默认执行时间**: 每天凌晨2点自动执行
- **清理条件**: 删除创建超过24小时且状态为"未付款"(PENDING)的订单
- **可配置**: 支持通过配置文件自定义清理时间和条件

### 2. 手动清理功能
- 管理员可以在后台手动触发清理任务
- 可以自定义清理的时间范围（1-168小时）
- 提供清理前的确认对话框，防止误操作

### 3. 用户提醒
- 在购买页面显示订单过期提醒
- 在订单查询页面显示自动清理说明
- 明确告知用户订单将在24小时后自动删除

## 技术实现

### 后端实现

#### 1. 数据库层 (OrderRepository)
```java
// 查询过期的未付款订单
@Select("SELECT * FROM orders WHERE status = 'PENDING' AND created_at < #{cutoffTime}")
List<Order> findUnpaidOrdersBeforeTime(LocalDateTime cutoffTime);

// 删除过期的未付款订单
@Delete("DELETE FROM orders WHERE status = 'PENDING' AND created_at < #{cutoffTime}")
int deleteUnpaidOrdersBeforeTime(LocalDateTime cutoffTime);
```

#### 2. 业务层 (OrderService)
```java
// 清理过期订单的核心方法
@Transactional
public int cleanupUnpaidOrders(int hours) {
    LocalDateTime cutoffTime = LocalDateTime.now().minusHours(hours);
    // 先查询要删除的订单（用于日志）
    List<Order> unpaidOrders = orderRepository.findUnpaidOrdersBeforeTime(cutoffTime);
    // 执行删除
    int deletedCount = orderRepository.deleteUnpaidOrdersBeforeTime(cutoffTime);
    return deletedCount;
}
```

#### 3. 定时任务 (OrderCleanupTask)
```java
@Component
@ConditionalOnProperty(name = "order.cleanup.enabled", havingValue = "true", matchIfMissing = true)
public class OrderCleanupTask {
    
    @Scheduled(cron = "${order.cleanup.cron:0 0 2 * * *}")
    public void cleanupUnpaidOrders() {
        // 自动清理逻辑
    }
}
```

#### 4. 管理接口 (OrderController)
```java
// 手动清理未付款订单
@PostMapping("/cleanup-unpaid")
public Map<String, Object> cleanupUnpaidOrders(@RequestBody Map<String, Object> request)
```

### 前端实现

#### 1. 购买页面提醒 (PaymentForm.vue)
- 在重要提示中添加24小时过期警告
- 添加独立的警告框显示订单清理规则

#### 2. 订单查询提醒 (OrderQuery.vue)
- 在查询提示中添加过期清理说明

#### 3. 管理后台 (OrderManagement.vue)
- 提供手动清理功能
- 显示自动清理配置信息
- 支持查看不同状态的订单列表

## 配置说明

### application.yml 配置项

```yaml
# 订单清理配置
order:
  cleanup:
    enabled: true # 是否启用自动清理
    cron: "0 0 2 * * *" # 执行时间（每天凌晨2点）
    hours: 24 # 清理多少小时前的未付款订单
```

### 配置说明
- `enabled`: 控制是否启用自动清理功能
- `cron`: 定时任务执行时间，使用标准cron表达式
- `hours`: 订单过期时间，单位小时

### 禁用自动清理
如果需要禁用自动清理功能，可以在配置文件中设置：
```yaml
order:
  cleanup:
    enabled: false
```

## 使用方法

### 管理员操作

1. **查看订单**: 在管理后台 -> 订单管理 -> 选择相应按钮查看不同状态的订单
2. **手动清理**: 点击"清理未付款订单"按钮，设置清理时间范围，确认执行
3. **监控日志**: 系统会在控制台输出清理任务的执行情况

### 用户提醒

1. **购买时提醒**: 用户在购买页面会看到订单过期警告
2. **查询时提醒**: 用户在查询订单时会看到自动清理说明

## 安全性考虑

1. **权限控制**: 手动清理功能仅限管理员使用
2. **确认机制**: 手动清理前有二次确认对话框
3. **日志记录**: 所有清理操作都有详细的日志记录
4. **数据恢复**: 清理前会记录被删除的订单信息

## 监控和日志

### 自动清理日志
```
=== 开始执行订单清理任务 (2024-01-01 02:00:00) ===
定时清理: 删除了 5 个超过 24 小时的未付款订单
删除订单: ID=abc123, 创建时间=2023-12-30 01:30:00
订单清理任务完成，共删除 5 个超过 24 小时的未付款订单
=== 订单清理任务结束 ===
```

### 手动清理日志
管理员手动清理也会产生类似的日志记录。

## 注意事项

1. **不可恢复**: 被清理的订单无法恢复，请谨慎操作
2. **仅删除未付款**: 只会删除状态为PENDING的订单，已支付订单不受影响
3. **时区考虑**: 清理时间基于服务器时区（GMT+8）
4. **性能影响**: 清理任务选择在凌晨执行，避免影响业务高峰期

## 常见问题

### Q: 如何修改清理时间？
A: 修改配置文件中的 `order.cleanup.cron` 值。

### Q: 如何修改过期时间？
A: 修改配置文件中的 `order.cleanup.hours` 值。

### Q: 如何临时禁用自动清理？
A: 设置 `order.cleanup.enabled: false` 并重启应用。

### Q: 清理的订单能恢复吗？
A: 不能。订单清理是物理删除，无法恢复。请确保用户了解这一点。

### Q: 清理任务失败怎么办？
A: 检查日志中的错误信息，通常是数据库连接或权限问题。可以通过管理后台手动执行清理。

## 更新历史

- **v1.0** (2024-01-01): 初始版本，支持自动清理和手动清理功能
- 添加了用户提醒功能
- 完善了管理后台界面
- 增加了配置文件支持 