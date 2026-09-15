package com.oa_server.module.ai.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * AI 对话发送 DTO
 *
 * @author Alu
 * @date 2026-09-15
 */
@Data
public class ChatSendDTO {
    /**
     * 会话ID，为空表示开启新会话
     */
    private Long sessionId;

    /**
     * 用户消息内容
     */
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 4000, message = "消息内容不能超过4000字")
    private String content;
}
