package com.gxa.controller;

import com.gxa.service.ChatSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ai")
@Tag(name = "智能体接口")
public class ChatMessageController {

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private ChatSessionService chatSessionService;

    @GetMapping("/chat/v1")
    @Operation(summary = "智能体聊天接口-普通调用")
    public String chat(
            @RequestParam String msg,
            @RequestParam String sessionId,
            @RequestParam String userId) {

        // 1. 确保会话存在（调用正确的方法）
        chatSessionService.ensureSessionExists(sessionId, userId);

        // 2. 保存用户消息到数据库（历史记录）
        chatSessionService.saveMessage(sessionId, "user", msg);

        // 3. 请求模型
        String response = chatClient.prompt()
                .user(msg)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, sessionId))
                .call()
                .content();

        // 4. 保存AI回复到数据库（历史记录）
        chatSessionService.saveMessage(sessionId, "assistant", response);

        return response;
    }

    @GetMapping("/chat/v2")
    @Operation(summary = "智能体聊天接口-流式调用")
    public Flux<String> chat2(
            @RequestParam String msg,
            @RequestParam String sessionId,
            @RequestParam String userId) {

        // 1. 确保会话存在（调用正确的方法）
        chatSessionService.ensureSessionExists(sessionId, userId);

        // 2. 保存用户消息到数据库（历史记录）
        chatSessionService.saveMessage(sessionId, "user", msg);

        // 3. 创建变量用于收集完整响应
        final StringBuilder fullResponse = new StringBuilder();

        // 4. 请求模型（流式）
        Flux<String> stream = chatClient.prompt()
                .user(msg)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, sessionId))
                .stream()
                .content()
                .doOnNext(chunk -> {
                    // 收集每个片段
                    fullResponse.append(chunk);
                })
                .doOnComplete(() -> {
                    // 流结束后保存完整回复（历史记录）
                    if (fullResponse.length() > 0) {
                        chatSessionService.saveMessage(sessionId, "assistant", fullResponse.toString());
                    }
                });

        return stream;
    }
}