# MySQL 数据库配置指南

## 📋 前提条件

确保您的系统已安装MySQL 8.0或更高版本。

## 🚀 快速设置步骤

### 1. 创建数据库

```sql
-- 连接到MySQL
mysql -u root -p

-- 执行数据库创建脚本
source database-setup.sql
```

或者手动执行：

```sql
CREATE DATABASE IF NOT EXISTS cardcode_system 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_unicode_ci;
```

### 2. 配置数据库连接

在 `service/src/main/resources/application.properties` 中修改以下配置：

```properties
# MySQL数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/cardcode_system?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
spring.datasource.driverClassName=com.mysql.cj.jdbc.Driver
spring.datasource.username=root
spring.datasource.password=你的MySQL密码
```

### 3. 启动应用

```bash
cd service
./mvnw spring-boot:run
```

## 🔐 安全配置（生产环境推荐）

### 创建专用数据库用户

```sql
-- 创建专用用户
CREATE USER 'cardcode_user'@'localhost' IDENTIFIED BY 'your_secure_password';

-- 授予权限
GRANT ALL PRIVILEGES ON cardcode_system.* TO 'cardcode_user'@'localhost';

-- 刷新权限
FLUSH PRIVILEGES;
```

然后在 `application.properties` 中使用专用用户：

```properties
spring.datasource.username=cardcode_user
spring.datasource.password=your_secure_password
```

## 📊 数据库信息

- **数据库名**: `cardcode_system`
- **字符集**: `utf8mb4`
- **排序规则**: `utf8mb4_unicode_ci`
- **默认端口**: `3306`

## 🗂️ 表结构

系统会自动创建以下表：

1. **products** - 商品表
2. **card_codes** - 卡密表
3. **orders** - 订单表

## ⚡ 性能优化

已添加的索引：

### card_codes 表
- `idx_product_id` - 商品ID索引
- `idx_is_used` - 使用状态索引
- `idx_code` - 卡密代码索引

### orders 表
- `idx_order_id` - 订单ID索引
- `idx_product_id` - 商品ID索引
- `idx_status` - 订单状态索引
- `idx_is_delivered` - 发货状态索引

## 🔧 常见问题

### 连接被拒绝
确保MySQL服务正在运行：
```bash
# Windows
net start mysql

# Linux/Mac
sudo systemctl start mysql
```

### 时区问题
如果遇到时区错误，在MySQL中执行：
```sql
SET GLOBAL time_zone = '+8:00';
```

### 字符编码问题
确保MySQL配置文件（my.cnf/my.ini）包含：
```ini
[mysqld]
character-set-server=utf8mb4
collation-server=utf8mb4_unicode_ci

[mysql]
default-character-set=utf8mb4

[client]
default-character-set=utf8mb4
```

## 🔄 切换回H2数据库

如需切换回H2数据库，只需在 `application.properties` 中注释MySQL配置，取消注释H2配置即可。 