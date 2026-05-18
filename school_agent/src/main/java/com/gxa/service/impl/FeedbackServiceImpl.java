package com.gxa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gxa.mapper.FeedbackMapper;
import com.gxa.mapper.OrderMapper;
import com.gxa.mapper.UserMapper;
import com.gxa.pojo.entity.Feedback;
import com.gxa.pojo.entity.Order;
import com.gxa.result.Result;
import com.gxa.service.FeedbackService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FeedbackServiceImpl implements FeedbackService {

    // 已有依赖
    private final FeedbackMapper feedbackMapper;
    private final OrderMapper orderMapper;
    private final UserMapper userMapper;

    public FeedbackServiceImpl(FeedbackMapper feedbackMapper,
                               OrderMapper orderMapper,
                               UserMapper userMapper) {
        this.feedbackMapper = feedbackMapper;
        this.orderMapper = orderMapper;
        this.userMapper = userMapper;
    }

    /* -------- 新增：倒序全部评价 -------- */
    @Override
    public List<Feedback> listAll() {
        return feedbackMapper.selectList(
                new LambdaQueryWrapper<Feedback>()
                        .orderByDesc(Feedback::getId));
    }

    /* -------- 原有：新增评价（MP 自动填充 create_time） -------- */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Long> add(Feedback feedback) {
        boolean exists = feedbackMapper.selectCount(
                new LambdaQueryWrapper<Feedback>()
                        .eq(Feedback::getOrderId, feedback.getOrderId())) > 0;
        if (exists) {
            return Result.buildFail(400, "该订单已评价，请勿重复提交");
        }
        feedback.setCreateTime(LocalDateTime.now());
        feedbackMapper.insert(feedback);
        Order order = orderMapper.selectById(feedback.getOrderId());
        userMapper.incrCredit(order.getSellerId());
        userMapper.incrCredit(order.getBuyerId());
        return Result.buildSuccess(feedback.getId());
    }

    @Override
    public Feedback getByOrderId(Long orderId) {
        return feedbackMapper.selectOne(
                new LambdaQueryWrapper<Feedback>()
                        .eq(Feedback::getOrderId, orderId));
    }

    @Override
    public boolean removeById(Long id) {
        return feedbackMapper.deleteById(id) > 0;
    }
}