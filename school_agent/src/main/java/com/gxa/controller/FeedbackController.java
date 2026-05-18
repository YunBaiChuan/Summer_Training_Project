package com.gxa.controller;

import com.gxa.pojo.entity.Feedback;
import com.gxa.result.Result;
import com.gxa.service.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/feedback")
@Tag(name = "评价接口")
public class FeedbackController {

    @Resource
    private FeedbackService feedbackService;

    @Operation(summary = "新增评价")
    @PostMapping
    public Result<Long> add(@RequestBody Feedback feedback){
        // 改成现有的 buildSuccess
        return Result.buildSuccess(feedbackService.add(feedback));
    }

    @Operation(summary = "查找评价")
    @GetMapping("/order/{orderId}")
    public Result<Feedback> getByOrderId(@PathVariable Long orderId){
        // 改成现有的 buildSuccess
        return Result.buildSuccess(feedbackService.getByOrderId(orderId));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteFeedback(@PathVariable Long id) {
        feedbackService.removeById(id);
        return Result.buildSuccess();
    }

    @GetMapping("/all")
    public Result<List<Feedback>> all() {
        return Result.buildSuccess(feedbackService.listAll());
    }
}