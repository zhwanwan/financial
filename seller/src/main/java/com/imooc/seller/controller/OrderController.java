package com.imooc.seller.controller;

import com.imooc.entity.Order;
import com.imooc.seller.params.OrderParam;
import com.imooc.seller.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单相关
 */
@RestController
@RequestMapping("/order")
public class OrderController {

    private static final Logger LOG = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    @PostMapping("/apply")
    public Order apply(@RequestHeader String authId, @RequestHeader String sign, @RequestBody OrderParam param) {
        LOG.info("申购请求：{}", param);
        Order order = new Order();
        BeanUtils.copyProperties(param, order);
        order = orderService.apply(order);
        LOG.info("申购结果:{}", order);
        return order;
    }
}
