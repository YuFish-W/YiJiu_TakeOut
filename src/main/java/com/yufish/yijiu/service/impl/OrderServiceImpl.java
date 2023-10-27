package com.yufish.yijiu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yufish.yijiu.common.BaseContext;
import com.yufish.yijiu.common.CustomException;
import com.yufish.yijiu.entity.*;
import com.yufish.yijiu.mapper.OrderMapper;
import com.yufish.yijiu.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderMapper, OrdersPO> implements OrderService {

    private final ShoppingCartService shoppingCartService;

    private final UserService userService;

    private final AddressBookService addressBookService;

    private final OrderDetailService orderDetailService;

    private final DishService dishService;

    @Autowired
    public OrderServiceImpl(ShoppingCartService shoppingCartService,
                            UserService userService,
                            AddressBookService addressBookService,
                            OrderDetailService orderDetailService,
                            DishService dishService) {
        this.shoppingCartService = shoppingCartService;
        this.userService = userService;
        this.addressBookService = addressBookService;
        this.orderDetailService = orderDetailService;
        this.dishService = dishService;
    }

    /**
     * 用户下单，如果当前订单需要的菜品中有数量不足的，抛出异常，数据不提交
     *
     * @param ordersPO
     */
    @Transactional
    public void submit(OrdersPO ordersPO) {
        //获得当前用户id
        Long userId = BaseContext.getCurrentId();

        //查询当前用户的购物车数据
        LambdaQueryWrapper<ShoppingCartPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCartPO::getUserId, userId);
        List<ShoppingCartPO> shoppingCartPOS = shoppingCartService.list(wrapper);

        if (shoppingCartPOS == null || shoppingCartPOS.size() == 0) {
            throw new CustomException("购物车为空，不能下单");
        }

        //查询用户数据
        UserPO userPO = userService.getById(userId);

        //查询地址数据
        Long addressBookId = ordersPO.getAddressBookId();
        AddressBookPO addressBookPO = addressBookService.getById(addressBookId);
        if (addressBookPO == null) {
            throw new CustomException("用户地址信息有误，不能下单");
        }

        long orderId = IdWorker.getId();//订单号，表中的number字段，同时将主键设置成和它一样的

        AtomicInteger amount = new AtomicInteger(0);

        List<OrderDetailPO> orderDetailPOS = shoppingCartPOS
                .stream()
                .map(item -> {
                    OrderDetailPO orderDetailPO = new OrderDetailPO();
                    orderDetailPO.setOrderId(orderId);
                    orderDetailPO.setNumber(item.getNumber());
                    orderDetailPO.setDishFlavor(item.getDishFlavor());
                    orderDetailPO.setDishId(item.getDishId());
                    orderDetailPO.setSetmealId(item.getSetmealId());
                    orderDetailPO.setName(item.getName());
                    orderDetailPO.setImage(item.getImage());
                    orderDetailPO.setAmount(item.getAmount());
                    amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
                    return orderDetailPO;
                })
                .collect(Collectors.toList());


        ordersPO.setId(orderId);
        ordersPO.setOrderTime(LocalDateTime.now());
        ordersPO.setCheckoutTime(LocalDateTime.now());
        //状态2表示 正在派送
        ordersPO.setStatus(2);
        ordersPO.setAmount(new BigDecimal(amount.get()));//总金额
        ordersPO.setUserId(userId);
        ordersPO.setNumber(String.valueOf(orderId));
        ordersPO.setUserName(userPO.getName());
        ordersPO.setConsignee(addressBookPO.getConsignee());
        ordersPO.setPhone(addressBookPO.getPhone());
        ordersPO.setAddress((addressBookPO.getProvinceName() == null ? "" : addressBookPO.getProvinceName())
                + (addressBookPO.getCityName() == null ? "" : addressBookPO.getCityName())
                + (addressBookPO.getDistrictName() == null ? "" : addressBookPO.getDistrictName())
                + (addressBookPO.getDetail() == null ? "" : addressBookPO.getDetail()));
        //向订单表插入数据，一条数据
        this.save(ordersPO);

        //向订单明细表插入数据，多条数据
        OrderDetailService.Status status = orderDetailService.MySaveBatch(orderDetailPOS);
        if (!status.isRes()) {
            Long dishId = status.getDishId();
            String name = dishService.getById(dishId).getName();
            throw new CustomException(name + " 数量不足，请换一盘菜");
        }

        //清空购物车数据
        shoppingCartService.remove(wrapper);
    }
}