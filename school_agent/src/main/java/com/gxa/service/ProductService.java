package com.gxa.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gxa.pojo.entity.Product;

import java.util.List;

/**
 * @author 22129
 * @date 2026/01/19
 */
public interface ProductService extends IService<Product> {
    List<Integer> getId(Product product);

    boolean updateProductStatus(Long productId, Integer status, Long currentUserId);
}
