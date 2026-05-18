package com.gxa.pojo.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderCreateDto {
    private Long productId;       // 商品ID
    private Long buyerId;         // 买家ID
    private BigDecimal finalPrice; // 成交价格
    private String place;         // 见面地点
    private LocalDateTime meetTime; // 见面时间
}