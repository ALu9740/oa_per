package com.oa_server.module.ai.chat.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oa_server.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI 消息实体
 *
 * @author Alu
 * @date 2026-09-15
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_chat_message")
public class AiChatMessage extends BaseEntity {

    /**
     * 所属会话ID
     */
    private Long sessionId;

    /**
     * 角色（user-用户/assistant-AI）
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;
}
