package com.gxa.controller;

import com.gxa.service.ChatSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai/history")
@Tag(name = "会话记录接口")
public class ChatSessionController {

    @Autowired
    private ChatSessionService chatSessionService;

    @GetMapping("/sessions")
    @Operation(summary = "获取用户的所有会话")
    public List<?> getUserSessions(@RequestParam(defaultValue = "1") String userId) {
        return chatSessionService.getUserSessions(userId);
    }

    @GetMapping("/chat/history")
    @Operation(summary = "获取会话历史")
    public Map<String, Object> getHistory(
            @RequestParam String sessionId) {

        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", sessionId);
        result.put("messages", chatSessionService.getHistory(sessionId));

        return result;
    }
    @DeleteMapping("/sessions/{sessionId}")
    @Operation(summary = "删除会话")
    public Map<String, Object> deleteSession(
            @PathVariable String sessionId,
            @RequestParam String userId) {

        Map<String, Object> result = new HashMap<>();

        try {
            chatSessionService.deleteSession(sessionId, userId);
            result.put("success", true);
            result.put("message", "会话删除成功");
            result.put("sessionId", sessionId);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除失败: " + e.getMessage());
            result.put("sessionId", sessionId);
        }

        return result;
    }
}