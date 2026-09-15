package com.oa_server.module.emp.rag.service;

import com.oa_server.module.ai.chat.dto.ChatSendDTO;
import com.oa_server.module.ai.chat.dto.ChatStreamEvent;
import reactor.core.publisher.Flux;

/**
 * 员工 RAG 问答服务接口
 *
 * @author Alu
 * @date 2026-09-15
 */
public interface RagChatService {
    /**
     * 员工 RAG 问答服务
     *
     * @param chatSendDTO 问答请求参数
     * @return 问答响应流
     */
    Flux<ChatStreamEvent> chat(ChatSendDTO chatSendDTO);
}
