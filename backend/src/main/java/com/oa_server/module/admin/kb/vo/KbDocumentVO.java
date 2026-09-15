package com.oa_server.module.admin.kb.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识库文档 VO
 *
 * @author Alu
 * @date 2026-09-15
 */
@Data
public class KbDocumentVO {

    /**
     * 文档ID
     */
    private Long id;

    /**
     * 文档名称
     */
    private String fileName;

    /**
     * 切分后的分块数量
     */
    private Integer chunkCount;

    /**
     * 状态（PROCESSING-处理中/READY-已入库/FAILED-失败）
     */
    private String status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

}
