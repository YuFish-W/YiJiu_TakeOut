package com.yufish.yijiu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yufish.yijiu.common.CustomException;
import com.yufish.yijiu.dto.SetmealDTO;
import com.yufish.yijiu.entity.CategoryPO;
import com.yufish.yijiu.entity.SetmealDishPO;
import com.yufish.yijiu.entity.SetmealPO;
import com.yufish.yijiu.mapper.SetmealMapper;
import com.yufish.yijiu.service.CategoryService;
import com.yufish.yijiu.service.SetmealDishService;
import com.yufish.yijiu.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, SetmealPO> implements SetmealService {

    private final SetmealDishService setmealDishService;

    private final CategoryService categoryService;

    @Autowired
    public SetmealServiceImpl(SetmealDishService setmealDishService,
                              CategoryService categoryService) {
        this.setmealDishService = setmealDishService;
        this.categoryService = categoryService;
    }

    /**
     * 新增套餐，同时需要保存套餐和菜品的关联关系
     * @param setmealDto
     */
    @Transactional
    public void saveWithDish(SetmealDTO setmealDto) {
        //保存套餐的基本信息，操作setmeal，执行insert操作
        this.save(setmealDto);

        List<SetmealDishPO> setmealDishPOS = setmealDto.getSetmealDishPOS();
        setmealDishPOS.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        //保存套餐和菜品的关联信息，操作setmeal_dish,执行insert操作
        setmealDishService.saveBatch(setmealDishPOS);
    }

    /**
     * 修改套餐，同时保存和产品的关联关系
     * @param setmealDto
     */
    @Transactional
    public void updateWithDish(SetmealDTO setmealDto) {
        this.updateById(setmealDto);

        List<SetmealDishPO> setmealDishPOS = setmealDto.getSetmealDishPOS();
        //先清除原先的菜品
        LambdaQueryWrapper<SetmealDishPO> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(SetmealDishPO::getSetmealId,setmealDto.getId());
        setmealDishService.remove(queryWrapper);
        //把现在的菜品加入
        setmealDishPOS.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        setmealDishService.saveBatch(setmealDishPOS);
    }

    /**
     * 删除套餐，同时需要删除套餐和菜品的关联数据
     * @param ids
     */
    @Transactional
    public void removeWithDish(List<Long> ids) {
        //select count(*) from setmeal where id in (1,2,3) and status = 1
        //查询套餐状态，确定是否可用删除
        LambdaQueryWrapper<SetmealPO> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.in(SetmealPO::getId,ids);
        queryWrapper.eq(SetmealPO::getStatus,1);

        int count = this.count(queryWrapper);
        if(count > 0){
            //如果不能删除，抛出一个业务异常
            throw new CustomException("套餐正在售卖中，不能删除");
        }

        //如果可以删除，先删除套餐表中的数据---setmeal
        this.removeByIds(ids);

        //delete from setmeal_dish where setmeal_id in (1,2,3)
        LambdaQueryWrapper<SetmealDishPO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(SetmealDishPO::getSetmealId,ids);
        //删除关系表中的数据----setmeal_dish
        setmealDishService.remove(lambdaQueryWrapper);
    }

    /**
     * 根据id返回套餐，带上该套餐中的各种菜品
     * @param id
     * @return
     */
    public SetmealDTO getWithDish(Long id){
        //得到套餐类，根据它的ID找出所有菜品
        SetmealPO setmealPO = this.getById(id);

        SetmealDTO setmealDto = new SetmealDTO();
        BeanUtils.copyProperties(setmealPO,setmealDto);

        LambdaQueryWrapper<SetmealDishPO> queryWrapper = new LambdaQueryWrapper();

        queryWrapper.eq(SetmealDishPO::getSetmealId,id);
        List<SetmealDishPO> list = setmealDishService.list(queryWrapper);
        setmealDto.setSetmealDishPOS(list);
        //带上套餐名字，其实不用查出来，前端自己有记录
        CategoryPO categoryPO = categoryService.getById(setmealPO.getCategoryId());
        setmealDto.setCategoryName(categoryPO.getName());
        return setmealDto;
    }
}
