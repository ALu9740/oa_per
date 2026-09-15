package com.oa_server.config;

import com.oa_server.module.admin.agent.tools.OaAdminTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI ChatClient 装配
 *
 * @author Alu
 * @date 2026-09-15
 */
@Configuration
public class AiChatClientConfig {
    /**
     * 管理员智能体：注入工具集，框架自动完成 ReAct 循环
     */
    @Bean
    public ChatClient agentChatClient(ChatClient.Builder builder, OaAdminTools oaAdminTools) {
        return builder
                .defaultTools(oaAdminTools)
                .build();
    }


}
