package com.oa_server.module.ai.chat.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 消息 VO
 *
 * @author Alu
 * @date 2026-09-14
 */
@Data
public class AiChatMessageVO {

    /**
     * 消息 ID
     */
    private Long id;

    /**
     * 消息角色
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
