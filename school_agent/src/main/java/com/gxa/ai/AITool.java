package com.gxa.ai;

import com.gxa.pojo.entity.Product;
import com.gxa.pojo.vo.CreateOrderVo;
import com.gxa.result.Result;
import com.gxa.service.OrderService;
import com.gxa.service.ProductService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
public class AITool {

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    // 功能1：商品查询
    @Tool(description = "搜索二手商品，支持按商品标题搜索")
    public List<Product> searchProducts(@ToolParam(description = "搜索关键词") String keyword) {
        return productService.lambdaQuery()
                .like(Product::getTitle, keyword)
                .eq(Product::getStatus, 1) // 1=上架商品
                .list();
    }

    // 功能2：创建订单
    @Tool(description = "创建二手交易订单，需要商品ID、买家ID、价格和见面地点")
    public Map<String, Object> createOrder(
            @ToolParam(description = "商品ID") Long productId,
            @ToolParam(description = "买家用户ID") Long userId,
            @ToolParam(description = "成交价格") BigDecimal price,
            @ToolParam(description = "见面交易地点") String place) {

        try {
            // 1. 检查商品是否存在且上架
            Product product = productService.getById(productId);
            if (product == null) {
                return Map.of("success", false, "message", "商品不存在");
            }

            if (product.getStatus() != 1) {
                return Map.of("success", false, "message", "商品已下架");
            }

            // 2. 验证价格
            if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
                return Map.of("success", false, "message", "价格无效");
            }

            // 3. 验证地点
            if (place == null || place.trim().isEmpty()) {
                return Map.of("success", false, "message", "请填写见面地点");
            }

            // 4. 创建CreateOrderVo对象
            CreateOrderVo vo = new CreateOrderVo();
            vo.setUserId(userId);
            vo.setProductId(productId);
            vo.setPrice(price);
            vo.setPlace(place);
            // 设置默认见面时间为明天
            vo.setMeetTime(LocalDateTime.now().plusDays(1));

            // 5. 调用Service方法创建订单
            Result<Long> result = orderService.createOrder(vo);

            // 6. 根据Result的code判断是否成功
            if (result.getCode() == 0) {
                // 创建成功，下架商品
                product.setStatus(0);
                productService.updateById(product);

                return Map.of(
                        "success", true,
                        "message", "订单创建成功",
                        "orderId", result.getData(),
                        "productTitle", product.getTitle()
                );
            } else {
                return Map.of(
                        "success", false,
                        "message", result.getMsg(),
                        "code", result.getCode()
                );
            }

        } catch (Exception e) {
            return Map.of(
                    "success", false,
                    "message", "系统错误：" + e.getMessage()
            );
        }
    }
}