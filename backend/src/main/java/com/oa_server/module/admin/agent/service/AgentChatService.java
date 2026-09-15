package com.oa_server.module.admin.agent.service;

import com.oa_server.module.ai.chat.dto.ChatSendDTO;
import com.oa_server.module.ai.chat.dto.ChatStreamEvent;
import reactor.core.publisher.Flux;

/**
 * 智能体对话服务
 *
 * @author Alu
 * @date 2026-09-15
 */
public interface AgentChatService {

    /**
     * 流式对话（SSE）
     *
     * @param chatSendDTO 对话发送 DTO
     * @return 流式事件 Flux
     */
    Flux<ChatStreamEvent> chat(ChatSendDTO chatSendDTO);
}
