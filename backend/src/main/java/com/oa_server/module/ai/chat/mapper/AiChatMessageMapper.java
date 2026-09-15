package com.oa_server.module.ai.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oa_server.module.ai.chat.entity.AiChatMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 消息 Mapper
 *
 * @author Alu
 * @date 2026-09-15
 */
@Mapper
public interface AiChatMessageMapper extends BaseMapper<AiChatMessage> {
}
