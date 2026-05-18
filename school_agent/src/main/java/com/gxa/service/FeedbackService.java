package com.gxa.service;


import com.gxa.pojo.entity.Feedback;
import com.gxa.result.Result;

import java.util.List;

public interface FeedbackService {

    /**
     * 新增评价
     */
    Result<Long> add(Feedback feedback);

    /**
     * 根据订单号查询评价
     */
    Feedback getByOrderId(Long orderId);

    boolean removeById(Long id);

    List<Feedback> listAll();
}