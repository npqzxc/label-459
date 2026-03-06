# Docker 构建错误修复说明

## 问题描述

在执行 `docker build` 时遇到以下错误：
```
Could not transfer artifact org.springframework.boot:spring-boot-starter-parent:pom:2.7.18 
from/to central (https://repo.maven.apache.org/maven2): transfer failed
Received fatal alert: handshake_failure
```

## 根本原因

这是一个 **SSL/TLS 握手失败**问题，主要原因包括：

1. **Java 8 的 TLS 支持过时**：Alpine Linux 中的 Java 8 版本不支持现代 TLS 协议
2. **Maven Central 仓库要求**：Maven Central 需要较新的 TLS 版本才能建立安全连接
3. **加密算法缺失**：`maven:3.8-eclipse-temurin-8-alpine` 镜像缺少必要的加密算法支持

## 解决方案

### ✅ 方案 1：升级到 Java 11（推荐，已应用）

**优点**：
- 完全解决 TLS 兼容性问题
- Java 11 是 LTS 版本，有长期支持
- 性能更好，安全性更高

**已修改的文件**：

1. **backend/Dockerfile**
   - 构建镜像：`maven:3.8-eclipse-temurin-8-alpine` → `maven:3.8-eclipse-temurin-11-alpine`
   - 运行镜像：`eclipse-temurin:8-jre-alpine` → `eclipse-temurin:11-jre-alpine`

2. **backend/pom.xml**
   - `java.version`: `1.8` → `11`
   - `maven.compiler.source`: `1.8` → `11`
   - `maven.compiler.target`: `1.8` → `11`

### 🔄 方案 2：保持 Java 8，使用非 Alpine 镜像（备选）

如果项目必须使用 Java 8，可以使用 `backend/Dockerfile.java8`：

```dockerfile
FROM maven:3.8-eclipse-temurin-8 AS build
# ... (非 Alpine 镜像有更好的 TLS 支持)
```

**使用方法**：
```bash
# 使用备选 Dockerfile 构建
docker build -f backend/Dockerfile.java8 -t tool-platform-backend ./backend

# 或修改 docker-compose.yml
services:
  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile.java8
```

**注意**：非 Alpine 镜像体积更大（约 200MB vs 50MB）

### 🔧 方案 3：配置 Maven 使用 HTTP 镜像（不推荐）

可以在 Dockerfile 中添加阿里云镜像配置，但这只是绕过问题，不是真正解决：

```dockerfile
RUN mkdir -p /root/.m2 && \
    echo '<settings><mirrors><mirror><id>aliyun</id><mirrorOf>central</mirrorOf><url>https://maven.aliyun.com/repository/public</url></mirror></mirrors></settings>' > /root/.m2/settings.xml
```

## 验证构建

```bash
# 构建后端镜像
docker build -t tool-platform-backend ./backend

# 或使用 docker-compose 构建所有服务
docker-compose build

# 运行服务
docker-compose up
```

## 其他可能的问题

如果升级到 Java 11 后仍有问题，检查：

1. **网络连接**：确保能访问 Maven Central
   ```bash
   curl -I https://repo.maven.apache.org/maven2/
   ```

2. **代理设置**：如果使用代理，需要配置 Maven 代理

3. **防火墙**：确保防火墙允许 HTTPS 连接

## 总结

当前已应用**方案 1**（升级到 Java 11），这是最彻底和推荐的解决方案。如果有特殊原因必须使用 Java 8，可以切换到 `Dockerfile.java8`。
