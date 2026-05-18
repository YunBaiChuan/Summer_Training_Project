package com.gxa.pojo.vo;

import lombok.Data;
import org.springframework.ai.chat.messages.Message;


/**
 * @author zxd
 * @date 2026/1/17
 */
@Data
public class MessageVo {

    private String role;

    private String content;

    public MessageVo(Message message){
        switch (message.getMessageType()){
            case USER:
                this.role = "user";
                break;
            case ASSISTANT:
                this.role = "assistant";
                break;
            case SYSTEM:
                this.role = "system";
                break;
        }
        this.content=message.getText();
    }


}