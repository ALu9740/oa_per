package com.oa_server.config;

import com.oa_server.module.admin.agent.tools.OaAdminTools;
import com.oa_server.module.emp.rag.prompt.RagPrompts;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
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

    /**
     * 员工 RAG 问答：注入检索 advisor，自动 检索->生成
     */
    @Bean
    public ChatClient ragChatClient(ChatClient.Builder builder, VectorStore vectorStore) {
        return builder
                .defaultSystem(RagPrompts.SYSTEM)
                .defaultAdvisors(QuestionAnswerAdvisor.builder(vectorStore)
                        .searchRequest(SearchRequest.builder()
                                .similarityThreshold(0.4)
                                .topK(4)
                                .build())
                        .promptTemplate(new PromptTemplate(RagPrompts.USER_TEMPLATE))
                        .build())
                .build();
    }

}
