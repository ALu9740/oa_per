package com.oa_server.module.ai.chat.service;

import com.oa_server.common.result.PageResult;
import com.oa_server.module.ai.chat.vo.AiChatSessionVO;

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
}
