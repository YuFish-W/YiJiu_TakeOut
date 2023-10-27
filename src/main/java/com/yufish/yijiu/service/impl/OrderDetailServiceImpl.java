package com.yufish.yijiu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yufish.yijiu.dto.SetmealDTO;
import com.yufish.yijiu.entity.DishPO;
import com.yufish.yijiu.entity.OrderDetailPO;
import com.yufish.yijiu.entity.SetmealDishPO;
import com.yufish.yijiu.mapper.OrderDetailMapper;
import com.yufish.yijiu.service.DishService;
import com.yufish.yijiu.service.OrderDetailService;
import com.yufish.yijiu.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetailPO> implements OrderDetailService {

    private final DishService dishService;

    private final SetmealService setmealService;

    @Autowired
    public OrderDetailServiceImpl(DishService dishService, SetmealService setmealService) {
        this.dishService = dishService;
        this.setmealService = setmealService;
    }

    /**
     * 保存多个订单详情，保存的同时检查当前需要的菜品数量是否足够，且用乐观锁保证不会发生超卖
     * @param entityList 订单集合
     * @return 返回一个自定义类Status
     */
    @Override
    public Status MySaveBatch(Collection<OrderDetailPO> entityList) {
        for (OrderDetailPO orderDetailPO : entityList) {
            Long dishId = orderDetailPO.getDishId();
            // 点了几份该套餐或者该菜品
            Integer number = orderDetailPO.getNumber();
            // 如果菜品Id是null就表示是套餐
            if (dishId == null) {

                // 套餐id
                Long setmealId = orderDetailPO.getSetmealId();

                // 得到该id的套餐有哪些菜品
                SetmealDTO setmealDto = setmealService.getWithDish(setmealId);
                List<SetmealDishPO> setmealDishPOS = setmealDto.getSetmealDishPOS();

                // 遍历这个套餐所有菜品，检查该菜品剩余份数是否足够
                for (SetmealDishPO setmealDishPO : setmealDishPOS) {
                    // 该套餐包含几份该菜品
                    Integer copies = setmealDishPO.getCopies();
                    // 该订单需要几份该菜品
                    Integer need = copies * number;
                    // 该菜品剩余多少份
                    DishPO dishPO = dishService.getById(setmealDishPO.getDishId());
                    Integer quantity = dishPO.getQuantity();
                    if (quantity < need) {
                        return new Status(false, setmealDishPO.getDishId());
                    }

                    // 如果菜品份数够，就减去此份数
                    dishPO.setQuantity(quantity - need);
                    boolean res = dishService.updateById(dishPO);
                    // 如果修改不成功，表示和其它用户修改发生了冲突，此时再次获取菜品数量比较，然后修改
                    while (!res) {
                        dishPO = dishService.getById(setmealDishPO.getDishId());
                        quantity = dishPO.getQuantity();
                        if (quantity < need) {
                            return new Status(false, setmealDishPO.getDishId());
                        }
                        // 如果菜品份数够，就减去此份数
                        dishPO.setQuantity(quantity - need);
                        res = dishService.updateById(dishPO);
                    }
                }
            } else {
                DishPO dishPO = dishService.getById(dishId);
                Integer quantity = dishPO.getQuantity();
                if (quantity < number) {
                    return new Status(false,dishId);
                }
                // 如果菜品份数够，就减去此份数
                dishPO.setQuantity(quantity - number);
                boolean res = dishService.updateById(dishPO);
                //如果修改不成功，表示和其它用户修改发生了冲突，此时再次获取菜品数量比较，然后修改
                while (!res) {
                    dishPO = dishService.getById(dishId);
                    quantity = dishPO.getQuantity();
                    if (quantity < number) {
                        return new Status(false,dishId);
                    }
                    // 如果菜品份数够，就减去此份数
                    dishPO.setQuantity(quantity - number);
                    res = dishService.updateById(dishPO);
                }
            }

            this.save(orderDetailPO);
        }
        return new Status(true,null);
    }
}