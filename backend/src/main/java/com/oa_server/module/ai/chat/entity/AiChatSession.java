package com.oa_server.module.ai.chat.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oa_server.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI 会话实体
 *
 * @author Alu
 * @date 2026-09-14
 */
@Data
@EqualsAndHashCode (callSuper = true) // 包含父类字段
@TableName("ai_chat_session")
public class AiChatSession extends BaseEntity {

    /**
     * 所属员工ID
     */
    private Long empId;

    /**
     * 会话类型（AGENT-管理员智能体/RAG-员工问答）
     */
    private String chatType;

    /**
     * 会话标题（取首条用户消息）
     */
    private String title;
}
