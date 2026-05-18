package com.gxa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gxa.pojo.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    List<Integer> getIdByProduct(Long sellerId, Long categoryId);

    boolean updateProductStatus(Long productId, Integer status, Long currentUserId);
}
