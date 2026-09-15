package com.oa_server.module.admin.kb.service;

import com.oa_server.module.admin.kb.vo.KbDocumentVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 知识库服务接口
 *
 * @author Alu
 * @date 2026-09-15
 */
public interface KbService {
    /**
     * 上传知识库文档:MinIO 存档 + 解析分块 + 向量化入 Qdrant
     *
     * @param file 文档文件对象
     * @return 文档VO
     */
    KbDocumentVO upload(MultipartFile file);
}
