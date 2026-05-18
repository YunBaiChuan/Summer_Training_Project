package com.gxa.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("`order`")
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long productId;
    private Long sellerId;
    private Long buyerId;
    private BigDecimal finalPrice;
    private String place;
    private LocalDateTime meetTime;
    private Integer status; // 0待见面 1已完成 2已取消
    private LocalDateTime createTime;
}