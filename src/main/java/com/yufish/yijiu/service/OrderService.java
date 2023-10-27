package com.yufish.yijiu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yufish.yijiu.entity.OrdersPO;

public interface OrderService extends IService<OrdersPO> {

    /**
     * 用户下单
     * @param ordersPO
     */
    public void submit(OrdersPO ordersPO);
}
