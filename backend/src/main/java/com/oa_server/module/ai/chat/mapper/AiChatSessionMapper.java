package com.oa_server.module.ai.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oa_server.module.ai.chat.entity.AiChatSession;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 会话 Mapper
 *
 * @author Alu
 * @date 2026-09-14
 */
@Mapper
public interface AiChatSessionMapper extends BaseMapper<AiChatSession> {
}
