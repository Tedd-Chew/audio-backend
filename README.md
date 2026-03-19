<<<<<<< HEAD
# 音响网站后端接口文档

## 1. 项目概述

本项目是一个音响网站的后端服务，提供用户认证和产品管理等功能。

## 2. 基础信息

- 服务地址：`http://localhost:8080`
- 响应格式：JSON
- 统一响应结构：
  ```json
  {
    "code": 200,
    "message": "成功",
    "data": {}
  }
  ```

## 3. 接口列表

### 3.1 通用接口

#### 3.1.1 健康检查

- **接口地址**：`/hello`
- **请求方式**：GET
- **接口描述**：检查服务是否正常运行
- **响应示例**：
  ```json
  "音响网站后端启动成功！Maven配置没问题～"
  ```

### 3.2 用户接口

#### 3.2.1 登录

- **接口地址**：`/api/user/login`
- **请求方式**：POST
- **接口描述**：用户登录获取Token
- **请求体**：
  ```json
  {
    "username": "用户名",
    "password": "密码"
  }
  ```
- **响应示例**：
  ```json
  {
    "code": 200,
    "message": "成功",
    "data": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
  ```

#### 3.2.2 退出登录

- **接口地址**：`/api/user/logout`
- **请求方式**：POST
- **接口描述**：用户退出登录，使Token失效
- **请求头**：
  ```
  Authorization: Bearer {token}
  ```
- **响应示例**：
  ```json
  {
    "code": 200,
    "message": "成功",
    "data": "退出登录成功"
  }
  ```

### 3.3 产品接口

#### 3.3.1 获取产品列表

- **接口地址**：`/api/product/list`
- **请求方式**：GET
- **接口描述**：获取所有产品列表
- **响应示例**：
  ```json
  {
    "code": 200,
    "message": "成功",
    "data": [
      {
        "id": 1,
        "name": "产品名称",
        "price": 1999.99,
        "description": "产品描述"
      }
    ]
  }
  ```

#### 3.3.2 获取产品详情

- **接口地址**：`/api/product/{id}`
- **请求方式**：GET
- **接口描述**：根据ID获取产品详情
- **路径参数**：
  - `id`：产品ID
- **响应示例**：
  ```json
  {
    "code": 200,
    "message": "成功",
    "data": {
      "id": 1,
      "name": "产品名称",
      "price": 1999.99,
      "description": "产品描述"
    }
  }
  ```

#### 3.3.3 添加产品

- **接口地址**：`/api/product/add`
- **请求方式**：POST
- **接口描述**：添加新产品
- **请求体**：
  ```json
  {
    "name": "产品名称",
    "price": 1999.99,
    "description": "产品描述"
  }
  ```
- **响应示例**：
  ```json
  {
    "code": 200,
    "message": "成功",
    "data": null
  }
  ```

#### 3.3.4 更新产品

- **接口地址**：`/api/product/update`
- **请求方式**：PUT
- **接口描述**：更新产品信息
- **请求体**：
  ```json
  {
    "id": 1,
    "name": "新产品名称",
    "price": 2999.99,
    "description": "新产品描述"
  }
  ```
- **响应示例**：
  ```json
  {
    "code": 200,
    "message": "成功",
    "data": null
  }
  ```

#### 3.3.5 删除产品

- **接口地址**：`/api/product/{id}`
- **请求方式**：DELETE
- **接口描述**：根据ID删除产品
- **路径参数**：
  - `id`：产品ID
- **响应示例**：
  ```json
  {
    "code": 200,
    "message": "成功",
    "data": null
  }
  ```

## 4. 错误码说明

| 错误码 | 描述 |
|-------|------|
| 200 | 成功 |
| 500 | 服务器内部错误 |
| 其他 | 业务逻辑错误（具体错误信息在message字段） |

## 5. 认证说明

- 除了登录接口外，其他需要认证的接口需要在请求头中携带Token：
  ```
  Authorization: Bearer {token}
  ```
- Token有效期与JWT配置一致
- 退出登录后，Token会在Redis中被删除，立即失效

## 6. 技术栈

- 后端框架：Spring Boot
- 数据库：MySQL
- 缓存：Redis
- 认证：JWT
- API文档：Swagger
=======
# 音响产品管理系统（后端）
## 项目介绍
基于 SpringBoot 开发的音响产品管理后端系统，聚焦产品全生命周期管理与用户认证核心场景，实现产品增删改查、分页查询、图片上传、JWT 登录认证、Redis 缓存优化、全局异常处理等核心功能，适配前后端分离的中小型管理系统开发需求，是面向 Java 后端入门学习与暑期实习展示的完整项目。

