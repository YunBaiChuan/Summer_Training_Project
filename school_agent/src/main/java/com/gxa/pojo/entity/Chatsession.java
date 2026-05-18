package com.gxa.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("chat_session")
public class Chatsession {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String sessionId;  // 会话ID
    private String userId;     // 用户ID
    private String content;    // 最后一条消息
    private Date createTime;   // 创建时间
}