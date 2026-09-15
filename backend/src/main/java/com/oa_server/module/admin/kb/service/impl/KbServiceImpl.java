package com.oa_server.module.admin.kb.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa_server.common.exception.BusinessException;
import com.oa_server.common.result.PageResult;
import com.oa_server.common.result.ResultCode;
import com.oa_server.module.admin.kb.entity.KbDocument;
import com.oa_server.module.admin.kb.enums.KbDocumentStatusEnum;
import com.oa_server.module.admin.kb.mapper.KbDocumentMapper;
import com.oa_server.module.admin.kb.service.KbService;
import com.oa_server.module.admin.kb.vo.KbDocumentVO;
import com.oa_server.module.file.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.tika.Tika;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 知识库服务实现
 *
 * @author Alu
 * @date 2026-09-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KbServiceImpl implements KbService {

    /**
     * 分块目标大小（字符数），制度类文档 600 字左右召回效果较好
     */
    private static final int CHUNK_SIZE = 600;

    private static final long MAX_FILE_SIZE = 20 * 1024 * 1024;

    private static final List<String> ALLOWED_EXTS = List.of("pdf", "doc", "docx", "txt", "md");

    private final KbDocumentMapper kbDocumentMapper;
    private final FileStorageService fileStorageService;
    private final VectorStore vectorStore;

    @Override
    public KbDocumentVO upload(MultipartFile file) {
        //校验文件是否为空
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_INVALID, ResultCode.FILE_EMPTY.getMessage());
        }
        String fileName = file.getOriginalFilename();
        String ext = FileUtil.extName(fileName);
        //校验文件格式是否支持
        if (ext == null || !ALLOWED_EXTS.contains(ext.toLowerCase())) {
            throw new BusinessException(ResultCode.PARAM_INVALID, ResultCode.FILE_FORMAT_INVALID.getMessage());
        }
        //校验文件大小是否超过最大限制
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ResultCode.PARAM_INVALID, ResultCode.FILE_SIZE_EXCEEDED.getMessage());
        }
        //读取文件内容
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (Exception e) {
            throw new BusinessException(ResultCode.FILE_READ_FAILED);
        }

        // 原文件存档到 MinIO
        String objectName = IdWorker.getId() + "." + ext;
        fileStorageService.uploadMinIO(bytes, "kb", objectName, file.getContentType());

        //落库记录（PROCESSING）
        KbDocument doc = new KbDocument();
        doc.setFileName(fileName);
        doc.setMinioPath("kb/" + objectName);
        doc.setStatus(KbDocumentStatusEnum.PROCESSING.getStatus());
        kbDocumentMapper.insert(doc);

        //解析 -> 分块 -> 向量化入 Qdrant
        try {
            String text = extractText(bytes);
            if (StrUtil.isBlank(text)) {
                throw new BusinessException(ResultCode.PARAM_INVALID,
                        ResultCode.FILE_PARSE_FAILED.getMessage());
            }
            List<Document> chunks = toDocuments(text, doc.getId(), fileName);
            // 幂等：先清掉该文档旧分块，再写入（支持同名/失败后重传）
            vectorStore.delete(new FilterExpressionBuilder()
                    .eq("docId", String.valueOf(doc.getId())).build());
            vectorStore.add(chunks);

            doc.setChunkCount(chunks.size());
            doc.setStatus(KbDocumentStatusEnum.READY.getStatus());
            log.info("[AI-KB] 文档入库成功：fileName={}, chunks={}", fileName, chunks.size());
        } catch (Exception e) {
            log.error("[AI-KB] 文档入库失败：fileName={}", fileName, e);
            doc.setStatus(KbDocumentStatusEnum.FAILED.getStatus());
            doc.setErrorMsg(StrUtil.sub(e.getMessage(), 0, 250));
        }
        kbDocumentMapper.updateById(doc);
        return toVO(doc);
    }

    @Override
    public PageResult<KbDocumentVO> list(String fileName, Long page, Long size) {
        // 分页查询
        Page<KbDocument> kbDocumentPage = new Page<>(
                page == null ? 1L : page,
                size == null ? 10L : Math.min(size,100L));
        // 分页查询知识库文档
        Page<KbDocument> documentPage = kbDocumentMapper.selectPage(kbDocumentPage,
                new LambdaQueryWrapper<KbDocument>()
                        .like(StrUtil.isNotBlank(fileName), KbDocument::getFileName, fileName)
                        .orderByDesc(KbDocument::getId));
        // 转换为VO列表
        List<KbDocumentVO> kbDocumentVOList = documentPage.getRecords().stream().map(this::toVO).toList();
        // 返回分页结果集
        return PageResult.of(documentPage.getTotal(), documentPage.getCurrent(), documentPage.getSize(), kbDocumentVOList);
    }

    /**
     * Tika 提取纯文本
     */
    private String extractText(byte[] bytes) {
        try {
            Tika tika = new Tika();
            tika.setMaxStringLength((int) MAX_FILE_SIZE);
            return tika.parseToString(new ByteArrayInputStream(bytes));
        } catch (Exception e) {
            throw new RuntimeException("文档解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 文本分块 + 挂元数据
     */
    private List<Document> toDocuments(String text, Long docId, String fileName) {
        List<Document> documents = new ArrayList<>();
        for (String chunk : splitChunks(text, CHUNK_SIZE)) {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("docId", String.valueOf(docId));
            metadata.put("fileName", fileName);
            documents.add(new Document(chunk, metadata));
        }
        return documents;
    }

    /**
     * 简单分块策略：优先按空行分段落聚合，聚合到目标大小即成块；超长段落硬切
     */
    private List<String> splitChunks(String text, int chunkSize) {
        List<String> chunks = new ArrayList<>();
        StringBuilder buffer = new StringBuilder();
        for (String paragraph : text.split("\\n\\s*\\n")) {
            String p = paragraph.strip();
            if (p.isEmpty()) {
                continue;
            }
            while (p.length() > chunkSize) {
                chunks.add(p.substring(0, chunkSize));
                p = p.substring(chunkSize);
            }
            if (buffer.length() + p.length() + 1 > chunkSize && buffer.length() > 0) {
                chunks.add(buffer.toString());
                buffer.setLength(0);
            }
            buffer.append(p).append('\n');
        }
        if (buffer.length() > 0) {
            chunks.add(buffer.toString());
        }
        return chunks;
    }

    private KbDocumentVO toVO(KbDocument doc) {
        KbDocumentVO vo = new KbDocumentVO();
        vo.setId(doc.getId());
        vo.setFileName(doc.getFileName());
        vo.setChunkCount(doc.getChunkCount());
        vo.setStatus(doc.getStatus());
        vo.setCreatedAt(doc.getCreatedAt());
        return vo;
    }
}
