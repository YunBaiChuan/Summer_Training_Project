package com.gxa.controller;

import com.gxa.pojo.entity.Order;
import com.gxa.result.Result;
import com.gxa.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
@Tag(name = "订单接口")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Operation(summary = "新增订单")
    @PostMapping("/create")
    public Result createOrder(@RequestBody com.gxa.pojo.vo.CreateOrderVo vo) {
        return orderService.createOrder(vo);
    }

    @Operation(summary = "查询用户订单")
    @GetMapping("/list/{userId}")
    public Result listOrders(@PathVariable Long userId) {
        return orderService.listOrders(userId);
    }

    // ✅ 新增：查询全部订单
    @Operation(summary = "查询全部订单")
    @GetMapping("/all")
    public Result listAllOrders() {
        return orderService.listAllOrders();
    }

    @Operation(summary = "标记订单完成")
    @PostMapping("/complete/{orderId}")
    public Result completeOrder(@PathVariable Long orderId) {
        return orderService.completeOrder(orderId);
    }

    @Operation(summary = "删除订单")
    @PostMapping("/delete/{orderId}")
    public Result deleteOrder(@PathVariable Long orderId) {
        return orderService.deleteOrder(orderId);
    }

    @Operation(summary = "根据id查询订单")
    @GetMapping("/{id}")
    public Result<com.gxa.pojo.entity.Order> getById(@PathVariable Long id){
        return Result.buildSuccess(orderService.getById(id));
    }

    @GetMapping("/dball")
    public Result<List<Order>> allOrders() {
        List<Order> list = orderService.list(); // MP 内置
        return Result.buildSuccess(list);
    }
}