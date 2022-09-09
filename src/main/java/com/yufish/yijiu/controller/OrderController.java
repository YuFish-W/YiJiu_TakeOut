package com.yufish.yijiu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yufish.yijiu.common.R;
import com.yufish.yijiu.entity.Orders;
import com.yufish.yijiu.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 订单
 */
@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 用户下单
     *
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders) {
        log.info("订单数据：{}", orders);
        orderService.submit(orders);
        return R.success("下单成功");
    }

    /**
     * 分页查询订单，number表示订单号，另外两个是时间范围
     * @param page
     * @param pageSize
     * @param number
     * @param beginTime
     * @param endTime
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, Long number, String beginTime, String endTime) {
        //分页构造器对象
        Page<Orders> pageInfo = new Page<>(page, pageSize);

        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        //找出日期在此之间的订单
        queryWrapper.between(beginTime != null && endTime != null, Orders::getOrderTime, beginTime, endTime);
        queryWrapper.ge(beginTime != null && endTime == null, Orders::getOrderTime, beginTime);
        queryWrapper.le(beginTime == null && endTime != null, Orders::getOrderTime, endTime);
        queryWrapper.like(number != null, Orders::getNumber, number);

        orderService.page(pageInfo, queryWrapper);

        return R.success(pageInfo);
    }

    @PutMapping
    public R<String > modifyStatus(@RequestBody Orders orders){
        orderService.updateById(orders);
        return R.success("状态修改成功");
    }
}