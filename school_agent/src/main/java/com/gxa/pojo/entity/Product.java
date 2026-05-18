package com.gxa.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("product")
public class Product {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("seller_id")
    private Long sellerId;

    @TableField("category_id")
    private Long categoryId;

    @TableField("title")
    private String title;

    @TableField("price")
    private BigDecimal price;

    @TableField("description")
    private String description;

    @TableField("status")
    private Integer status;
}
