package com.yufish.yijiu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yufish.yijiu.entity.OrderDetail;
import lombok.Data;

import java.util.Collection;

public interface OrderDetailService extends IService<OrderDetail> {

    /**
     * 一个自定义类，作为saveBatch方法返回值
     * res表示保存成功还是不成功
     * dishId表示保存 不成功 的时候，是哪一盘菜数量不够
     */
    @Data
    static class Status{
        private boolean res;
        private Long dishId;

        public Status(boolean res, Long dishId) {
            this.res = res;
            this.dishId = dishId;
        }
    }

    /**
     * 保存多个订单详情，保存的同时检查当前需要的菜品数量是否足够，且用乐观锁保证不会发生超卖
     * @param entityList 订单集合
     * @return
     */
    public Status MySaveBatch(Collection<OrderDetail> entityList);
}
