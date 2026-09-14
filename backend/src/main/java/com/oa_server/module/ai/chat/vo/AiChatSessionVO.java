package com.oa_server.module.ai.chat.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 会话 VO
 *
 * @author Alu
 * @date 2026-09-14
 */
@Data
public class AiChatSessionVO {

    /**
     * 会话ID
     */
    private Long id;

    /**
     * 会话类型（AGENT-管理员智能体/RAG-员工问答）
     */
    private String chatType;

    /**
     * 会话标题（取首条用户消息）
     */
    private String title;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}