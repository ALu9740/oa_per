package com.oa_server.module.ai.chat.service;

import com.oa_server.common.result.PageResult;
import com.oa_server.module.ai.chat.entity.AiChatSession;
import com.oa_server.module.ai.chat.vo.AiChatMessageVO;
import com.oa_server.module.ai.chat.vo.AiChatSessionVO;
import org.springframework.ai.chat.messages.Message;

import java.util.List;

/**
 * AI会话公共服务接口
 *
 * @author Alu
 * @date 2026-09-14
 */
public interface ChatSessionService {
    /**
     * 当前登录员工的会话分页列表
     *
     * @param chatType 会话类型（AGENT-管理员智能体/RAG-员工问答）
     * @param page 页码
     * @param size 每页数量
     * @return 会话分页列表
     */
    PageResult<AiChatSessionVO> listSessions(String chatType, Long page, Long size);

    /**
     * 会话消息列表
     *
     * @param sessionId 会话 ID
     * @return 会话消息列表
     */
    List<AiChatMessageVO> listMessages(Long sessionId);

    /**
     * 删除会话
     *
     * @param sessionId 会话 ID
     */
    void deleteSession(Long sessionId);

    /**
     * 校验/创建会话
     *
     * @param empId 员工 ID
     * @param chatType 会话类型（AGENT-管理员智能体/RAG-员工问答）
     * @param sessionId 会话 ID
     * @param firstMessage 会话第一条消息
     * @return 会话实体
     */
    AiChatSession ensureSession(Long empId, String chatType, Long sessionId, String firstMessage);

    /**
     * 加载最近 maxMessages 条消息
     *
     * @param sessionId 会话 ID
     * @param maxMessages 最大消息数量
     * @return 消息列表
     */
    List<Message> getHistoryMessages(Long sessionId, int maxMessages);

    /**
     * 保存消息
     *
     * @param sessionId 会话 ID
     * @param role 角色
     * @param content 消息内容
     */
    void saveMessage(Long sessionId, String role, String content);
}
