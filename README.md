# TACE 电商

基于 Spring Boot 3 + MyBatis + Redis + Spring AI 的全栈电商平台，涵盖商品浏览、购物车、订单管理，并集成 AI 智能客服。

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3.4.3 |
| ORM | MyBatis（注解 + XML 混合策略） |
| 数据库 | MySQL 8.0 |
| 缓存 | Redis（Lettuce 客户端） |
| 认证 | JWT（jjwt 0.12）+ Redis 双校验 |
| AI | Spring AI 1.1.3 + 阿里百炼 qwen3.5-plus |
| 构建 | Maven |
| 语言 | Java 17 |

## 项目结构

```
tace-ecommerce/
├── pom.xml
└── src/main/
    ├── java/com/ecommerce/
    │   ├── ECommerceApplication.java        # 启动入口
    │   │
    │   ├── config/                          # 配置层
    │   │   ├── AuthInterceptor.java          ★ JWT → Redis → ThreadLocal
    │   │   ├── JwtProperties.java            密钥 + TTL 配置
    │   │   └── WebMvcConfig.java             跨域 + 拦截器注册
    │   │
    │   ├── ai/                              # AI 客服模块
    │   │   ├── AiConfig.java                 ChatClient Bean
    │   │   ├── AiSystemPrompt.java           系统提示词
    │   │   └── EcommerceTools.java           ★ Function Calling 工具集
    │   │
    │   ├── entity/                          # 实体（7 张表）
    │   │   ├── User.java / Category.java / Product.java
    │   │   └── Cart.java / CartItem.java / Order.java / OrderItem.java
    │   │
    │   ├── dto/                             # 数据传输对象
    │   │   ├── UserDTO.java                 Redis/ThreadLocal 脱敏用户
    │   │   ├── request/                     (8) 请求体
    │   │   └── response/                    (8) 响应体 + ApiResponse + PageResult
    │   │
    │   ├── mapper/                          # MyBatis 数据层
    │   │   ├── UserMapper.java               @注解
    │   │   ├── ProductMapper.java            @注解 + XML（复杂搜索）
    │   │   └── ...
    │   │
    │   ├── service/ + impl/                 # 业务逻辑层
    │   │   ├── UserServiceImpl.java          ★ JWT 生成 + Redis 存储
    │   │   ├── ProductServiceImpl.java       ★ Redis 缓存（防穿透）
    │   │   ├── CategoryServiceImpl.java      ★ Redis 缓存（树结构）
    │   │   ├── CartServiceImpl.java          购物车去重 + 自动创建
    │   │   └── OrderServiceImpl.java         ★ 事务下单：校验 → 快照 → 扣库存 → 清购物车
    │   │
    │   ├── controller/                      # REST 接口
    │   │   ├── UserController.java           /api/users
    │   │   ├── AiController.java             ★ /api/ai/chat 流式 AI 对话
    │   │   └── ...
    │   │
    │   ├── utils/                           # 工具
    │   │   ├── JwtUtil.java                  JWT 生成/解析
    │   │   ├── UserHolder.java               ThreadLocal 上下文
    │   │   └── RedisConstants.java           缓存 key 管理
    │   │
    │   └── exception/                       # 异常处理
    │
    └── resources/
        ├── application.yml                  全部配置（DB+Redis+JWT+AI）
        ├── db/schema.sql                    建库建表 DDL
        └── mapper/
            ├── ProductMapper.xml             动态搜索 + 分页
            └── OrderMapper.xml               状态筛选 + 分页
```

## 快速启动

### 环境要求

- JDK 17+
- MySQL 8.0+
- Redis
- Maven 3.6+

### 1. 数据库

```bash
# 执行建表脚本
mysql -u root -p < src/main/resources/db/schema.sql
```

### 2. Redis

```bash
redis-server
```

### 3. AI API Key（可选，不配不影响电商功能）

前往 [百炼控制台](https://bailian.console.aliyun.com) 申请 API Key，然后在 IDEA 启动配置中设置环境变量：

```
AI_API_KEY=sk-你的密钥
```

或在 `application.yml` 中直接写入 `api-key`。

### 4. 启动

```bash
mvn spring-boot:run
# 或直接在 IDEA 中运行 ECommerceApplication
```

服务启动在 `http://localhost:8080`。

## API 接口

所有接口前缀 `/api`，响应格式：

```json
{ "code": 200, "message": "success", "data": { ... } }
```

### 用户模块（公开）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/users/register` | 注册 `{ username, password, nickname? }` |
| POST | `/api/users/login` | 登录，返回 JWT |

### 分类模块（读公开）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/categories` | 分类树 |
| GET/POST/PUT/DELETE | `/api/categories[/{id}]` | CRUD |

### 商品模块（读公开）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/products?page=1&keyword=&categoryId=` | 分页搜索 |
| GET/POST/PUT/DELETE | `/api/products[/{id}]` | CRUD |

### 购物车模块（需 `X-Token` 头）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/cart` | 查看购物车 |
| POST | `/api/cart/items` | 添加 `{ productId, quantity }` |
| PUT | `/api/cart/items/{id}` | 修改数量/勾选 |
| DELETE | `/api/cart/items/{id}` | 删除单项 |
| DELETE | `/api/cart` | 清空 |

### 订单模块（需 `X-Token` 头）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/orders` | 下单 `{ receiverName, receiverPhone, receiverAddress }` |
| GET | `/api/orders?page=&status=` | 订单列表 |
| GET | `/api/orders/{id}` | 订单详情 |

### AI 客服（公开）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/ai/chat?prompt=问题` | 流式 AI 对话 |

## 核心设计

### 认证链路

```
登录 → JWT（含 userId）→ Redis 存储 → 返回客户端
请求 → X-Token 头 → JWT 解析 → Redis 校验 → ThreadLocal → Controller
```

每次请求零 DB 查询，活跃用户自动续期。

### 缓存策略

| 数据 | Redis Key | TTL | 清理策略 |
|------|-----------|-----|---------|
| 登录态 | `login:user:{id}` | 30min | 自动过期 |
| 商品详情 | `cache:product:{id}` | 30min | 增删改时清理 |
| 商品列表 | `cache:product:page:*` | 10min | 增删改时通配清 |
| 分类树 | `cache:category:tree` | 1h | 增删改时清理 |

空值缓存 2 分钟，防止缓存穿透。

### 下单事务

```
1. 查购物车勾选商品
2. 校验商品在售 + 库存充足
3. 生成订单号 → 插入 order
4. 商品信息快照 → 批量插入 order_item
5. UPDATE stock = stock - qty WHERE stock >= qty （乐观锁防超卖）
6. 删除已下单的购物车项
   任一失败 → 全部回滚 (@Transactional)
```

### AI Function Calling

大模型可自动调用后端方法：

- `searchProducts` — 搜索商品
- `queryMyOrders` — 查当前用户订单
- `queryOrder` — 查订单详情

## 前端项目

前端使用 Vue 3 + Vite，位于 `../tace-ecommerce-frontend/`：

```bash
cd ../tace-ecommerce-frontend
npm install
npm run dev
# → http://localhost:3000
```

## License

MIT
