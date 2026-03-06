# Word 转 PDF 功能改进说明

## 问题描述

原始的 Word 转 PDF 功能只提取纯文本，**不保留任何格式**，导致转换后的 PDF 内容不正确。

### 原始实现的问题

```java
// 旧代码：只提取纯文本
for (XWPFParagraph paragraph : document.getParagraphs()) {
    String text = paragraph.getText();
    pdfDocument.add(new Paragraph(text));
}
```

**缺陷：**
- ❌ 不保留字体样式（粗体、斜体）
- ❌ 不保留字号
- ❌ 不保留对齐方式
- ❌ 不支持表格
- ❌ 不支持列表
- ❌ 所有内容都是默认格式

## 改进内容

### 1. 依赖库优化

**修改前：**
```xml
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>itext7-core</artifactId>
    <version>7.2.5</version>
    <type>pom</type>  <!-- 这种方式不会引入实际的 jar -->
</dependency>
```

**修改后：**
```xml
<!-- 明确引入需要的模块 -->
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>kernel</artifactId>
    <version>7.2.5</version>
</dependency>
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>layout</artifactId>
    <version>7.2.5</version>
</dependency>
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>io</artifactId>
    <version>7.2.5</version>
</dependency>
```

### 2. 转换功能增强

#### ✅ 支持的格式

| 格式元素 | 支持情况 | 说明 |
|---------|---------|------|
| **段落文本** | ✅ 完全支持 | 保留原始文本内容 |
| **字体大小** | ✅ 完全支持 | 保留原始字号 |
| **粗体** | ✅ 完全支持 | 保留粗体样式 |
| **斜体** | ✅ 完全支持 | 保留斜体样式 |
| **对齐方式** | ✅ 完全支持 | 左对齐、居中、右对齐、两端对齐 |
| **表格** | ✅ 完全支持 | 保留表格结构和内容 |
| **页面边距** | ✅ 已设置 | 统一设置为 50pt |
| **字体颜色** | ⚠️ 部分支持 | 需要进一步扩展 |
| **图片** | ❌ 暂不支持 | 未来可以添加 |
| **列表** | ❌ 暂不支持 | 未来可以添加 |

#### 新增方法

1. **`createParagraphWithFormatting()`**
   - 从 Word 段落提取格式信息
   - 应用字号、粗体、斜体、对齐方式到 PDF

2. **`convertTable()`**
   - 将 Word 表格转换为 PDF 表格
   - 保留表格结构和单元格内容

### 3. 代码改进细节

```java
// 设置页面边距
pdfDocument.setMargins(50, 50, 50, 50);

// 处理段落格式
private Paragraph createParagraphWithFormatting(XWPFParagraph paragraph) {
    // 1. 提取字体大小
    int fontSize = run.getFontSize();
    if (fontSize > 0) {
        pdfParagraph.setFontSize(fontSize);
    }
    
    // 2. 应用粗体和斜体
    if (run.isBold()) pdfParagraph.setBold();
    if (run.isItalic()) pdfParagraph.setItalic();
    
    // 3. 设置对齐方式
    switch (paragraph.getAlignment()) {
        case CENTER: // 居中
        case RIGHT:  // 右对齐
        case BOTH:   // 两端对齐
        // ...
    }
}

// 处理表格
private Table convertTable(XWPFTable wordTable) {
    // 创建 PDF 表格
    Table pdfTable = new Table(numCols);
    pdfTable.setWidth(UnitValue.createPercentValue(100));
    
    // 转换每个单元格
    for (XWPFTableRow row : wordTable.getRows()) {
        for (XWPFTableCell cell : row.getTableCells()) {
            Cell pdfCell = new Cell().add(new Paragraph(cellText));
            pdfTable.addCell(pdfCell);
        }
    }
}
```

## 应用更新

需要重新构建后端服务以应用这些更改：

```bash
# 停止服务
docker compose down

# 重新构建并启动
docker compose up --build -d

# 查看日志
docker compose logs -f backend
```

## 测试建议

创建一个包含以下内容的 Word 文档进行测试：

1. **标题**（大字号、粗体）
2. **正文段落**（不同对齐方式）
3. **表格**（多行多列）
4. **格式化文本**（粗体、斜体混合）

## 已知限制

### 当前不支持的功能

1. **图片**：Word 中的图片不会被转换
2. **列表**：有序/无序列表会被转换为普通段落
3. **页眉页脚**：不会被转换
4. **分栏**：多栏布局会变成单栏
5. **复杂样式**：如文本框、艺术字等

### 为什么有这些限制？

- **技术复杂度**：完整的 Word 转 PDF 需要处理非常复杂的格式
- **库的限制**：Apache POI + iText 的组合有一定局限性
- **性能考虑**：过于复杂的转换会影响性能

### 更好的替代方案

如果需要完美的 Word 转 PDF，可以考虑：

1. **LibreOffice/OpenOffice**（通过命令行调用）
   ```bash
   soffice --headless --convert-to pdf document.docx
   ```

2. **商业库**：如 Aspose.Words（需要付费）

3. **云服务**：如 Microsoft Graph API、Google Docs API

## 未来改进方向

### 短期改进（相对容易）

- [ ] 支持字体颜色
- [ ] 支持下划线、删除线
- [ ] 支持有序/无序列表
- [ ] 支持超链接
- [ ] 改进表格样式（边框、背景色）

### 中期改进（需要更多工作）

- [ ] 支持图片转换
- [ ] 支持页眉页脚
- [ ] 支持分页符
- [ ] 支持文本框

### 长期改进（复杂）

- [ ] 集成 LibreOffice 进行转换
- [ ] 支持复杂的样式和布局
- [ ] 支持嵌入字体

## 性能优化建议

1. **异步处理**：对于大文件，考虑异步转换
2. **缓存结果**：相同文件可以缓存转换结果
3. **文件清理**：定期清理临时文件
4. **资源限制**：限制同时转换的文件数量

## 总结

当前的改进版本能够：
- ✅ 保留基本的文本格式（字号、粗体、斜体、对齐）
- ✅ 支持表格转换
- ✅ 提供更好的用户体验

虽然不是完美的转换，但对于大多数简单到中等复杂度的 Word 文档已经足够使用。