### 技术栈
- 核心框架：SpringBoot 2.7.x
- 数据持久层：MyBatis + MySQL 8.0
- 缓存：Redis（String 类型缓存产品列表）
- 认证授权：JWT（无状态登录认证）
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
    "data": {} // 业务数据体
  }
  // 失败响应
  {
    "code": 500/401/400,
    "msg": "失败原因描述",
    "data": null
  }
  ```
- 认证方式：JWT Token，除登录接口外，所有接口需在请求头携带 `Authorization: Token值`
- 错误码说明：
  | 错误码 | 含义                 |
  |--------|----------------------|
  | 200    | 操作成功             |
  | 400    | 参数校验失败         |
  | 401    | 未登录/Token失效/无权限 |
  | 500    | 服务器内部错误       |

### 1. 用户模块接口
| 接口地址       | 请求方式 | 接口说明 | 请求参数（JSON）| 返回示例                          | 备注       |
|----------------|----------|----------|---------------------------------|-----------------------------------|------------|
| `/api/user/login` | POST     | 用户登录 | `{"username":"admin","password":"123456"}` | `{"code":200,"msg":"登录成功","data":"eyJhbGciOiJIUzI1NiJ9..."}` | 无需认证   |

### 2. 产品模块接口
| 接口地址               | 请求方式 | 接口说明       | 请求参数                          | 返回示例                                                                 | 备注                     |
|------------------------|----------|----------------|-----------------------------------|--------------------------------------------------------------------------|--------------------------|
| `/api/product/list`    | GET      | 查询产品列表   | 无                                | `{"code":200,"msg":"成功","data":[{"id":1,"name":"蓝牙音箱","price":199.99,"description":"便携款","imageUrl":"/images/1.jpg"}]}` | 需认证，Redis 缓存       |
| `/api/product/list/page` | GET    | 产品分页查询   | 路径参数：pageNum=1（默认）、pageSize=10（默认） | `{"code":200,"msg":"成功","data":{"list":[...],"total":20,"pages":2}}` | 需认证                   |
| `/api/product/add`     | POST     | 新增产品       | JSON：`{"name":"家庭影院","price":1999.99,"description":"5.1声道","imageUrl":""}` | `{"code":200,"msg":"新增成功","data":null}`                              | 需认证，参数非空校验     |
| `/api/product/update`  | PUT      | 修改产品       | JSON：`{"id":1,"name":"蓝牙音箱Pro","price":299.99,"description":"升级款","imageUrl":"/images/2.jpg"}` | `{"code":200,"msg":"修改成功","data":null}` | 需认证                   |
| `/api/product/delete/{id}` | DELETE | 删除产品 | 路径参数：id（产品ID）| `{"code":200,"msg":"删除成功","data":null}`                              | 需认证                   |
| `/api/product/upload/image` | POST | 图片上传 | 表单参数：file（jpg/png格式图片） | `{"code":200,"msg":"上传成功","data":"/images/171234567890.jpg"}`       | 需认证，限制文件大小     |

## 项目运行说明
### 环境要求
- JDK 8 及以上
- MySQL 8.0 及以上
- Redis 3.0 及以上
- Maven 3.6 及以上

### 部署步骤
1. **数据库配置**
   - 新建 MySQL 数据库：`audio_db`
   - 手动创建 `product` 表（字段与 `entity/Product` 类对应）：
     ```sql
     CREATE TABLE product (
       id BIGINT PRIMARY KEY AUTO_INCREMENT,
       name VARCHAR(50) NOT NULL COMMENT '产品名称',
       price DOUBLE NOT NULL COMMENT '产品价格',
       description VARCHAR(500) COMMENT '产品描述',
       image_url VARCHAR(255) COMMENT '产品图片路径'
     ) COMMENT '音响产品表';
     ```
2. **配置修改**
   - 复制 `application-template.properties` 并重命名为 `application-dev.properties`；
   - 修改数据库、Redis 连接配置：
     ```properties
     # MySQL 配置
     spring.datasource.url=jdbc:mysql://localhost:3306/audio_db?useUnicode=true&characterEncoding=utf8mb4&useSSL=false&serverTimezone=Asia/Shanghai
     spring.datasource.username=你的MySQL用户名
     spring.datasource.password=你的MySQL密码
     # Redis 配置
     spring.redis.host=localhost
     spring.redis.port=6379
     spring.redis.password=你的Redis密码（无则留空）
     ```
3. **启动项目**
   - 运行 `com.example.audiobackend.AudiobackendApplication` 类的 `main` 方法；
   - 项目启动后默认端口：8080。

### 接口调试
1. 推荐工具：Postman/ApiPost；
2. 调试流程：
   - 先调用 `/api/user/login` 获取 Token；
   - 在请求头添加 `Authorization: 你的Token值`，调用其他需认证接口；
   - 分页查询示例：`http://localhost:8080/api/product/list/page?pageNum=1&pageSize=10`。

## 项目结构（核心包）
```
com.example.audiobackend
├── AudiobackendApplication.java  // 项目启动类
├── controller/                   // 接口层，接收前端请求
│   ├── UserController.java       // 用户登录接口
│   └── ProductController.java    // 产品CRUD接口
├── service/                      // 业务逻辑层
│   ├── ProductService.java       // 产品业务接口
│   └── impl/                     // 业务逻辑实现类
├── mapper/                       // 数据访问层，操作数据库
│   └── ProductMapper.java
├── entity/                       // 实体类（对应数据库表）
│   ├── User.java
│   └── Product.java
├── common/                       // 通用工具/全局配置
│   ├── Result.java               // 统一返回结果
│   ├── GlobalExceptionHandler.java // 全局异常处理
│   └── JwtUtil.java              // JWT工具类
└── config/                       // 配置类
    ├── WebConfig.java            // 拦截器/跨域配置
    └── RedisConfig.java          // Redis配置
```

## 核心功能亮点
1. **性能优化**：Redis 缓存高频查询的产品列表，减少数据库访问压力；
2. **代码规范**：使用 Lombok 简化实体类，全局异常处理统一返回格式，参数校验避免非法请求；
3. **安全认证**：JWT 无状态登录认证，拦截器校验 Token，适配前后端分离场景；
4. **工程化设计**：分页查询避免大数据量返回，图片上传支持产品素材管理。

## 注意事项
1. 请勿将 `application-dev.properties`（含敏感配置）提交到代码仓库；
2. 图片上传功能默认保存到本地 `src/main/resources/static/images` 目录，生产环境建议替换为OSS；
3. Redis 缓存默认过期时间为 10 分钟，可根据需求在 `ProductServiceImpl` 中调整。
>>>>>>> 2bd02e691becb0c8042c5e40b0d8fc588ddca1c0
