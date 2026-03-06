# Word 转 PDF 中文字体最终解决方案

## 问题总结

经过多次尝试，中文显示为 `####` 或乱码的根本原因是：

### Alpine Linux 的字体限制

1. **ttf-dejavu 不支持中文**
   - DejaVu 字体主要支持拉丁字母
   - 对中日韩（CJK）字符支持非常有限
   - 验证：`fc-list :lang=zh` 返回空

2. **Alpine 缺少 Noto CJK 字体包**
   - Alpine 的软件仓库中没有 Noto CJK 字体
   - 手动安装字体文件过于复杂

## 最终解决方案

### 切换到 Debian 基础镜像 + Noto CJK 字体

```dockerfile
# 使用 Debian 基础镜像（而非 Alpine）
FROM eclipse-temurin:11-jre

# 安装 Noto CJK 中文字体
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    fontconfig \
    fonts-noto-cjk \      # Google Noto CJK 字体
    fonts-dejavu \        # 英文字体
    && fc-cache -fv \     # 刷新字体缓存（verbose）
    && rm -rf /var/lib/apt/lists/*
```

### 为什么选择 Noto CJK？

**Noto Sans CJK** 是 Google 开发的开源字体，专门为中日韩文字设计：

✅ **完整的中文支持**
- 简体中文
- 繁体中文
- 日文
- 韩文

✅ **高质量渲染**
- 专业的字形设计
- 清晰的屏幕显示
- 完美的 PDF 嵌入

✅ **开源免费**
- Apache License 2.0
- 可商用

## 镜像大小对比

| 方案 | 基础镜像 | 字体 | 总大小 | 中文支持 |
|------|---------|------|--------|---------|
| 方案 1 | Alpine | ttf-dejavu | ~250MB | ❌ 不支持 |
| 方案 2 | Debian | Noto CJK | ~450MB | ✅ 完美支持 |

**增加约 200MB，但获得完美的中文支持，非常值得！**

## 验证字体安装

构建完成后，进入容器验证：

```bash
# 进入容器
docker exec -it tool-platform-backend bash

# 查看所有字体
fc-list

# 查看中文字体（应该看到 Noto Sans CJK）
fc-list :lang=zh

# 查看 Noto 字体
fc-list | grep -i noto
```

**预期输出示例：**
```
/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc: Noto Sans CJK SC:style=Regular
/usr/share/fonts/opentype/noto/NotoSansCJK-Bold.ttc: Noto Sans CJK SC:style=Bold
...
```

## 重新构建和测试

```bash
# 停止服务
docker compose down

# 重新构建（会下载 Debian 镜像和字体，需要几分钟）
docker compose up --build -d

# 查看构建日志
docker compose logs -f backend
```

## 测试步骤

1. **准备测试文档**
   - 创建包含中英文混合的 .docx 文档
   - 包含标题、正文、表格
   - 使用常见字体（如宋体、黑体、Arial）

2. **上传转换**
   - 通过前端上传文档
   - 等待转换完成

3. **验证结果**
   - 下载生成的 PDF
   - 检查中文是否正常显示
   - 检查格式是否保留

## 预期效果

✅ **中文正常显示**
- 不再出现 `####`
- 不再出现方块
- 字符清晰可读

✅ **格式保留**
- 字体样式（粗体、斜体）
- 段落对齐
- 表格结构
- 图片（如果有）

## 如果仍有问题

### 问题 1：部分中文仍显示为方块

**可能原因：**
Word 文档使用了特殊的中文字体（如方正字体、华文字体）

**解决方案：**
安装更多中文字体：

```dockerfile
RUN apt-get install -y --no-install-recommends \
    fontconfig \
    fonts-noto-cjk \
    fonts-wqy-microhei \    # 文泉驿微米黑
    fonts-wqy-zenhei \      # 文泉驿正黑
    fonts-arphic-ukai \     # AR PL UKai
    fonts-arphic-uming \    # AR PL UMing
    && fc-cache -fv \
    && rm -rf /var/lib/apt/lists/*
```

### 问题 2：镜像太大

**当前大小：** ~450MB

**如果需要减小：**
1. 只安装 Noto Sans CJK SC（简体中文）
2. 使用多阶段构建优化
3. 考虑使用云服务（如 AWS Lambda）

### 问题 3：构建时间长

**原因：**
- 下载 Debian 镜像
- 下载 Noto CJK 字体包（约 100MB）

**优化：**
- 使用 Docker 缓存
- 使用私有镜像仓库
- 预构建基础镜像

## 其他中文字体选项

### 文泉驿字体（开源）

```dockerfile
RUN apt-get install -y fonts-wqy-zenhei fonts-wqy-microhei
```

- 专为中文优化
- 体积较小
- 适合简体中文

### AR PL 字体（开源）

```dockerfile
RUN apt-get install -y fonts-arphic-ukai fonts-arphic-uming
```

- 包含楷体和明体
- 适合正式文档

## 性能影响

| 指标 | 影响 |
|------|------|
| **构建时间** | +2-3 分钟（首次） |
| **镜像大小** | +200MB |
| **运行时内存** | 无明显影响 |
| **转换速度** | 无影响 |
| **转换质量** | ⭐⭐⭐⭐⭐ 显著提升 |

## 总结

### ✅ 最终方案

- **基础镜像**：eclipse-temurin:11-jre (Debian)
- **字体**：Noto Sans CJK + DejaVu
- **镜像大小**：~450MB
- **中文支持**：完美

### 🎯 适用场景

- ✅ 需要处理中文文档
- ✅ 对转换质量要求高
- ✅ 服务器资源充足
- ✅ 可以接受较大的镜像

### ⚠️ 注意事项

- 镜像体积增加约 200MB
- 首次构建时间较长
- 需要足够的磁盘空间

**这是目前最可靠的中文 Word 转 PDF 解决方案！**
