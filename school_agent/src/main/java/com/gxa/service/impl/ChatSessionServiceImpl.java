package com.gxa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gxa.mapper.ChatMessageMapper;
import com.gxa.mapper.ChatSessionMapper;
import com.gxa.pojo.entity.ChatMessage;
import com.gxa.pojo.entity.Chatsession;
import com.gxa.service.ChatSessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
public class ChatSessionServiceImpl implements ChatSessionService {

    @Autowired
    private ChatSessionMapper chatsessionMapper;

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Override
    @Transactional
    public String createSession(String userId) {
        String sessionId = UUID.randomUUID().toString();

        log.info("创建新会话，userId: {}, sessionId: {}", userId, sessionId);

        Chatsession session = new Chatsession();
        session.setSessionId(sessionId);
        session.setUserId(userId);
        session.setCreateTime(new Date());
        session.setContent("新会话");

        int result = chatsessionMapper.insert(session);
        log.info("插入会话结果: {}, 影响行数: {}", sessionId, result);

        return sessionId;
    }

    @Override
    @Transactional
    public void ensureSessionExists(String sessionId, String userId) {
        // 检查会话是否已存在
        QueryWrapper<Chatsession> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("session_id", sessionId);
        log.info("现在的sessionid:{},userid:{}",sessionId,userId);

        Long count = chatsessionMapper.selectCount(queryWrapper);

        if (count == 0) {
            log.info("会话不存在，创建会话: sessionId={}, userId={}", sessionId, userId);

            Chatsession session = new Chatsession();
            session.setSessionId(sessionId);
            session.setUserId(userId);
            session.setCreateTime(new Date());
            session.setContent("已有会话");

            int result = chatsessionMapper.insert(session);
            log.info("创建会话结果: 影响行数: {}", result);
        } else {
            log.info("会话已存在: sessionId={}", sessionId);
        }
    }

    @Override
    @Transactional
    public void saveMessage(String sessionId, String senderType, String content) {
        log.info("保存消息，sessionId: {}, senderType: {}", sessionId, senderType);
        log.info("消息内容（前100字符）: {}", content.length() > 100 ? content.substring(0, 100) + "..." : content);

        ChatMessage message = new ChatMessage();
        message.setSessionId(sessionId);
        message.setSenderType(senderType);
        message.setContent(content);
        message.setCreateTime(new Date());

        int result = chatMessageMapper.insert(message);
        log.info("插入消息结果: 影响行数: {}, 消息ID: {}", result, message.getId());
    }

    @Override
    @Transactional
    public void deleteSession(String sessionId, String userId) {
        log.info("开始删除会话，sessionId: {}, userId: {}", sessionId, userId);

        // 1. 验证会话是否存在且属于该用户
        QueryWrapper<Chatsession> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("session_id", sessionId)
                .eq("user_id", userId);

        Long sessionCount = chatsessionMapper.selectCount(queryWrapper);

        if (sessionCount == 0) {
            log.warn("要删除的会话不存在或不属于该用户，sessionId: {}, userId: {}", sessionId, userId);
            throw new RuntimeException("会话不存在或无权删除");
        }

        try {
            // 2. 先删除该会话下的所有消息（根据session_id）
            QueryWrapper<ChatMessage> messageQueryWrapper = new QueryWrapper<>();
            messageQueryWrapper.eq("session_id", sessionId);

            int deletedMessages = chatMessageMapper.delete(messageQueryWrapper);
            log.info("删除会话消息成功，共删除 {} 条消息，sessionId: {}", deletedMessages, sessionId);

            // 3. 删除会话记录（根据session_id和user_id）
            int deletedSession = chatsessionMapper.delete(queryWrapper);
            log.info("删除会话记录成功，影响行数: {}, sessionId: {}", deletedSession, sessionId);

            log.info("会话删除完成，sessionId: {}, userId: {}", sessionId, userId);
        } catch (Exception e) {
            log.error("删除会话时发生异常，sessionId: {}, userId: {}", sessionId, userId, e);
            throw new RuntimeException("删除会话失败: " + e.getMessage());
        }
    }


    @Override
    public List<ChatMessage> getHistory(String sessionId) {
        log.info("查询历史消息，sessionId: {}", sessionId);

        List<ChatMessage> messages = chatMessageMapper.findBySessionId(sessionId);
        log.info("查询到 {} 条消息", messages.size());

        return messages;
    }

    @Override
    public List<Chatsession> getUserSessions(String userId) {
        log.info("查询用户会话，userId: {}", userId);

        QueryWrapper<Chatsession> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .orderByDesc("create_time");

        List<Chatsession> sessions = chatsessionMapper.selectList(queryWrapper);

        log.info("查询到 {} 个会话", sessions.size());
        return sessions;
    }

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void save(String type, String chatId, String userId) {
        //把chatId保存到Redis的set数据结构
        redisTemplate.opsForSet().add("type:"+type+"userId:"+userId,chatId);
    }

    @Override
    public List<String> getChatIds(String type, String userId) {
        //取出chatId的set集合
        Set chatIds = redisTemplate.opsForSet().members("type:" + type + "userId:" + userId);
        //判断set集合是否为空，如果不为空转换成list集合返回，如果是空的返回一个空的list集合
        return chatIds!=null?new ArrayList<>(chatIds): Collections.emptyList();
    }
}