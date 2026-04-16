# Audio Backend 前端测试应用

这是一个用于测试Audio Backend API接口的前端应用，使用React + Vite构建。

## 功能特性

- 产品管理：查看、添加、编辑、删除产品
- 用户管理：登录、退出登录
- AI聊天：与AI进行对话
- Hello接口测试：测试基础接口

## 技术栈

- React 18
- Vite
- Axios

## 本地运行

### 前置条件

- Node.js 16.0 或更高版本
- Audio Backend 服务已在本地运行（localhost:8080）

### 安装依赖

```bash
# 进入前端目录
cd frontend

# 安装依赖
npm install
```

### 启动开发服务器

```bash
# 启动开发服务器
npm run dev
```

开发服务器将在 http://localhost:3000 运行。

### 构建生产版本

```bash
# 构建生产版本
npm run build
```

构建产物将生成在 `dist` 目录中。

## 代理配置

前端应用通过Vite的代理功能将API请求转发到本地的Audio Backend服务：

- 前端地址：http://localhost:3000
- 后端地址：http://localhost:8080

所有以 `/api` 开头的请求都会被代理到后端服务。

## API接口测试

### 产品管理

- GET /api/product/list - 获取产品列表
- GET /api/product/page - 分页获取产品
- GET /api/product/{id} - 获取单个产品
- POST /api/product/add - 添加产品
- PUT /api/product/update - 更新产品
- DELETE /api/product/{id} - 删除产品

### 用户管理

- POST /api/user/login - 用户登录
- POST /api/user/logout - 用户退出登录

### AI聊天

- POST /api/ai/chat - 与AI聊天

### Hello接口

- GET /hello - 测试基础接口
