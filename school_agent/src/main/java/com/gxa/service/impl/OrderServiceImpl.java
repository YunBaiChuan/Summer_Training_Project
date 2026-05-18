package com.gxa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper; // ← 新增导入
import com.gxa.result.Result;
import com.gxa.pojo.vo.CreateOrderVo;
import com.gxa.pojo.entity.Order;
import com.gxa.pojo.entity.Product;
import com.gxa.mapper.OrderMapper;
import com.gxa.mapper.ProductMapper;
import com.gxa.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ProductMapper productMapper;

    @Override
    public Result createOrder(CreateOrderVo vo) {
        if (vo == null || vo.getUserId() == null || vo.getProductId() == null) {
            return Result.buildFail(1, "订单信息不完整");
        }

        Product product = productMapper.selectById(vo.getProductId());
        if (product == null) {
            return Result.buildFail(1, "商品不存在");
        }

        Order order = new Order();
        order.setBuyerId(vo.getUserId());
        order.setProductId(vo.getProductId());
        order.setSellerId(product.getSellerId());
        order.setFinalPrice(vo.getPrice());
        order.setPlace(vo.getPlace());
        order.setMeetTime(vo.getMeetTime());
        order.setStatus(0);

        int rows = orderMapper.insert(order);
        if (rows > 0 && order.getId() != null) {
            return Result.buildSuccess("订单创建成功", order.getId());
        } else {
            return Result.buildFail(1, "订单插入失败");
        }
    }

    @Override
    public Result listOrders(Long userId) {
        if (userId == null || userId <= 0) {
            return Result.buildFail(1, "用户ID无效");
        }

        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getBuyerId, userId)
                .orderByDesc(Order::getCreateTime);

        List<Order> orders = orderMapper.selectList(wrapper);
        return Result.buildSuccess(orders);
    }

    // ✅ 新增：查询全部订单
    @Override
    public Result listAllOrders() {
        List<Order> orders = orderMapper.selectList(new QueryWrapper<>());
        return Result.buildSuccess(orders);
    }

    @Override
    public Result completeOrder(Long orderId) {
        if (orderId == null || orderId <= 0) {
            return Result.buildFail(1, "订单ID无效");
        }

        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            return Result.buildFail(1, "订单不存在");
        }

        if (order.getStatus() != 0) {
            return Result.buildFail(1, "订单不可完成");
        }

        order.setStatus(1);
        orderMapper.updateById(order);
        return Result.buildSuccess("订单已完成");
    }

    @Override
    public Result deleteOrder(Long orderId) {
        if (orderId == null || orderId <= 0) {
            return Result.buildFail(1, "订单ID无效");
        }

        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            return Result.buildFail(1, "订单不存在");
        }

        if (order.getStatus() != 0) {
            return Result.buildFail(1, "只能删除待见面的订单");
        }

        int rows = orderMapper.deleteById(orderId);
        if (rows > 0) {
            return Result.buildSuccess("订单删除成功");
        } else {
            return Result.buildFail(1, "订单删除失败");
        }
    }
    @Override
    public Order getById(Long id) {
        return orderMapper.selectById(id);
    }

    @Override
    public List<Order> list() {
        return orderMapper.selectList(null);
    }
}