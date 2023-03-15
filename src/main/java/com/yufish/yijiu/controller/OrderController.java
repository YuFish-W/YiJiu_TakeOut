package com.yufish.yijiu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yufish.yijiu.common.Result;
import com.yufish.yijiu.entity.Orders;
import com.yufish.yijiu.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

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
    public Result<String> submit(@RequestBody Orders orders) {
        log.info("订单数据：{}", orders);
        orderService.submit(orders);
        return Result.success("下单成功");
    }

    /**
     * 分页查询订单，number表示订单号，另外两个是时间范围
     *
     * @param page      第几页
     * @param pageSize  每页多少条数据
     * @param number    表示订单号
     * @param beginTime 订单开始时间，这段时间内的订单
     * @param endTime   订单结束时间
     * @return 返回分页对象Page
     */
    @GetMapping("/page")
    public Result<Page<Orders>> page(int page, int pageSize, Long number, String beginTime, String endTime) {
        //分页构造器对象
        Page<Orders> pageInfo = new Page<>(page, pageSize);

        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        //找出日期在此之间的订单
        queryWrapper.between(beginTime != null && endTime != null, Orders::getOrderTime, beginTime, endTime);
        queryWrapper.ge(beginTime != null && endTime == null, Orders::getOrderTime, beginTime);
        queryWrapper.le(beginTime == null && endTime != null, Orders::getOrderTime, endTime);
        queryWrapper.like(number != null, Orders::getNumber, number);

        orderService.page(pageInfo, queryWrapper);

        return Result.success(pageInfo);
    }

    /**
     * 根据传入的状态码和订单id修改订单状态
     * @param orders
     * @return
     */
    @PutMapping
    public Result<String> modifyStatus(@RequestBody Orders orders) {
        orderService.updateById(orders);
        return Result.success("状态修改成功");
    }

    /**
     * 获取当前用户的订单记录
     * @param page 第几页
     * @param pageSize 分页大小
     * @param session 得到当前用户id
     * @return 返回R对象
     */
    @GetMapping("/userPage")
    public Result<Page<Orders>> userPage(int page, int pageSize, HttpSession session) {
        //获取当前用户id
        Long userId = (Long) session.getAttribute("user");
        //分页构造器对象
        Page<Orders> pageInfo = new Page<>(page, pageSize);

        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getUserId, userId);

        //订单按照时间顺序，倒序排列
        queryWrapper.orderByDesc(Orders::getOrderTime);


        orderService.page(pageInfo, queryWrapper);

        return Result.success(pageInfo);
    }
}