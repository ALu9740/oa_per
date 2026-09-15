package com.oa_server.module.admin.agent.controller;

import com.oa_server.module.admin.agent.service.AgentChatService;
import com.oa_server.module.ai.chat.dto.ChatSendDTO;
import com.oa_server.module.ai.chat.dto.ChatStreamEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * 智能体对话接口
 *
 * @author Alu
 * @date 2026-09-15
 */
@RestController
@RequestMapping("/api/ai/agent")
@RequiredArgsConstructor
public class AgentChatController {
    private final AgentChatService agentChatService;

    /**
     * 对话接口
     */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatStreamEvent> chat(@Validated @RequestBody ChatSendDTO chatSendDTO) {
        return agentChatService.chat(chatSendDTO);
    }
}
