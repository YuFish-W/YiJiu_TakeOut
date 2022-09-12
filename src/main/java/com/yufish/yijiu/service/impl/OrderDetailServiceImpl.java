package com.yufish.yijiu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yufish.yijiu.dto.SetmealDto;
import com.yufish.yijiu.entity.Dish;
import com.yufish.yijiu.entity.OrderDetail;
import com.yufish.yijiu.entity.SetmealDish;
import com.yufish.yijiu.mapper.OrderDetailMapper;
import com.yufish.yijiu.service.DishService;
import com.yufish.yijiu.service.OrderDetailService;
import com.yufish.yijiu.service.SetmealService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {

    @Autowired
    DishService dishService;

    @Autowired
    SetmealService setmealService;

    /**
     * 保存多个订单详情，保存的同时检查当前需要的菜品数量是否足够，且用乐观锁保证不会发生超卖
     * @param entityList 订单集合
     * @return 返回一个自定义类Status
     */
    @Override
    public Status MySaveBatch(Collection<OrderDetail> entityList) {
        for (OrderDetail orderDetail : entityList) {
            Long dishId = orderDetail.getDishId();
            //点了几份该套餐或者该菜品
            Integer number = orderDetail.getNumber();
            //如果菜品Id是null就表示是套餐
            if (dishId == null) {

                //套餐id
                Long setmealId = orderDetail.getSetmealId();

                //得到该id的套餐有哪些菜品
                SetmealDto setmealDto = setmealService.getWithDish(setmealId);
                List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();

                //遍历这个套餐所有菜品，检查该菜品剩余份数是否足够
                for (SetmealDish setmealDish : setmealDishes) {
                    //该套餐包含几份该菜品
                    Integer copies = setmealDish.getCopies();
                    //该订单需要几份该菜品
                    Integer need = copies * number;
                    //该菜品剩余多少份
                    Dish dish = dishService.getById(setmealDish.getDishId());
                    Integer quantity = dish.getQuantity();
                    if (quantity < need) {
                        return new Status(false,setmealDish.getDishId());
                    }

                    //如果菜品份数够，就减去此份数
                    dish.setQuantity(quantity - need);
                    boolean res = dishService.updateById(dish);
                    //如果修改不成功，表示和其它用户修改发生了冲突，此时再次获取菜品数量比较，然后修改
                    while (!res) {
                        dish = dishService.getById(setmealDish.getDishId());
                        quantity = dish.getQuantity();
                        if (quantity < need) {
                            return new Status(false,setmealDish.getDishId());
                        }
                        //如果菜品份数够，就减去此份数
                        dish.setQuantity(quantity - need);
                        res = dishService.updateById(dish);
                    }
                }
            } else {
                Dish dish = dishService.getById(dishId);
                Integer quantity = dish.getQuantity();
                if (quantity < number) {
                    return new Status(false,dishId);
                }
                //如果菜品份数够，就减去此份数
                dish.setQuantity(quantity - number);
                boolean res = dishService.updateById(dish);
                //如果修改不成功，表示和其它用户修改发生了冲突，此时再次获取菜品数量比较，然后修改
                while (!res) {
                    dish = dishService.getById(dishId);
                    quantity = dish.getQuantity();
                    if (quantity < number) {
                        return new Status(false,dishId);
                    }
                    //如果菜品份数够，就减去此份数
                    dish.setQuantity(quantity - number);
                    res = dishService.updateById(dish);
                }
            }

            this.save(orderDetail);
        }
        return new Status(true,null);
    }
}