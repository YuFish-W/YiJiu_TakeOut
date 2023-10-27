package com.yufish.yijiu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yufish.yijiu.entity.ShoppingCartPO;
import com.yufish.yijiu.mapper.ShoppingCartMapper;
import com.yufish.yijiu.service.ShoppingCartService;
import org.springframework.stereotype.Service;

@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCartPO> implements ShoppingCartService {

}
