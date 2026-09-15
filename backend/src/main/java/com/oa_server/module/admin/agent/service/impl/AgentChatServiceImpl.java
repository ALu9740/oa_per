package com.oa_server.module.admin.agent.service.impl;

import cn.hutool.core.util.StrUtil;
import com.oa_server.common.result.ResultCode;
import com.oa_server.module.admin.agent.prompt.AgentPrompts;
import com.oa_server.module.admin.agent.service.AgentChatService;
import com.oa_server.module.ai.chat.dto.ChatSendDTO;
import com.oa_server.module.ai.chat.dto.ChatStreamEvent;
import com.oa_server.module.ai.chat.entity.AiChatSession;
import com.oa_server.module.ai.chat.service.ChatSessionService;
import com.oa_server.security.LoginEmp;
import com.oa_server.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * 智能体对话服务实现
 *
 * @author Alu
 * @date 2026-09-15
 */
@Slf4j
@Service
public class AgentChatServiceImpl implements AgentChatService {

    private final ChatSessionService chatSessionService;
    private final ChatClient chatClient;

    public AgentChatServiceImpl(@Qualifier("agentChatClient") ChatClient agentChatClient,
                                ChatSessionService chatSessionService) {
        this.chatClient = agentChatClient;
        this.chatSessionService = chatSessionService;
    }

    @Override
    public Flux<ChatStreamEvent> chat(ChatSendDTO chatSendDTO) {
        //获取当前登录员工信息
        LoginEmp loginEmp = SecurityUtils.getCurrentEmp();
        //校验/创建会话
        AiChatSession aiChatSession = chatSessionService.ensureSession(loginEmp.getId(), "AGENT", chatSendDTO.getSessionId(), chatSendDTO.getContent());
        Long sessionId = aiChatSession.getId();
        //获取会话历史消息
        List<Message> historyMessages = chatSessionService.getHistoryMessages(sessionId, 10);
        chatSessionService.saveMessage(sessionId, "user", chatSendDTO.getContent());
        //流式调用，边输出边累计最终答复
        StringBuilder answer = new StringBuilder();
        //构建请求 → 发起流式调用 → 逐块转换事件 → 流结束后收尾 → 异常兜底
        return chatClient.prompt() //创建请求构造器
                .system(AgentPrompts.SYSTEM + "\n当前操作管理员ID:"+loginEmp.getId()) //设置系统提示词
                .messages(historyMessages) //注入历史消息
                .user(chatSendDTO.getContent()) //设置本轮用户消息
                .stream() //开启流式模式
                .chatResponse() //获取完整响应对象流
                .concatMapIterable(response -> { //逐块转换事件
                    if (response.getResult() == null) { // Spring AI 流式响应中部分 chunk 只有元数据（usage/model）没有 result，跳过处理
                        return List.of();
                    }
                    List<ChatStreamEvent> chatStreamEventList = new ArrayList<>(2); //初始化事件列表
                    AssistantMessage assistantMessage = response.getResult().getOutput(); //获取当前响应消息
                    //工具调用阶段：向前端推送"正在执行什么工具"
                    if (assistantMessage.getToolCalls() != null && !assistantMessage.getToolCalls().isEmpty()) {
                        for (AssistantMessage.ToolCall toolCall : assistantMessage.getToolCalls()) {
                            chatStreamEventList.add(ChatStreamEvent.tool(toolCall.name() + " " + toolCall.arguments()));
                        }
                    }
                    // 文本阶段：推送增量内容
                    String text = assistantMessage.getText();
                    if (StrUtil.isNotBlank(text)) {
                        answer.append(text);
                        chatStreamEventList.add(ChatStreamEvent.delta(text));
                    }
                    return chatStreamEventList;
                })
                .concatWith(Mono.fromCallable(() -> { //上游流正常结束后：持久化完整答复 + 推送 done 信号通知前端
                    chatSessionService.saveMessage(sessionId, "assistant", answer.toString());
                    return ChatStreamEvent.done(String.valueOf(sessionId));
                }))
                .onErrorResume(ex -> { //上游流异常结束后：推送 error 信号通知前端
                    log.error("[AI-Agent] 流式对话异常 sessionId={}", sessionId, ex);
                    return Flux.just(ChatStreamEvent.error(ResultCode.AI_SERVICE_ERROR.getMessage()));
                });
    }
}