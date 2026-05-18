package com.gxa.pojo.dto;

import lombok.Data;

/**
 * @author zxd
 * @date 2026/1/17
 */
@Data
public class BasePageDto {

    //当前页数
    private Integer page=1;

    //分页大小
    private Integer limit=5;

}
