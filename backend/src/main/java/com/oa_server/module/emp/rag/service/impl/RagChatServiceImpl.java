package com.oa_server.module.emp.rag.service.impl;

import cn.hutool.core.util.StrUtil;
import com.oa_server.common.result.ResultCode;
import com.oa_server.module.ai.chat.dto.ChatSendDTO;
import com.oa_server.module.ai.chat.dto.ChatStreamEvent;
import com.oa_server.module.ai.chat.entity.AiChatSession;
import com.oa_server.module.ai.chat.service.ChatSessionService;
import com.oa_server.module.emp.rag.service.RagChatService;
import com.oa_server.security.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 员工 RAG 问答服务实现
 *
 * @author Alu
 * @date 2026-09-15
 */
@Slf4j
@Service
public class RagChatServiceImpl implements RagChatService {

    private final ChatClient ragChatClient;
    private final ChatSessionService chatSessionService;

    public RagChatServiceImpl(@Qualifier("ragChatClient") ChatClient ragChatClient,
                              ChatSessionService chatSessionService) {
        this.ragChatClient = ragChatClient;
        this.chatSessionService = chatSessionService;
    }

    @Override
    public Flux<ChatStreamEvent> chat(ChatSendDTO dto) {
        //获取当前登录员工ID
        Long empId = SecurityUtils.getCurrentEmpId();
        //校验/创建会话
        AiChatSession aiChatSession = chatSessionService.ensureSession(
                empId, "RAG", dto.getSessionId(), dto.getContent());
        Long sessionId = aiChatSession.getId();
        //获取会话历史消息
        List<Message> historyMessages = chatSessionService.getHistoryMessages(sessionId, 10);
        chatSessionService.saveMessage(sessionId, "user", dto.getContent());
        // 用 AtomicReference 包裹，保证跨线程可见性（Reactor publishOn 可能切换线程）
        AtomicReference<StringBuilder> atomicReference = new AtomicReference<>(new StringBuilder());
        return ragChatClient.prompt() //创建 Prompt 构建器
                .messages(historyMessages) //注入历史消息
                .user(dto.getContent()) //设置本轮用户消息
                .stream() //开启流式模式
                .chatResponse() //获取完整响应对象流
                .concatMapIterable(response -> { //逐块转换事件
                    if (response.getResult() == null || response.getResult().getOutput() == null) { // Spring AI 流结束标记：result 为 null，跳过不处理
                        return List.<ChatStreamEvent>of();
                    }
                    String text = response.getResult().getOutput().getText();
                    if (StrUtil.isBlank(text)) { //空块直接返回空列表，不产生事件
                        return List.<ChatStreamEvent>of();
                    }
                    atomicReference.get().append(text); //累计文本
                    return List.of(ChatStreamEvent.delta(text)); //封装为 delta 事件推送前端渲染
                })
                .concatWith(Mono.fromCallable(() -> { //上游流正常结束后：持久化完整答复 + 推送 done 信号通知前端
                    chatSessionService.saveMessage(sessionId, "assistant", atomicReference.get().toString());
                    return ChatStreamEvent.done(String.valueOf(sessionId));
                }))
                .onErrorResume(ex -> { //上游流异常结束后：推送 error 信号通知前端
                    log.error("[AI-RAG] 流式问答异常 sessionId={}", sessionId, ex);
                    return Flux.just(ChatStreamEvent.error(ResultCode.AI_SERVICE_ERROR.getMessage()));
                });
    }
}
