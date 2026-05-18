package com.gxa.service;

import com.gxa.pojo.entity.ChatMessage;
import com.gxa.pojo.entity.Chatsession;

import java.util.List;

public interface ChatSessionService {

    // 创建新会话
    String createSession(String userId);

    // 确保会话存在（如果不存在则创建）
    void ensureSessionExists(String sessionId, String userId);

    // 保存对话消息
    void saveMessage(String sessionId, String senderType, String content);

    // 获取会话历史
    List<ChatMessage> getHistory(String sessionId);

    // 获取用户的所有会话
    List<Chatsession> getUserSessions(String userId);

    void save(String type,String chatId,String userId);

    List<String> getChatIds(String type,String userId);

    void deleteSession(String sessionId, String userId);
}