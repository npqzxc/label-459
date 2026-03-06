# Word 转 PDF 终极解决方案 - 使用 LibreOffice

## 问题回顾

之前尝试的方案都无法正确转换 Word 文档：
1. ❌ **Apache POI + iText**：只能提取纯文本，丢失所有格式
2. ❌ **改进的格式处理**：虽然保留了部分格式，但仍然不够准确

## 最终解决方案：LibreOffice Headless

### 为什么选择 LibreOffice？

LibreOffice 是业界公认的**最准确**的开源 Word 转 PDF 解决方案：

✅ **完美保留格式**
- 字体、字号、颜色
- 粗体、斜体、下划线
- 段落对齐、行距、缩进
- 表格、边框、背景色
- 图片、图表
- 页眉、页脚
- 分页符、分栏
- 列表（有序/无序）
- 超链接

✅ **兼容性强**
- 支持 .doc（Word 97-2003）
- 支持 .docx（Word 2007+）
- 支持复杂的 Office 文档

✅ **开源免费**
- 无需商业许可
- 社区支持活跃

## 实现细节

### 1. Dockerfile 修改

**关键变更：**
- 从 Alpine 改为 Debian 基础镜像（LibreOffice 在 Debian 上更稳定）
- 安装 LibreOffice Writer 和必要的字体

```dockerfile
# 运行阶段
FROM eclipse-temurin:11-jre

# 安装 LibreOffice
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    libreoffice-writer \
    libreoffice-java-common \
    fonts-liberation \
    fonts-dejavu \
    && rm -rf /var/lib/apt/lists/*
```

**镜像大小对比：**
- Alpine 版本：~150MB
- Debian + LibreOffice 版本：~600MB
- **值得的代价**：准确性 > 镜像大小

### 2. Java 服务重写

**核心转换逻辑：**

```java
ProcessBuilder processBuilder = new ProcessBuilder(
    "libreoffice",
    "--headless",           // 无界面模式
    "--convert-to", "pdf",  // 转换为 PDF
    "--outdir", tempDir,    // 输出目录
    wordFile.getAbsolutePath()
);
```

**关键特性：**

1. **超时控制**：120 秒超时，防止卡死
2. **错误处理**：捕获 LibreOffice 输出，便于调试
3. **环境变量**：设置 HOME 避免权限问题
4. **文件验证**：确保 PDF 文件成功生成
5. **资源清理**：自动清理临时文件

### 3. 依赖简化

**移除的依赖：**
- ❌ Apache POI（不再需要）
- ❌ iText7（不再需要）

**保留的依赖：**
- ✅ Spring Boot Web
- ✅ Lombok
- ✅ Commons Codec（用于其他功能）

## 使用方法

### 重新构建并启动

```bash
# 停止现有服务
docker compose down

# 重新构建（会下载 LibreOffice，需要一些时间）
docker compose up --build -d

# 查看日志
docker compose logs -f backend
```

### 验证 LibreOffice 安装

启动时会看到类似日志：
```
LibreOffice is available: LibreOffice 7.x.x.x
```

### 测试转换

1. 上传任意 Word 文档（.doc 或 .docx）
2. 转换后的 PDF 应该**完美保留**所有格式
3. 下载 PDF 进行验证

## 性能考虑

### 转换速度

| 文档大小 | 预计转换时间 |
|---------|-------------|
| 小文档（< 1MB） | 2-5 秒 |
| 中等文档（1-10MB） | 5-15 秒 |
| 大文档（10-50MB） | 15-60 秒 |

### 资源消耗

- **CPU**：转换时会占用较高 CPU
- **内存**：LibreOffice 需要约 200-500MB 内存
- **磁盘**：临时文件会占用磁盘空间

### 优化建议

1. **并发控制**：限制同时转换的文档数量
2. **队列处理**：对于大量文档，使用消息队列异步处理
3. **缓存结果**：相同文档可以缓存转换结果
4. **定期清理**：定时清理临时文件

## 故障排查

### 问题 1：LibreOffice 未找到

**症状：**
```
LibreOffice is not available
```

**解决：**
```bash
# 进入容器检查
docker exec -it tool-platform-backend bash
libreoffice --version
```

### 问题 2：转换超时

**症状：**
```
LibreOffice conversion timed out after 120 seconds
```

**解决：**
- 检查文档是否过大或过于复杂
- 增加超时时间（修改 `CONVERSION_TIMEOUT_SECONDS`）
- 检查服务器资源是否充足

### 问题 3：PDF 未生成

**症状：**
```
PDF file was not created by LibreOffice
```

**解决：**
- 检查 Word 文档是否损坏
- 查看 LibreOffice 输出日志
- 确保临时目录有写权限

### 问题 4：中文乱码

**症状：**
PDF 中中文显示为方块或乱码

**解决：**
在 Dockerfile 中添加中文字体：
```dockerfile
RUN apt-get install -y --no-install-recommends \
    fonts-wqy-microhei \
    fonts-wqy-zenhei
```

## 与其他方案对比

| 方案 | 准确性 | 速度 | 镜像大小 | 成本 |
|-----|--------|------|---------|------|
| **LibreOffice** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | 600MB | 免费 |
| Apache POI + iText | ⭐⭐ | ⭐⭐⭐⭐⭐ | 150MB | 免费 |
| Docx4j | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | 200MB | 免费 |
| Aspose.Words | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | 小 | 💰 商业 |
| Microsoft Graph API | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | 无 | 💰 订阅 |

## 高级配置

### 自定义 LibreOffice 参数

可以在转换命令中添加更多参数：

```java
ProcessBuilder processBuilder = new ProcessBuilder(
    "libreoffice",
    "--headless",
    "--convert-to", "pdf",
    "--outdir", tempDir,
    "-env:UserInstallation=file:///tmp/libreoffice", // 自定义配置目录
    wordFile.getAbsolutePath()
);
```

### PDF 质量设置

LibreOffice 支持通过过滤器参数控制 PDF 质量：

```bash
--convert-to pdf:"writer_pdf_Export:SelectPdfVersion=1"
```

可选参数：
- `SelectPdfVersion=1`：PDF/A-1
- `UseTaggedPDF=true`：生成标签 PDF（可访问性）
- `ExportBookmarks=true`：导出书签

## 生产环境建议

### 1. 健康检查

添加 LibreOffice 可用性检查：

```yaml
# docker-compose.yml
services:
  backend:
    healthcheck:
      test: ["CMD", "libreoffice", "--version"]
      interval: 30s
      timeout: 10s
      retries: 3
```

### 2. 资源限制

```yaml
services:
  backend:
    deploy:
      resources:
        limits:
          cpus: '2'
          memory: 2G
        reservations:
          memory: 1G
```

### 3. 监控

记录关键指标：
- 转换成功率
- 平均转换时间
- 失败原因统计
- 资源使用情况

## 总结

✅ **LibreOffice 方案的优势：**
1. **准确性最高**：几乎完美还原 Word 文档
2. **功能完整**：支持所有 Word 特性
3. **开源免费**：无许可成本
4. **久经考验**：被广泛使用的成熟方案

⚠️ **需要注意：**
1. 镜像体积较大（~600MB）
2. 转换速度中等（但准确性值得）
3. 需要足够的服务器资源

这是目前**最佳的开源 Word 转 PDF 解决方案**！
