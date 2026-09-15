package com.oa_server.module.ai.chat.service.impl;

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
import org.springframework.stereotype.Service;

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
}
