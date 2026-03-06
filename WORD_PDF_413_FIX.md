# Word 转 PDF 功能 413 错误修复说明

## 问题描述

在使用 Word 转 PDF 功能时遇到 HTTP 413 错误：
```
POST /api/word/convert HTTP/1.1" 413 585
```

## 错误原因

**HTTP 413 Payload Too Large** 表示上传的文件超过了服务器允许的最大大小限制。

具体原因：
1. **Nginx 默认限制**：Nginx 默认的 `client_max_body_size` 为 1MB
2. **后端限制较小**：Spring Boot 之前配置的限制为 10MB

## 修复内容

### 1. Nginx 配置 (`frontend/nginx.conf`)

**添加的配置：**

```nginx
# 全局文件上传大小限制
client_max_body_size 50M;

# API 代理配置优化
location /api/ {
    # ... 其他配置 ...
    
    # 支持大文件上传
    proxy_request_buffering off;
    proxy_buffering off;
    
    # 增加超时时间，支持大文件转换
    proxy_connect_timeout 300s;
    proxy_send_timeout 300s;
    proxy_read_timeout 300s;
}
```

**作用：**
- ✅ 允许上传最大 50MB 的文件
- ✅ 关闭请求缓冲，提高大文件上传效率
- ✅ 增加超时时间到 5 分钟，支持大文件转换处理

### 2. Spring Boot 配置 (`backend/src/main/resources/application.yml`)

**修改的配置：**

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 50MB      # 从 10MB 增加到 50MB
      max-request-size: 50MB   # 从 10MB 增加到 50MB
```

**作用：**
- ✅ 后端支持接收最大 50MB 的文件
- ✅ 与 Nginx 配置保持一致

## 应用修复

修改配置后需要重新构建并重启服务：

```bash
# 停止当前服务
docker compose down

# 重新构建（因为修改了配置文件）
docker compose build

# 启动服务
docker compose up
```

或者使用一条命令：

```bash
docker compose down && docker compose up --build
```

## 验证修复

1. **测试小文件**（< 1MB）：应该正常工作
2. **测试中等文件**（1-10MB）：之前失败，现在应该成功
3. **测试大文件**（10-50MB）：现在应该可以上传和转换

## 文件大小限制说明

| 层级 | 配置项 | 限制大小 | 说明 |
|------|--------|----------|------|
| Nginx | `client_max_body_size` | 50MB | 前端反向代理限制 |
| Spring Boot | `max-file-size` | 50MB | 后端应用限制 |
| Spring Boot | `max-request-size` | 50MB | 整个请求大小限制 |

## 如果需要更大的文件

如果需要支持超过 50MB 的文件，需要同时修改：

1. **frontend/nginx.conf**：
   ```nginx
   client_max_body_size 100M;  # 或更大
   ```

2. **backend/src/main/resources/application.yml**：
   ```yaml
   max-file-size: 100MB
   max-request-size: 100MB
   ```

3. 重新构建并重启服务

## 注意事项

⚠️ **性能考虑**：
- 大文件转换可能需要较长时间和更多内存
- 建议根据服务器配置合理设置文件大小限制
- 可以考虑添加前端文件大小验证，提前提示用户

⚠️ **安全考虑**：
- 过大的文件限制可能导致服务器资源耗尽
- 建议在生产环境中根据实际需求设置合理的限制
- 可以考虑添加文件类型验证

## 其他可能的错误

如果修复后仍有问题，检查：

1. **502 Bad Gateway**：后端服务未启动或崩溃
2. **504 Gateway Timeout**：转换时间超过超时限制（已设置为 300s）
3. **500 Internal Server Error**：后端转换逻辑错误，查看后端日志
