package com.oa_server.module.admin.kb.service;

import com.oa_server.common.result.PageResult;
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

    /**
     * 分页查询知识库文档
     *
     * @param fileName 文档名称
     * @param page 分页码
     * @param size 分页大小
     * @return 分页结果集
     */
    PageResult<KbDocumentVO> list(String fileName, Long page, Long size);

    /**
     * 删除文档 （含 Qdrant 中的分块；MinIO 原文件保留）
     *
     * @param id 文档ID
     */
    void delete(Long id);
}
