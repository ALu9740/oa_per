package com.oa_server.module.ai.chat.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa_server.common.exception.BusinessException;
import com.oa_server.common.result.PageResult;
import com.oa_server.common.result.ResultCode;
import com.oa_server.module.ai.chat.entity.AiChatMessage;
import com.oa_server.module.ai.chat.entity.AiChatSession;
import com.oa_server.module.ai.chat.mapper.AiChatMessageMapper;
import com.oa_server.module.ai.chat.mapper.AiChatSessionMapper;
import com.oa_server.module.ai.chat.service.ChatSessionService;
import com.oa_server.module.ai.chat.vo.AiChatMessageVO;
import com.oa_server.module.ai.chat.vo.AiChatSessionVO;
import com.oa_server.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * AI 会话公共服务实现
 *
 * @author Alu
 * @date 2026-09-14
 */
@Service
@RequiredArgsConstructor
public class ChatSessionServiceImpl implements ChatSessionService {

    private static final List<String> VALID_TYPES = List.of("AGENT", "RAG");
    private static final Logger log = LoggerFactory.getLogger(ChatSessionServiceImpl.class);

    private final AiChatSessionMapper aiChatSessionMapper;
    private final AiChatMessageMapper aiChatMessageMapper;

    @Override
    public PageResult<AiChatSessionVO> listSessions(String chatType, Long page, Long size) {
        //获取当前登录员工ID
        Long empId = SecurityUtils.getCurrentEmpId();
        //校验会话类型是否合法
        if (!VALID_TYPES.contains(chatType)) {
            throw new BusinessException(ResultCode.PARAM_INVALID,ResultCode.INVALID_CHAT_TYPE.getMessage());
        }

        //构造分页对象
        Page<AiChatSession> aiChatSessionVOPage = new Page<>(
                page == null ? 1L : page,
                size == null ? 10L : Math.min(size,100L)
        );

        //查询会话
        Page<AiChatSession> selectedPage = aiChatSessionMapper.selectPage(aiChatSessionVOPage,
                new LambdaQueryWrapper<AiChatSession>()
                        .eq(AiChatSession::getEmpId, empId)
                        .eq(AiChatSession::getChatType, chatType)
                        .orderByDesc(AiChatSession::getUpdatedAt));
        //转换为VO
        List<AiChatSessionVO> aiChatSessionVOList = selectedPage.getRecords().stream().map(s->{
            AiChatSessionVO aiChatSessionVO = new AiChatSessionVO();
            aiChatSessionVO.setId(s.getId());
            aiChatSessionVO.setChatType(s.getChatType());
            aiChatSessionVO.setTitle(s.getTitle());
            aiChatSessionVO.setCreatedAt(s.getCreatedAt());
            aiChatSessionVO.setUpdatedAt(s.getUpdatedAt());
            return aiChatSessionVO;
        }).toList();
        // 返回分页结果
        return PageResult.of(selectedPage.getTotal(), selectedPage.getCurrent(), selectedPage.getSize(), aiChatSessionVOList);
    }

    @Override
    public List<AiChatMessageVO> listMessages(Long sessionId) {
        //获取当前登录员工ID
        Long empId = SecurityUtils.getCurrentEmpId();
        //校验会话是否存在
        AiChatSession aiChatSession = aiChatSessionMapper.selectById(sessionId);
        if (aiChatSession == null || !Objects.equals(aiChatSession.getEmpId(), empId)) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        //查询会话消息列表
        List<AiChatMessage> aiChatMessageList = aiChatMessageMapper.selectList(new LambdaQueryWrapper<AiChatMessage>()
                .eq(AiChatMessage::getSessionId, sessionId)
                .orderByAsc(AiChatMessage::getCreatedAt));
        //转换为VO
        return aiChatMessageList.stream().map(aiChatMessage -> {
            AiChatMessageVO aiChatMessageVO = new AiChatMessageVO();
            aiChatMessageVO.setId(aiChatMessage.getId());
            aiChatMessageVO.setRole(aiChatMessage.getRole());
            aiChatMessageVO.setContent(aiChatMessage.getContent());
            aiChatMessageVO.setCreatedAt(aiChatMessage.getCreatedAt());
            return aiChatMessageVO;
        }).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSession(Long sessionId) {
        //获取当前登录员工ID
        Long empId = SecurityUtils.getCurrentEmpId();
        AiChatSession aiChatSession = aiChatSessionMapper.selectById(sessionId);
        if (aiChatSession == null || !Objects.equals(aiChatSession.getEmpId(), empId)) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        aiChatSessionMapper.deleteById(sessionId);
        log.info("[AI] 删除会话：sessionId={}, empId={}", sessionId, empId);
    }

    @Override
    public AiChatSession ensureSession(Long empId, String chatType, Long sessionId, String firstMessage) {
        //校验会话类型是否合法
        if (!VALID_TYPES.contains(chatType)) {
            throw new BusinessException(ResultCode.PARAM_INVALID, ResultCode.INVALID_CHAT_TYPE.getMessage());
        }
        //校验会话是否存在
        if (sessionId != null) {
            AiChatSession aiChatSession = aiChatSessionMapper.selectById(sessionId);
            if (aiChatSession == null || !Objects.equals(aiChatSession.getEmpId(), empId)) {
                throw new BusinessException(ResultCode.NOT_FOUND);
            }
            if (!Objects.equals(chatType, aiChatSession.getChatType())) {
                throw new BusinessException(ResultCode.PARAM_INVALID, ResultCode.INVALID_CHAT_SESSION_TYPE.getMessage());
            }
            return aiChatSession;
        }
        //创建新会话
        AiChatSession aiChatSession = new AiChatSession();
        aiChatSession.setEmpId(empId);
        aiChatSession.setChatType(chatType);
        aiChatSession.setTitle(StrUtil.isBlank(firstMessage) ? "新会话" : StrUtil.sub(firstMessage, 0, 20));
        aiChatSessionMapper.insert(aiChatSession);
        return aiChatSession;
    }

    @Override
    public List<Message> getHistoryMessages(Long sessionId, int maxMessages) {
        //id 倒序取最近 maxMessages 条消息
        List<AiChatMessage> aiChatMessageList = aiChatMessageMapper.selectList(
                new LambdaQueryWrapper<AiChatMessage>()
                        .eq(AiChatMessage::getSessionId,sessionId)
                        .orderByDesc(AiChatMessage::getId)
                        .last("LIMIT " + maxMessages)
        );
        //倒序
        Collections.reverse(aiChatMessageList);

        //转换为Message列表
        List<Message> messageList = new ArrayList<>(aiChatMessageList.size());
        for (AiChatMessage aiChatMessage : aiChatMessageList) {
            if (Objects.equals(aiChatMessage.getRole(), "user")) {
                messageList.add(new UserMessage(aiChatMessage.getContent()));
            } else {
                messageList.add(new AssistantMessage(aiChatMessage.getContent()));
            }
        }
        return messageList;
    }

    @Override
    public void saveMessage(Long sessionId, String role, String content) {
        AiChatMessage message = new AiChatMessage();
        message.setSessionId(sessionId);
        message.setRole(role);
        message.setContent(content);
        aiChatMessageMapper.insert(message);
    }
}
