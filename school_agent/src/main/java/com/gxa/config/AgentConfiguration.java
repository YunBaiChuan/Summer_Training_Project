package com.gxa.config;

import com.gxa.ai.AITool;
import com.gxa.ai.SMSConstants;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zxd
 * @date 2026/1/16
 */
@Configuration
public class AgentConfiguration {

    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .maxMessages(80)
                .build();
    }

    @Bean
    public ChatClient chatClient(
            @Qualifier("deepSeekChatModel") ChatModel model,
            ChatMemory chatMemory,
            AITool aiTool,
            VectorStore vectorStore) {

        return ChatClient.builder(model)
                .defaultSystem(SMSConstants.CUSTOMER_SERVICE_SYSTEM) // 配置提示词
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(), // 配置日志
                        MessageChatMemoryAdvisor.builder(chatMemory).build(), // 配置会话记忆
                        QuestionAnswerAdvisor.builder(vectorStore) // 配置Rag的支持
                                .searchRequest(SearchRequest.builder()
                                        .similarityThreshold(0.3)
                                        .topK(3)
                                        .build())
                                .build()
                )
                .defaultTools(aiTool) // 配置工具，Spring AI会自动发现@Tool注解的方法
                .build();
    }
}