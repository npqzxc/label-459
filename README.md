# 在线工具平台 (Online Tool Platform)

一个基于 Spring Boot + Vue 2 的现代化在线工具平台，提供多种实用工具功能。

## 🛠 技术栈

### 后端
- **框架**: Spring Boot 2.7.18
- **JDK**: Java 8
- **构建工具**: Maven
- **核心依赖**:
  - Spring Web
  - Lombok
  - Apache POI 5.2.3 (Word文档处理)
  - iText 7.2.5 (PDF生成)
  - Commons Codec (加解密)

### 前端
- **框架**: Vue 2.6.14
- **UI组件库**: Element UI 2.15.13
- **HTTP客户端**: Axios 0.27.2
- **路由**: Vue Router 3.5.1
- **生产环境**: Nginx

## 🚀 启动指南

### 前置要求
- Docker Desktop 已安装并启动
- 确保端口 3000 和 8080 未被占用

### 一键启动
在项目根目录执行以下命令：

```bash
docker compose up --build
```

容器启动需要几分钟时间，等待构建完成后，你会看到如下日志：
```
tool-platform-backend  | Started ToolPlatformApplication in X seconds
tool-platform-frontend | /docker-entrypoint.sh: Configuration complete; ready for start up
```

## 🔗 服务地址

- **前端应用**: http://localhost:3000
- **后端API**: http://localhost:8080
- **后端健康检查**: http://localhost:8080/api/text/convert (POST请求)

## 📋 功能列表

### 1. 英文大小写转换
- 支持转换为全大写 (UPPERCASE)
- 支持转换为全小写 (lowercase)
- 支持转换为首字母大写 (Title Case)
- 一键复制转换结果
- 实时前端展示

### 2. Word转PDF
- 支持上传 `.doc` 和 `.docx` 格式文件
- 文件大小限制：10MB
- 自动转换为PDF格式
- 提供下载链接
- 转换过程中显示loading状态
- 转换失败给出友好错误提示

### 3. 文本加解密
- **Base64**: 编码/解码
- **MD5**: 单向加密
- **AES**: 对称加密/解密（需提供密钥）
- 一键复制处理结果
- 错误输入自动提示

## 🏗 工程结构

```
tool/
├── backend/                    # Spring Boot后端
│   ├── src/main/java/com/toolplatform/
│   │   ├── controller/        # REST控制器
│   │   ├── service/           # 业务逻辑层
│   │   ├── dto/               # 数据传输对象
│   │   ├── exception/         # 异常处理
│   │   └── config/            # 配置类
│   ├── pom.xml                # Maven配置
│   └── Dockerfile             # 后端Docker配置
├── frontend/                   # Vue前端
│   ├── src/
│   │   ├── views/             # 页面组件
│   │   ├── router/            # 路由配置
│   │   ├── api/               # API封装
│   │   └── App.vue            # 根组件
│   ├── package.json           # npm配置
│   ├── Dockerfile             # 前端Docker配置
│   └── nginx.conf             # Nginx配置
├── docker-compose.yml         # Docker编排配置
└── README.md                  # 项目文档
```

## 📡 API接口说明

### 1. 文本大小写转换
**接口**: `POST /api/text/convert`

**请求体**:
```json
{
  "text": "Hello World",
  "type": "uppercase"
}
```

**响应**:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "result": "HELLO WORLD"
  }
}
```

### 2. Word转PDF
**接口**: `POST /api/word/convert`

**请求**: `multipart/form-data`, 参数名 `file`

**响应**:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "filename": "xxx.pdf",
    "downloadUrl": "/api/word/download/xxx.pdf"
  }
}
```

### 3. 文本加解密
**接口**: `POST /api/crypto/process`

**请求体**:
```json
{
  "text": "Hello World",
  "algorithm": "base64",
  "action": "encode",
  "key": "mykey123"
}
```

**响应**:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "result": "SGVsbG8gV29ybGQ="
  }
}
```

## 🐳 Docker 架构

本项目采用 **100% 容器化** 架构：

- **后端容器**: 多阶段构建，使用 Maven 构建 + JRE 运行
- **前端容器**: 多阶段构建，使用 Node 构建 + Nginx 提供服务
- **网络通信**: 通过 Docker Network 实现容器间通信
- **端口映射**: 前端 3000 端口，后端 8080 端口

## 📦 Docker 镜像优化

- 使用 Alpine 基础镜像减小体积
- 多阶段构建分离构建和运行环境
- 配置 npm 淘宝镜像源加速依赖下载
- 通过 .dockerignore 排除不必要文件

## 🔧 开发模式

如需本地开发（不使用Docker）：

### 后端
```bash
cd backend
mvn spring-boot:run
```

### 前端
```bash
cd frontend
npm install
npm run serve
```

## ⚠️ 注意事项

1. 首次启动需要下载依赖和镜像，可能需要较长时间
2. 确保 Docker Desktop 有足够的内存分配（建议至少 4GB）
3. Word转PDF功能会在 `/tmp/uploads` 目录生成临时文件，自动清理
4. AES加密的密钥会自动填充到16字节（128位）
5. 文件上传限制为10MB，超过会返回错误提示

## 🛑 停止服务

```bash
docker compose down
```

如需清理所有数据和镜像：
```bash
docker compose down --rmi all -v
```

## 📝 版本信息

- **项目版本**: 1.0.0
- **最后更新**: 2026-01-22

## 📄 许可证

MIT License

---

**开发者备注**: 本项目严格遵循模块化设计原则，新增工具只需添加对应的Controller、Service和前端页面组件，无需修改现有代码。
