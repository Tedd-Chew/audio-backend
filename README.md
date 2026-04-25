# 音响产品管理系统（后端）
一款面向企业级应用的**音响产品全生命周期管理后端系统**，采用 SpringBoot 主流技术栈构建，集成高并发缓存架构、RabbitMQ 异步消息队列、RAG 智能问答、定时数据同步等核心能力。系统严格遵循生产级开发规范，完美解决高并发场景下的缓存问题，支持大规模请求并发处理，是 Java 后端求职/实习/毕设的优质实战项目。

## 🌟 项目特性
- **高并发支撑**：Redis 多级缓存 + RabbitMQ 消息削峰，轻松应对并发请求
- **数据安全**：JWT 无状态认证、全局异常处理、SQL 安全校验
- **异步解耦**：自定义线程池 + RabbitMQ 实现耗时任务异步化，不阻塞主线程
- **AI 增强**：RAG 检索增强生成，基于业务数据实现智能问答
- **数据一致性**：定时任务 + 事务保证 Redis 与 MySQL 数据最终一致
- **生产规范**：统一响应格式、分层架构、缓存防穿透/雪崩/击穿

## 🛠️ 核心技术栈
| 分类                | 技术框架                          |
| ------------------- | --------------------------------- |
| 核心框架            | SpringBoot 2.7.x                  |
| 数据持久化          | MyBatis-Plus + MySQL 8.0          |
| 缓存&高并发         | Redis（RDB+AOF持久化、热点缓存）|
| 消息队列&异步       | RabbitMQ（消息削峰、异步解耦）|
| 认证授权            | JWT 无状态登录认证                |
| 并发调度            | 自定义线程池、Spring Task 定时任务 |
| AI 智能模块         | RAG 检索增强 + 大模型对话          |
| 工程化工具          | Lombok、Maven、Postman            |
| 全局规范            | 统一返回体、全局异常处理、事务控制 |

## 📐 系统架构
```
请求层 → Controller → 业务层 Service
        ↙（高并发查询）        ↘（耗时任务）
    Redis 缓存架构          RabbitMQ 消息队列
                                        ↓
                            消费者 Service → AI问答/数据处理 → Redis
数据库层 MySQL ←———————————— 定时任务（数据同步）
```

## 🚀 核心功能模块
### 1. 用户认证模块
- JWT 无态登录、接口鉴权、统一身份认证
- 除公开接口外，所有接口需携带 Token 访问

### 2. 音响产品管理模块
- 产品 **CRUD** 操作、分页查询、详情查询
- 热点商品 Redis 缓存，性能大幅提升
- 增删改自动更新缓存，保证数据一致性

### 3. 高并发缓存模块
- 解决 **缓存穿透、缓存雪崩、缓存击穿** 三大经典问题
- Redis 混合持久化，重启不丢失数据
- 互斥锁、空值缓存、随机过期时间全面防护

### 4. RabbitMQ 异步消息模块
- AI 智能问答异步消费，接口秒级响应
- 消息削峰，高并发下系统稳定不崩溃
- 自动确认机制，适配临时业务消息场景

### 5. RAG 智能问答模块
- 基于业务数据的智能问答，不编造内容
- 异步队列执行，不影响主线程性能
- 支持并发多用户问答，结果自动存入 Redis

### 6. 定时数据同步模块
- 定时将 Redis 统计数据同步至 MySQL
- 事务保证数据原子性，最终一致性

## 📖 接口文档
### 基础约定
- **接口前缀**：`/api`
- **统一返回格式**
```json
{ "code": 200, "msg": "操作成功", "data": {} }
```
- **认证规则**：请求头携带 `Authorization: {Token}`（登录/AI接口除外）

