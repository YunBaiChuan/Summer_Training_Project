package com.gxa.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxa.pojo.entity.Category;
import com.gxa.result.Result; // 导入你自己的 Result 类
import com.gxa.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/category")
@Tag(name = "类别接口")
@Validated // 开启参数校验
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    // 新增类别 - 适配你的 Result 类
    @Operation(summary = "新增类别")
    @PostMapping
    public Result<String> addCategory(@Validated @RequestBody Category category) {
        try {
            boolean saved = categoryService.save(category);
            if (saved) {
                // 调用你定义的 buildSuccess 方法
                return Result.buildSuccess("新增成功");
            } else {
                // 调用 buildFail，错误码建议用非0（比如500）
                return Result.buildFail(500, "新增失败：数据库保存未生效");
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 异常时返回错误信息
            return Result.buildFail(500, "新增失败：" + e.getMessage());
        }
    }

    // 分页查询 - 适配分页版 buildSuccess
    @Operation(summary = "分页查找类别", description = "一页查10条，支持翻页")
    @GetMapping("/page")
    public Result<IPage<Category>> getCategoryPage(
            @RequestParam(defaultValue = "1") @Min(1) Integer current,
            @RequestParam(defaultValue = "10") @Min(1) Integer size) {
        try {
            IPage<Category> page = categoryService.page(new Page<>(current, size));
            // 分页查询用带 count 的 buildSuccess
            return Result.buildSuccess(page, page.getTotal());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.buildFail(500, "分页查询失败：" + e.getMessage());
        }
    }

    // 修改类别
    @Operation(summary = "修改类别")
    @PutMapping("/{id}")
    public Result<String> updateCategory(@PathVariable @Min(1) Long id, @Validated @RequestBody Category category) {

        try {
            // 1. 直接使用路径ID，覆盖请求体中的ID（如果有的话）
            category.setId(id);

            // 2. 检查记录是否存在（可选）
            Category existing = categoryService.getById(id);
            if (existing == null) {
                return Result.buildFail(404, "修改失败：未找到该类别");
            }

            // 3. 检查名称是否重复（排除自身）
            if (category.getName() != null && !category.getName().equals(existing.getName())) {
                LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(Category::getName, category.getName())
                        .ne(Category::getId, id);
                long count = categoryService.count(queryWrapper);
                if (count > 0) {
                    return Result.buildFail(400, "类别名称已存在");
                }
            }

            // 4. 执行更新
            boolean updated = categoryService.updateById(category);
            return updated ? Result.buildSuccess("修改成功") : Result.buildSuccess("数据无变化");

        } catch (DuplicateKeyException e) {
            return Result.buildFail(400, "类别名称已存在");
        } catch (Exception e) {
            return Result.buildFail(500, "修改失败：系统内部错误");
        }
    }

    // 删除类别
    @Operation(summary = "删除类别", description = "根据ID删对应的类别")
    @DeleteMapping("/{id}")
    public Result<String> deleteCategory(@PathVariable @Min(1) Long id) {
        try {
            boolean removed = categoryService.removeById(id);
            if (removed) {
                return Result.buildSuccess("删除成功");
            } else {
                return Result.buildFail(500, "删除失败：未找到该类别");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.buildFail(500, "删除失败：" + e.getMessage());
        }
    }

    // 查单个类别
    @Operation(summary = "查找类别", description = "根据ID查类别详情")
    @GetMapping("/{id}")
    public Result<Category> getCategoryById(@PathVariable @Min(1) Long id) {
        try {
            Category category = categoryService.getById(id);
            if (category != null) {
                return Result.buildSuccess(category);
            } else {
                return Result.buildFail(404, "查询失败：未找到该类别");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.buildFail(500, "查询失败：" + e.getMessage());
        }
    }
}