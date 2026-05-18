package com.gxa.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("chat_message")
public class ChatMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String sessionId;    // 会话ID
    private String senderType;   // 发送者类型：user/assistant
    private String content;      // 消息内容
    private Date createTime;     // 创建时间
}