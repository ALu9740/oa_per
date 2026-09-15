package com.oa_server.module.admin.kb.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Alu
 * @date 2026-09-15
 */
@Getter
@AllArgsConstructor
public enum KbDocumentStatusEnum {
    /**
     * 状态（PROCESSING-处理中/READY-已入库/FAILED-失败）
     */
    PROCESSING("PROCESSING"),
    READY("READY"),
    FAILED("FAILED");
    private final String status;
}
