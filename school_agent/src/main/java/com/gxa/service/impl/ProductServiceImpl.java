package com.gxa.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gxa.mapper.ProductMapper;
import com.gxa.pojo.entity.Product;
import com.gxa.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {
    @Autowired
    private ProductMapper productMapper;
    @Override
    public List<Integer> getId(Product product) {
        Long sellerId = product.getSellerId();
        Long categoryId = product.getCategoryId();
        return productMapper.getIdByProduct(sellerId,categoryId);
    }

    @Override
    public boolean updateProductStatus(Long productId, Integer status, Long currentUserId) {
        try{
            boolean flag = productMapper.updateProductStatus(productId,status,currentUserId);
        }catch (RuntimeException e){
            return false;
        }
        return true;
    }
}
