package com.gxa.pojo.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductVo{
    private Long id;

    private Long sellerId;

    private Long categoryId;

    private String title;

    private BigDecimal price;

    private String description;

    private Integer status; // 0在售 1已售 2下架
}
