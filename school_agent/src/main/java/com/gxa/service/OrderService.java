package com.gxa.service;

import com.gxa.pojo.entity.Order;
import com.gxa.result.Result;

import java.util.List;

public interface OrderService {

    Result createOrder(com.gxa.pojo.vo.CreateOrderVo vo);

    Result listOrders(Long userId);

    Result completeOrder(Long orderId);

    Result deleteOrder(Long orderId);

    Result listAllOrders();

    Order getById(Long id);

    List<Order> list();
}