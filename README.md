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
