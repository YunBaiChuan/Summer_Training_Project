package com.gxa.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("feedback")
public class Feedback {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private Integer star;

    private String tag;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}