package com.oa_server.module.emp.rag.controller;

import com.oa_server.module.ai.chat.dto.ChatSendDTO;
import com.oa_server.module.ai.chat.dto.ChatStreamEvent;
import com.oa_server.module.emp.rag.service.RagChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * 员工制度问答 接口
 *
 * @author Alu
 * @date 2026-09-15
 */
@RestController
@RequestMapping("/api/ai/rag")
@RequiredArgsConstructor
public class RagChatController {

    private final RagChatService ragChatService;

    /**
     * 员工对话接口
     */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatStreamEvent> chat(@Validated @RequestBody ChatSendDTO chatSendDTO) {
        return ragChatService.chat(chatSendDTO);
    }
}
