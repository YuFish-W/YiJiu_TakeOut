package com.yufish.yijiu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yufish.yijiu.dto.SetmealDTO;
import com.yufish.yijiu.entity.SetmealPO;

import java.util.List;

public interface SetmealService extends IService<SetmealPO> {
    /**
     * 新增套餐，同时需要保存套餐和菜品的关联关系
     * @param setmealDto
     */
    public void saveWithDish(SetmealDTO setmealDto);

    /**
     * 修改套餐，同时保存和产品的关联关系
     * @param setmealDto
     */
    public void updateWithDish(SetmealDTO setmealDto);

    /**
     * 删除套餐，同时需要删除套餐和菜品的关联数据
     * @param ids
     */
    public void removeWithDish(List<Long> ids);

    public SetmealDTO getWithDish(Long id);
}
