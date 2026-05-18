package com.gxa.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    private String nickname;
    private String phone;
    private String password;
    private String avatarUrl;
    private Integer credit;
    private LocalDateTime createTime;
}