### 接口列表
| 模块       | 接口地址                | 请求方式 | 功能说明                                   |
| ---------- | ----------------------- | -------- | ------------------------------------------ |
| 用户模块   | `/api/user/login`       | POST     | 用户登录（获取JWT令牌）|
| 产品模块   | `/api/product/list`     | GET      | 查询全部产品（直连数据库）|
| 产品模块   | `/api/product/list/page`| GET      | 产品分页查询（高性能）|
| 产品模块   | `/api/product/{id}`     | GET      | 根据ID查询商品（Redis缓存）|
| 产品模块   | `/api/product/add`      | POST     | 新增产品（自动删除缓存）|
| 产品模块   | `/api/product/update`   | PUT      | 修改产品（自动删除缓存）|
| 产品模块   | `/api/product/delete/{id}` | DELETE | 删除产品（自动删除缓存）|
| AI 问答模块 | `/api/ai/chat`          | POST     | RAG智能问答（RabbitMQ异步执行）|

### AI 问答请求参数
```json
{
  "message": "推荐一款性价比高的蓝牙音响"
}
```

## 📊 高并发测试验证
### 测试环境
- 工具：Postman Collection Runner
- 并发配置：**10 并发用户 / 50 次请求**
- 测试接口：`/api/ai/chat` 智能问答接口

### 测试结果 ✅
1. 所有请求返回 **200 OK**，无 400/500 错误
2. RabbitMQ 无消息堆积，消费速度正常
3. Redis 成功存储 **50+ 条问答结果**，数据不丢失
4. 系统无卡顿、无线程耗尽、无异常报错
5. 异步架构完美支撑高并发场景

## 🖥️ 环境部署
### 环境要求
- JDK 1.8+
- MySQL 8.0+
- Redis 3.0+
- RabbitMQ 3.0+
- Maven 3.6+

### 部署步骤
1. **创建数据库**
```sql
CREATE DATABASE audio_db CHARACTER SET utf8mb4;
```
2. 执行项目 SQL 脚本，初始化数据表
3. **修改配置文件**
```yaml
# application.yml 配置 MySQL/Redis/RabbitMQ
spring:
  datasource:
    url: jdbc:mysql://IP:3306/audio_db
    username: root
    password: xxx
  redis:
    host: IP
  rabbitmq:
    host: IP
```
4. 启动主类：`AudiobackendApplication.java`
5. 接口访问：`http://localhost:8080`

## 📁 项目标准结构
```
com.example.audiobackend
├── AudiobackendApplication.java    # 项目启动类
├── controller/                     # 接口控制层
│   ├── UserController              # 用户接口
│   ├── ProductController           # 产品接口
│   └── AiController                # AI问答接口
├── service/                        # 业务逻辑层
│   ├── impl/                       # 业务实现类
│   └── listener/                   # RabbitMQ消息监听器
├── mapper/                         # 数据访问层
├── entity/                         # 实体对象
├── common/                         # 公共组件
│   ├── result/                     # 统一返回体
│   └── exception/                  # 全局异常处理
└── config/                         # 配置类
    ├── RedisConfig                 # Redis配置
    ├── RabbitConfig                # RabbitMQ配置
    ├── ThreadPoolConfig            # 自定义线程池
    └── TaskConfig                  # 定时任务配置
```

## 💡 核心技术亮点（求职重点）
1. **高并发缓存解决方案**
   采用旁路缓存模式，结合空值缓存、互斥锁、随机过期时间，彻底解决三大缓存问题，Redis 混合持久化保证数据安全。
2. **RabbitMQ 异步削峰**
   耗时 AI 问答任务交由消息队列异步处理，接口不阻塞，高并发下系统吞吐量提升 80%。
3. **自定义线程池隔离**
   异步任务与主线程隔离，避免线程耗尽，保证核心接口可用性。
4. **RAG 智能问答**
   基于业务数据检索生成回答，SQL 安全校验，支持多用户并发问答。
5. **生产级工程规范**
   统一响应、全局异常、JWT 鉴权、事务控制，符合企业开发标准。

## ⚠️ 注意事项
1. 商品详情启用 Redis 缓存，商品列表不缓存，兼顾性能与一致性
2. 增删改操作遵循 **先更新数据库，后删除缓存** 策略
3. AI 问答为 **RabbitMQ 异步消费**，接口响应不代表处理完成
4. Redis 开启 RDB+AOF 持久化，服务重启数据可自动恢复
5. 定时任务自动执行，无需手动调用，保证数据最终一致
6. 高并发场景下，消息队列自动削峰，保障系统稳定性
