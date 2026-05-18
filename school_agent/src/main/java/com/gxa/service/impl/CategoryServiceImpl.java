package com.gxa.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gxa.mapper.CategoryMapper;
import com.gxa.pojo.entity.Category;
import com.gxa.service.CategoryService;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
}