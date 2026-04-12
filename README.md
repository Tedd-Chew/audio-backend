# 音响产品管理系统（后端）
## 项目介绍
基于 SpringBoot 开发的**企业级音响产品管理后端系统**，实现用户认证、产品 CRUD、分页查询、Redis 高并发缓存、RAG 智能问答、异步任务、定时数据同步等完整能力。系统深度优化缓存架构，解决**缓存穿透、缓存雪崩、缓存击穿**三大经典问题，采用异步线程池隔离耗时任务，是一套符合生产规范、可直接用于实习/求职展示的 Java 后端完整项目。

### 技术栈
- 核心框架：SpringBoot 2.7.x
- 数据持久层：MyBatis-Plus + MySQL 8.0
- 缓存&高可用：Redis（**RDB+AOF持久化**、热点缓存、缓存防穿透/雪崩/击穿）
- 认证授权：JWT 无状态登录认证
- 并发&异步：**自定义线程池**、异步任务、**定时任务+事务同步**
- AI 增强：RAG 检索增强生成 + 大模型智能问答
- 工具：Lombok、Postman
- 构建工具：Maven

---

## 接口文档
### 基础约定
- 接口前缀：`/api`
- 统一返回格式：
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {}
}
```
- 认证方式：JWT Token，除登录、AI 聊天接口外，所有接口需在请求头携带 `Authorization: Token值`

### 1. 用户模块接口
| 接口地址 | 请求方式 | 接口说明 |
| --- | --- | --- |
| `/api/user/login` | POST | 用户登录 |

### 2. 产品模块接口
| 接口地址 | 请求方式 | 接口说明 |
| --- | --- | --- |
| `/api/product/list` | GET | 查询产品列表（直连数据库） |
| `/api/product/list/page` | GET | **产品分页查询** |
| `/api/product/{id}` | GET | **根据ID查询商品（带Redis缓存）** |
| `/api/product/add` | POST | 新增产品 |
| `/api/product/update` | PUT | 修改产品 |
| `/api/product/delete/{id}` | DELETE | 删除产品 |

### 3. AI 智能问答模块（RAG 增强）
| 接口地址 | 请求方式 | 接口说明 |
| --- | --- | --- |
| `/api/ai/chat` | POST | **AI 智能问答（异步线程池执行，不阻塞主线程）** |

**请求参数（JSON）**
```json
{
  "message": "推荐一款性价比高的蓝牙音响"
}
```

---

## 项目运行说明
### 环境要求
- JDK 8+
- MySQL 8.0+
- Redis 3.0+
- Maven 3.6+
- 大模型 API Key（RAG 模块）

### 部署步骤
1. 创建数据库 `audio_db`，执行建表语句
2. 配置 MySQL、Redis 连接信息
3. 配置 RAG 大模型参数（非必需）
4. 启动主类：AudiobackendApplication
5. 接口访问地址：http://localhost:8080

---

## 项目结构
```
com.example.audiobackend
├── AudiobackendApplication.java  // 启动类
├── controller/                  // 控制层
│   ├── UserController.java
│   ├── ProductController.java
│   └── AiController.java        
├── service/                     // 业务层
│   ├── impl/
│   │   ├── ProductServiceImpl.java
│   │   └── ChatServiceImpl.java 
├── mapper/                      // 数据访问
├── entity/                      // 实体
├── common/                      // 通用工具/返回值
└── config/                      // 配置类（线程池、Redis、定时任务）
```

---

## 核心功能亮点
### 1. 高并发缓存架构（核心亮点）
- 使用 **Redis Hash** 结构缓存热点商品数据，查询性能提升显著
- **完整解决三大缓存问题**：
  - 缓存穿透：空值缓存 + ID 合法性校验
  - 缓存雪崩：过期时间随机化 +  Redis 持久化
  - 缓存击穿：互斥锁（Lock）控制并发查询
- 增删改操作严格遵循 **旁路缓存模式**，先更新数据库，再删除缓存，保证数据一致性
- Redis 开启 **RDB+AOF 混合持久化**，服务重启不丢失数据

### 2. 异步&并发优化
- **自定义线程池**执行 AI RAG 问答，耗时任务异步化
- 不阻塞 Tomcat 主线程，避免接口超时、线程耗尽
- 商品浏览量/计数使用**异步更新**，提升接口响应速度

### 3. 定时任务 + 事务一致性
- 定时任务（5分钟）**异步同步 Redis 计数到 MySQL**
- 方法添加 `@Transactional` 事务保证数据原子性
- 防止多节点重复执行，生产可直接扩展分布式锁

### 4. 分页查询优化
- 基于 MyBatis-Plus 实现标准分页，避免全表查询
- 控制单次数据加载量，大幅降低数据库与网络开销

### 5. RAG 智能问答
- 双轮对话：AI 自动判断是否需要查询数据库
- 仅允许 `SELECT` 查询，保证 SQL 安全
- 基于真实业务数据回答，不编造内容

### 6. 工程化规范
- 统一返回体 + 全局异常处理
- JWT 无状态认证，拦截器统一鉴权
- 增删改接口全部添加事务保证
- 缓存、锁、Key 统一规范管理

---

## 注意事项
1. 商品详情使用 Redis 缓存，列表不做缓存，兼顾性能与一致性
2. 增删改成功后自动删除对应缓存，保证数据一致
3. AI 问答**异步执行**，接口不会阻塞，不影响系统稳定性
4. Redis 开启持久化，重启后缓存与计数数据可恢复
5. 定时任务自动启动，无需手动调用，保证数据最终一致