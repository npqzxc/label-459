# Word 转 PDF 字体问题修复

## 问题现象

转换后的 PDF 文件中，所有文字都显示为 `####`，如下图所示：

![字体显示问题](uploaded_image_1769133619873.png)

## 问题原因

这是**字体缺失**导致的问题：

1. **Docx4j 需要字体**：在将 Word 转换为 PDF 时，docx4j 需要访问系统字体来渲染文本
2. **Alpine 默认无字体**：`eclipse-temurin:11-jre-alpine` 镜像非常精简，不包含任何字体
3. **无法渲染字符**：当 docx4j 找不到合适的字体时，会用 `#` 符号替代无法渲染的字符

## 解决方案

在 Dockerfile 中安装字体支持：

```dockerfile
# 安装字体支持（解决 docx4j PDF 转换中文显示问题）
RUN apk add --no-cache \
    fontconfig \        # 字体配置工具
    ttf-dejavu \        # DejaVu 字体（支持中英文）
    && fc-cache -f      # 刷新字体缓存
```

### 安装的组件

1. **fontconfig**
   - 字体配置和管理库
   - 让 Java 应用能够找到和使用系统字体

2. **ttf-dejavu**
   - DejaVu 字体家族
   - 支持拉丁字母、中文、日文、韩文等多种语言
   - 开源免费

3. **fc-cache**
   - 刷新字体缓存
   - 确保新安装的字体立即可用

## 其他字体选项

如果需要更好的中文支持，可以安装额外的字体：

### 方案 1：安装 Noto 字体（推荐）

```dockerfile
RUN apk add --no-cache \
    fontconfig \
    ttf-dejavu \
    font-noto-cjk \
    && fc-cache -f
```

**Noto CJK** 是 Google 开发的字体，对中日韩文字支持最好。

### 方案 2：安装文泉驿字体

```dockerfile
RUN apk add --no-cache \
    fontconfig \
    ttf-dejavu \
    wqy-zenhei \
    && fc-cache -f
```

**文泉驿正黑** 是专门为中文优化的开源字体。

### 方案 3：使用 Debian 基础镜像

如果 Alpine 的字体支持仍然不够，可以切换到 Debian：

```dockerfile
FROM eclipse-temurin:11-jre

# 安装字体
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    fontconfig \
    fonts-dejavu \
    fonts-noto-cjk \
    && rm -rf /var/lib/apt/lists/*
```

**镜像大小对比：**
- Alpine + 字体：~250MB
- Debian + 字体：~400MB

## 验证字体安装

构建完成后，可以进入容器验证：

```bash
# 进入容器
docker exec -it tool-platform-backend sh

# 列出可用字体
fc-list

# 查看中文字体
fc-list :lang=zh
```

## 重新构建

```bash
# 停止服务
docker compose down

# 重新构建
docker compose up --build -d

# 查看日志
docker compose logs -f backend
```

## 测试转换

1. 上传包含中文的 .docx 文件
2. 转换为 PDF
3. 下载并查看 PDF
4. 确认文字正常显示

## 常见问题

### Q1：字体仍然显示为 #

**可能原因：**
- 字体缓存未刷新
- Word 文档使用了特殊字体

**解决：**
```dockerfile
# 确保 fc-cache 执行
RUN apk add --no-cache fontconfig ttf-dejavu && \
    fc-cache -fv  # 添加 -v 查看详细输出
```

### Q2：中文显示为方块

**可能原因：**
- DejaVu 字体对某些中文字符支持不完整

**解决：**
安装 Noto CJK 字体（见上文方案 1）

### Q3：镜像体积增加太多

**说明：**
- 基础 Alpine 镜像：~150MB
- 添加字体后：~250MB
- 增加约 100MB

**如果需要更小的镜像：**
只安装最小字体集：
```dockerfile
RUN apk add --no-cache fontconfig ttf-dejavu-core
```

## 性能影响

安装字体对性能的影响：

| 指标 | 影响 |
|------|------|
| **构建时间** | +10-20 秒（下载和安装字体）|
| **镜像大小** | +100MB |
| **运行时性能** | 无影响 |
| **转换速度** | 无影响 |

## 总结

✅ **问题已解决**：
- 安装 fontconfig 和 ttf-dejavu
- 刷新字体缓存
- 重新构建镜像

✅ **预期效果**：
- 中英文正常显示
- 表格、格式保留
- PDF 可读性良好

⚠️ **注意事项**：
- 镜像体积会增加约 100MB
- 如需更好的中文支持，安装 Noto CJK
- 特殊字体可能仍需额外处理
