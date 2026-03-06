package com.toolplatform.service;

import com.toolplatform.exception.BusinessException;
import org.docx4j.Docx4J;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class WordToPdfService {
    private static final Logger logger = LoggerFactory.getLogger(WordToPdfService.class);
    private static final String[] CJK_FONT_CANDIDATES = {
            "Noto Sans CJK SC",
            "Noto Serif CJK SC",
            "WenQuanYi Zen Hei",
            "WenQuanYi Micro Hei",
            "AR PL UMing CN",
            "AR PL UKai CN"
    };
    private static final String[] SANS_FONT_CANDIDATES = {
            "DejaVu Sans",
            "Liberation Sans",
            "Arial"
    };
    private static final String[] SERIF_FONT_CANDIDATES = {
            "DejaVu Serif",
            "Liberation Serif",
            "Times New Roman"
    };

    @Value("${file.upload.temp-dir}")
    private String tempDir;

    @PostConstruct
    public void init() {
        File dir = new File(tempDir);
        if (!dir.exists()) {
            dir.mkdirs();
            logger.info("Created temp directory: {}", tempDir);
        }
    }

    public String convertToPdf(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.endsWith(".docx")) {
            throw new BusinessException("只支持 .docx 格式的Word文档");
        }

        String fileId = UUID.randomUUID().toString();
        Path wordPath = Paths.get(tempDir, fileId + "_" + originalFilename);
        String pdfFilename = fileId + ".pdf";
        Path pdfPath = Paths.get(tempDir, pdfFilename);

        try {
            // 保存上传的Word文件
            file.transferTo(wordPath.toFile());
            logger.info("Word file saved: {}", wordPath);

            // 使用 docx4j 转换为PDF
            convertWordToPdfWithDocx4j(wordPath.toFile(), pdfPath.toFile());
            logger.info("PDF file created: {}", pdfPath);

            // 清理Word文件
            Files.deleteIfExists(wordPath);

            return pdfFilename;
        } catch (Exception e) {
            logger.error("Failed to convert Word to PDF", e);
            // 清理临时文件
            try {
                Files.deleteIfExists(wordPath);
                Files.deleteIfExists(pdfPath);
            } catch (IOException cleanupException) {
                logger.warn("Failed to cleanup temporary files", cleanupException);
            }
            throw new BusinessException("文件转换失败: " + e.getMessage());
        }
    }

    private void convertWordToPdfWithDocx4j(File wordFile, File pdfFile) throws Exception {
        logger.info("Starting Word to PDF conversion using docx4j");

        // 加载 Word 文档
        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(wordFile);

        configureFontMapper(wordMLPackage);

        // 转换为 PDF
        try (OutputStream os = new FileOutputStream(pdfFile)) {
            Docx4J.toPDF(wordMLPackage, os);
        }

        logger.info("Conversion completed successfully. PDF size: {} bytes", pdfFile.length());
    }

    private void configureFontMapper(WordprocessingMLPackage wordMLPackage) throws Exception {
        org.docx4j.fonts.Mapper fontMapper = new org.docx4j.fonts.IdentityPlusMapper();
        wordMLPackage.setFontMapper(fontMapper);

        org.docx4j.fonts.PhysicalFont cjkFont = resolveFirstAvailableFont(CJK_FONT_CANDIDATES);
        org.docx4j.fonts.PhysicalFont sansFont = resolveFirstAvailableFont(SANS_FONT_CANDIDATES);
        org.docx4j.fonts.PhysicalFont serifFont = resolveFirstAvailableFont(SERIF_FONT_CANDIDATES);

        bindAliases(fontMapper, cjkFont,
                "宋体", "SimSun",
                "黑体", "SimHei",
                "微软雅黑", "Microsoft YaHei",
                "仿宋", "FangSong",
                "楷体", "KaiTi",
                "等线", "DengXian",
                "华文宋体", "STSong",
                "华文黑体", "STHeiti");

        bindAliases(fontMapper, sansFont,
                "Arial", "Calibri", "Helvetica");
        bindAliases(fontMapper, serifFont,
                "Times New Roman", "Cambria", "Georgia");

        if (cjkFont == null) {
            logger.warn("No CJK font detected. Chinese characters may render as garbled text.");
        }
        logger.info("Font mapper configured. cjk={}, sans={}, serif={}",
                cjkFont != null ? cjkFont.getName() : "N/A",
                sansFont != null ? sansFont.getName() : "N/A",
                serifFont != null ? serifFont.getName() : "N/A");
    }

    private org.docx4j.fonts.PhysicalFont resolveFirstAvailableFont(String... candidates) {
        for (String candidate : candidates) {
            org.docx4j.fonts.PhysicalFont font = org.docx4j.fonts.PhysicalFonts.get(candidate);
            if (font != null) {
                return font;
            }
        }
        return null;
    }

    private void bindAliases(org.docx4j.fonts.Mapper fontMapper,
                             org.docx4j.fonts.PhysicalFont targetFont,
                             String... aliases) {
        if (targetFont == null) {
            return;
        }
        for (String alias : aliases) {
            fontMapper.put(alias, targetFont);
        }
    }

    public File getFile(String filename) {
        File file = new File(tempDir, filename);
        if (!file.exists()) {
            throw new BusinessException("文件不存在或已过期");
        }
        return file;
    }

    public void deleteFile(String filename) {
        try {
            Path path = Paths.get(tempDir, filename);
            Files.deleteIfExists(path);
            logger.info("Deleted file: {}", filename);
        } catch (IOException e) {
            logger.warn("Failed to delete file: {}", filename, e);
        }
    }
}
