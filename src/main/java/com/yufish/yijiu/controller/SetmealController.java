package com.yufish.yijiu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yufish.yijiu.common.Result;
import com.yufish.yijiu.dto.SetmealDTO;
import com.yufish.yijiu.entity.CategoryPO;
import com.yufish.yijiu.entity.SetmealPO;
import com.yufish.yijiu.service.CategoryService;
import com.yufish.yijiu.service.SetmealDishService;
import com.yufish.yijiu.service.SetmealService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 套餐管理
 */

@RestController
@RequestMapping("/setmeal")
@Slf4j
@Api(tags = "套餐相关接口")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 新增套餐
     * @param setmealDto
     * @return
     */
    @PostMapping
    @CacheEvict(value = "setmealCache",allEntries = true)
    @ApiOperation(value = "新增套餐接口")
    public Result<String> save(@RequestBody SetmealDTO setmealDto){
        log.info("套餐信息：{}",setmealDto);

        setmealService.saveWithDish(setmealDto);

        return Result.success("新增套餐成功");
    }

    @PutMapping
    @CacheEvict(value = "setmealCache",key = "#setmealDto.categoryId + '_' + #setmealDto.status")
    @ApiOperation(value = "修改套餐接口")
    public Result<String> update(@RequestBody SetmealDTO setmealDto){
        log.info("套餐信息：{}",setmealDto);

        setmealService.updateWithDish(setmealDto);

        return Result.success("套餐修改成功");
    }

    /**
     * 套餐分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    @ApiOperation(value = "套餐分页查询接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page",value = "页码",required = true),
            @ApiImplicitParam(name = "pageSize",value = "每页记录数",required = true),
            @ApiImplicitParam(name = "name",value = "套餐名称",required = false)
    })
    public Result<Page> page(int page, int pageSize, String name){
        //分页构造器对象
        Page<SetmealPO> pageInfo = new Page<>(page,pageSize);
        Page<SetmealDTO> dtoPage = new Page<>();

        LambdaQueryWrapper<SetmealPO> queryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件，根据name进行like模糊查询
        queryWrapper.like(name != null, SetmealPO::getName,name);
        //添加排序条件，根据更新时间降序排列
        queryWrapper.orderByDesc(SetmealPO::getUpdateTime);

        setmealService.page(pageInfo,queryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(pageInfo,dtoPage,"records");
        List<SetmealPO> records = pageInfo.getRecords();

        List<SetmealDTO> list = records.stream().map((item) -> {
            SetmealDTO setmealDto = new SetmealDTO();
            //对象拷贝
            BeanUtils.copyProperties(item,setmealDto);
            //分类id
            Long categoryId = item.getCategoryId();
            //根据分类id查询分类对象
            CategoryPO categoryPO = categoryService.getById(categoryId);
            if(categoryPO != null){
                //分类名称
                String categoryName = categoryPO.getName();
                setmealDto.setCategoryName(categoryName);
            }
            return setmealDto;
        }).collect(Collectors.toList());

        dtoPage.setRecords(list);
        return Result.success(dtoPage);
    }

    /**
     * 删除套餐
     * @param ids
     * @return
     */
    @DeleteMapping
    @CacheEvict(value = "setmealCache",allEntries = true)
    @ApiOperation(value = "套餐删除接口")
    public Result<String> delete(@RequestParam List<Long> ids){
        log.info("ids:{}",ids);

        setmealService.removeWithDish(ids);

        return Result.success("套餐数据删除成功");
    }

    /**
     * 根据条件查询套餐数据,查询指定套餐分类下的所有套餐
     * @param setmealPO
     * @return
     */
    @GetMapping("/list")
    @Cacheable(value = "setmealCache",key = "#setmealPO.categoryId + '_' + #setmealPO.status")
    @ApiOperation(value = "套餐条件查询接口")
    public Result<List<SetmealPO>> list(SetmealPO setmealPO){
        LambdaQueryWrapper<SetmealPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmealPO.getCategoryId() != null, SetmealPO::getCategoryId, setmealPO.getCategoryId());
        queryWrapper.eq(setmealPO.getStatus() != null, SetmealPO::getStatus, setmealPO.getStatus());
        queryWrapper.orderByDesc(SetmealPO::getUpdateTime);

        List<SetmealPO> list = setmealService.list(queryWrapper);
        return Result.success(list);
    }

    /**
     * 根据传过来的状态码以及套餐Id集合，修改这些套餐的状态
     * @param status 状态
     * @param ids 套餐id集合
     * @return
     */
    @PostMapping("/status/{status}")
    public Result<String> status(@PathVariable("status") Integer status, @RequestParam("ids") List<Long> ids){
        for (Long id : ids) {
            SetmealPO setmealPO = new SetmealPO();
            setmealPO.setId(id);
            setmealPO.setStatus(status);
            boolean res = setmealService.updateById(setmealPO);
            if (!res){
                return Result.error("状态修改失败");
            }
        }
        return Result.success("状态修改成功");
    }

    /**
     * 根据id查找套餐以及该套餐下面的菜品
     * @param id
     * @return
     */
    @GetMapping("{id}")
    public Result<SetmealDTO> get(@PathVariable("id") Long id){
        SetmealDTO setmealDto = setmealService.getWithDish(id);
        return Result.success(setmealDto);
    }
}
