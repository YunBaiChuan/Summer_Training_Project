package com.gxa.pojo.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

@Data
public class ProductDto extends BasePageDto{
    //卖家id
    private Long sellerId;
    //类别id
    private Long categoryId;
    //商品标签
    private String title;
}
