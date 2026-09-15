package com.oa_server.module.admin.kb.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oa_server.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 知识库文档实体
 *
 * @author Alu
 * @date 2026-09-15
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kb_document")
public class KbDocument extends BaseEntity {

    /**
     * 原始文件名
     */
    private String fileName;

    /**
     * MinIO 对象路径
     */
    private String minioPath;

    /**
     * 切分后的分块数量
     */
    private Integer chunkCount;

    /**
     * 状态（PROCESSING-处理中/READY-已入库/FAILED-失败）
     */
    private String status;

    /**
     * 失败原因
     */
    private String errorMsg;
}
