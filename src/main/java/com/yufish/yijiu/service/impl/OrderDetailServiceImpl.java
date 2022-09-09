package com.yufish.yijiu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yufish.yijiu.entity.OrderDetail;
import com.yufish.yijiu.mapper.OrderDetailMapper;
import com.yufish.yijiu.service.OrderDetailService;
import org.springframework.stereotype.Service;

@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {

}