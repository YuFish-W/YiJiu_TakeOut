package com.yufish.yijiu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yufish.yijiu.common.BaseContext;
import com.yufish.yijiu.common.Result;
import com.yufish.yijiu.entity.ShoppingCartPO;
import com.yufish.yijiu.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 购物车
 */
@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;


    /**
     * 添加购物车
     *
     * @param shoppingCartPO
     * @return
     */
    @PostMapping("/add")
    public Result<ShoppingCartPO> add(@RequestBody ShoppingCartPO shoppingCartPO) {
        log.info("购物车数据:{}", shoppingCartPO);

        //设置用户id，指定当前是哪个用户的购物车数据
        Long currentId = BaseContext.getCurrentId();
        shoppingCartPO.setUserId(currentId);

        Long dishId = shoppingCartPO.getDishId();

        LambdaQueryWrapper<ShoppingCartPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCartPO::getUserId, currentId);

        if (dishId != null) {
            //添加到购物车的是菜品
            queryWrapper.eq(ShoppingCartPO::getDishId, dishId);

        } else {
            //添加到购物车的是套餐
            queryWrapper.eq(ShoppingCartPO::getSetmealId, shoppingCartPO.getSetmealId());
        }

        //查询当前菜品或者套餐是否在购物车中
        //SQL:select * from shopping_cart where user_id = ? and dish_id/setmeal_id = ?
        ShoppingCartPO cartServiceOne = shoppingCartService.getOne(queryWrapper);

        if (cartServiceOne != null) {
            //如果已经存在，就在原来数量基础上加一
            Integer number = cartServiceOne.getNumber();
            cartServiceOne.setNumber(number + 1);
            shoppingCartService.updateById(cartServiceOne);
        } else {
            //如果不存在，则添加到购物车，数量默认就是一
            shoppingCartPO.setNumber(1);
            shoppingCartPO.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCartPO);
            cartServiceOne = shoppingCartPO;
        }

        return Result.success(cartServiceOne);
    }

    /**
     * 查看购物车
     *
     * @return
     */
    @GetMapping("/list")
    public Result<List<ShoppingCartPO>> list() {
        log.info("查看购物车...");

        LambdaQueryWrapper<ShoppingCartPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCartPO::getUserId, BaseContext.getCurrentId());
        queryWrapper.orderByAsc(ShoppingCartPO::getCreateTime);

        List<ShoppingCartPO> list = shoppingCartService.list(queryWrapper);

        return Result.success(list);
    }

    /**
     * 清空购物车
     *
     * @return
     */
    @DeleteMapping("/clean")
    public Result<String> clean() {
        //SQL:delete from shopping_cart where user_id = ?

        LambdaQueryWrapper<ShoppingCartPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCartPO::getUserId, BaseContext.getCurrentId());

        shoppingCartService.remove(queryWrapper);

        return Result.success("清空购物车成功");
    }

    /**
     * 购物车商品-1，每发一次请求就该购物车商品的数量就减一，为0的时候删除
     * @param shoppingCartPO 传入的可能是套餐id，也可能是菜品id，直接用shoppingCart接收
     * @return
     */
    @PostMapping("/sub")
    public Result<ShoppingCartPO> sub(@RequestBody ShoppingCartPO shoppingCartPO) {
        //得到当前用户id
        Long currentId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCartPO> queryWrapper = new LambdaQueryWrapper<>();

        Long dishId = shoppingCartPO.getDishId();
        if (dishId == null) {
            Long setmealId = shoppingCartPO.getSetmealId();
            queryWrapper.eq(ShoppingCartPO::getUserId,currentId).eq(ShoppingCartPO::getSetmealId,setmealId);
        }else {
            queryWrapper.eq(ShoppingCartPO::getUserId,currentId).eq(ShoppingCartPO::getDishId,dishId);
        }
        //得到当前用户该套餐id的购物车对象
        shoppingCartPO = shoppingCartService.getOne(queryWrapper);

        shoppingCartPO.setNumber(shoppingCartPO.getNumber() - 1);
        //如果数量减一之后为0就删除该对象
        if (shoppingCartPO.getNumber() == 0) {
            shoppingCartService.removeById(shoppingCartPO.getId());
        } else {
            shoppingCartService.updateById(shoppingCartPO);
        }
        return Result.success(shoppingCartPO);
    }
}