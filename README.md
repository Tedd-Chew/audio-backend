# 音响产品管理系统（后端）
## 项目介绍
基于 SpringBoot 开发的音响产品管理后端系统，聚焦产品全生命周期管理与用户认证核心场景，实现产品增删改查、分页查询、图片上传、JWT 登录认证、Redis 热点缓存优化、RAG 智能问答、全局异常处理等核心功能，适配前后端分离的中小型管理系统开发需求，是面向 Java 后端入门学习与暑期实习展示的完整项目。

### 技术栈
- 核心框架：SpringBoot 2.7.x
- 数据持久层：MyBatis + MySQL 8.0
- 缓存：Redis（Hash 结构缓存热点商品详情）
- 认证授权：JWT（无状态登录认证）
- AI 增强：RAG 检索增强生成 + 大模型对话
- 工具：Lombok（简化实体类代码）、Postman（接口调试）
- 构建工具：Maven

## 接口文档
### 基础约定
- 接口前缀：`/api`
- 统一返回格式：
```json
// 成功响应
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
| `/api/product/list/page` | GET | 产品分页查询 |
| `/api/product/add` | POST | 新增产品 |
| `/api/product/update` | PUT | 修改产品 |
| `/api/product/delete/{id}` | DELETE | 删除产品 |
| `/api/product/upload/image` | POST | 图片上传 |

### 3. AI 智能问答模块（RAG 增强）
| 接口地址 | 请求方式 | 接口说明 |
| --- | --- | --- |
| `/api/ai/chat` | POST | AI 智能问答（基于私有知识库 RAG） |

**请求参数（JSON）**
```json
{
  "message": "推荐一款性价比高的蓝牙音响"
}
```

**返回示例**
```json
{
  "reply": "为你推荐性价比款蓝牙音箱：价格199.99，便携轻便，续航持久..."
}
```

**功能说明**
- 基于 **RAG 检索增强生成** 技术
- 从产品知识库中检索相关信息
- 结合大模型生成精准、专业的回答
- 无需登录即可使用

## 项目运行说明
### 环境要求
- JDK 8+
- MySQL 8.0+
- Redis 3.0+
- Maven 3.6+
- 大模型 API Key（RAG 模块）

### 部署步骤
1. 创建数据库 `audio_db`，执行 product 建表语句
2. 配置 MySQL、Redis 连接信息
3. 配置 RAG 大模型参数（非必需，系统可正常运行）
4. 启动主类：AudiobackendApplication
5. 接口访问地址：http://localhost:8080

## 项目结构
```
com.example.audiobackend
├── AudiobackendApplication.java  // 启动类
├── controller/
│   ├── UserController.java
│   ├── ProductController.java
│   └── AiController.java        // AI 对话接口
├── service/
│   ├── impl/
│   │   ├── ProductServiceImpl.java
│   │   └── ChatServiceImpl.java // RAG 核心实现
├── mapper/
├── entity/
├── common/
└── config/
```

## 核心功能亮点
1. **热点缓存优化**
   使用 Redis Hash 结构缓存热点商品详情，只缓存高频访问数据，内存占用低、查询速度快，符合企业级缓存设计规范。

2. **RAG 智能问答（核心亮点）**
   接入检索增强生成技术，构建产品私有知识库，实现 AI 智能问答、产品推荐、参数咨询等增强功能，让传统管理系统具备 AI 能力。

3. **安全认证**
   JWT 无状态认证，拦截器统一鉴权，适配前后端分离架构。

4. **工程化实践**
   全局异常处理、统一返回格式、分页查询、文件上传，完整覆盖企业开发需求。

## 注意事项
1. Redis 仅缓存热点商品详情，列表接口不做缓存，避免数据不一致
2. AI 模块依赖大模型接口，未配置时不影响基础功能运行
3. 图片上传默认本地存储，生产环境可切换 OSS