package com.gxa.test;

import com.gxa.ai.AITool;
import com.gxa.pojo.entity.Product;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class TestAITool {
    @Autowired
    private AITool aiTool;

    @Test
    void testSearchProduct() {
        // 直接测试工具方法
        List<Product> products = aiTool.searchProducts("手机");
        System.out.println("搜索结果：" + products);
        Assertions.assertNotNull(products);
    }

    @Test
    void testCreateOrder() {
        Map<String, Object> result = aiTool.createOrder(
                1L,  // productId
                100L, // userId
                new BigDecimal("1999.99"), // price
                "北京中关村" // place
        );
        System.out.println("创建订单结果：" + result);
        Assertions.assertNotNull(result);
    }
}