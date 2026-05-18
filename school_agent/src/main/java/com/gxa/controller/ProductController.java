package com.gxa.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxa.pojo.dto.ProductDto;
import com.gxa.pojo.entity.Product;
import com.gxa.pojo.vo.ProductVo;
import com.gxa.result.Result;
import com.gxa.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "商品接口")
@RequestMapping("/product")
public class ProductController {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private ProductService productService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 创建商品
     *
     * @param product
     * @return {@link Result }
     */
    @PostMapping("/add")
    @Operation(summary = "新增商品")
    public Result add(@RequestBody Product product) {
        productService.save(product);
        List<Integer> ids = productService.getId(product);
        Collections.sort(ids);
        redisTemplate.opsForValue().set(ids.get(ids.size() - 1) + "", 0);
        return Result.buildSuccess("新增商品成功");
    }

    /**
     * 更新商品
     *
     * @param product
     * @return {@link Result }
     */
    @PutMapping("/update/product")
    @Operation(summary = "更新商品")
    public Result updateProduct(@RequestBody Product product) {
        productService.updateById(product);
        return Result.buildSuccess();
    }

    /**
     * 删除商品
     *
     * @param id
     * @return {@link Result }
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除商品")
    public Result delete(@RequestParam Long id) {
        if (id == null) {
            return Result.buildFail(20001, "id不能为空");
        }
        productService.removeById(id);
        return Result.buildSuccess();
    }

    /**
     * 分页查询商品
     *
     * @param productDto
     * @return {@link Result }
     */
    @GetMapping("/list")
    @Operation(summary = "分页查找商品")
    public Result list(@ModelAttribute ProductDto productDto) {
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        if (productDto.getSellerId() != null) {
            queryWrapper.eq("seller_id", productDto.getSellerId());
        }
        if (productDto.getCategoryId() != null) {
            queryWrapper.eq("category_id", productDto.getCategoryId());
        }
        if (!StringUtils.isEmpty(productDto.getTitle())) {
            queryWrapper.like("title", productDto.getTitle());
        }

        Page<Product> page = new Page<>(productDto.getPage(), productDto.getLimit());

        Page<Product> productPage = productService.page(page, queryWrapper);

        return Result.buildSuccess(productPage.getRecords(), productPage.getPages());
    }

    /**
     * 查询用户发布的商品
     *
     * @param sellerId
     * @return {@link Result }
     */
    @GetMapping("/seek/product/{sellerId}")
    @Operation(summary = "查找用户发布的商品")
    public Result getProduct(@PathVariable Long sellerId) {
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        if (sellerId != null) {
            queryWrapper.eq("seller_id", sellerId);
        }
        List<Product> list = productService.list(queryWrapper);
        return Result.buildSuccess(list);
    }

    /**
     * 搜索商品
     *
     * @param title
     * @return {@link Result }
     */
    @GetMapping("/search")
    @Operation(summary = "查找商品")
    public Result search(@RequestParam String title) {
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("title", title);
        queryWrapper.orderByDesc("price");
        List<Product> productList = productService.list(queryWrapper);
        if (productList.size() == 0) {
            return Result.buildFail(20001, "title检索为空");
        }
        return Result.buildSuccess(productList);
    }
}
