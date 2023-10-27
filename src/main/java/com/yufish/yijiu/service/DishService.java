package com.yufish.yijiu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yufish.yijiu.dto.DishDTO;
import com.yufish.yijiu.entity.DishPO;

public interface DishService extends IService<DishPO> {

    //新增菜品，同时插入菜品对应的口味数据，需要操作两张表：dish、dish_flavor
    public void saveWithFlavor(DishDTO dishDto);

    //根据id查询菜品信息和对应的口味信息
    public DishDTO getByIdWithFlavor(Long id);

    //更新菜品信息，同时更新对应的口味信息
    public void updateWithFlavor(DishDTO dishDto);
}
