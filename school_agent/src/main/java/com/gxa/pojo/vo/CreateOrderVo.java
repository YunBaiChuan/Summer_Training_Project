package com.gxa.pojo.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreateOrderVo {
    private Long userId;          // 买家ID
    private Long productId;       // 商品ID
    private BigDecimal price;     // 价格
    private String place;         // 见面地点
    private LocalDateTime meetTime; // 见面时间
}