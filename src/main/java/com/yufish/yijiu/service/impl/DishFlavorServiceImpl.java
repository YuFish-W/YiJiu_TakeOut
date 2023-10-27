package com.yufish.yijiu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yufish.yijiu.entity.DishFlavorPO;
import com.yufish.yijiu.mapper.DishFlavorMapper;
import com.yufish.yijiu.service.DishFlavorService;
import org.springframework.stereotype.Service;

@Service
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMapper, DishFlavorPO> implements DishFlavorService {
}
