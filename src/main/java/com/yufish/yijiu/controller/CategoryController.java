package com.yufish.yijiu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yufish.yijiu.common.Result;
import com.yufish.yijiu.entity.CategoryPO;
import com.yufish.yijiu.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类管理
 */
@RestController
@RequestMapping("/category")
@Slf4j
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 新增分类
     * @param categoryPO
     * @return
     */
    @PostMapping
    public Result<String> save(@RequestBody CategoryPO categoryPO){
        log.info("category:{}", categoryPO);
        categoryService.save(categoryPO);
        return Result.success("新增分类成功");
    }

    /**
     * 分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public Result<Page> page(int page, int pageSize){
        //分页构造器
        Page<CategoryPO> pageInfo = new Page<>(page,pageSize);
        //条件构造器
        LambdaQueryWrapper<CategoryPO> queryWrapper = new LambdaQueryWrapper<>();
        //添加排序条件，根据sort进行排序
        queryWrapper.orderByAsc(CategoryPO::getSort);

        //分页查询
        categoryService.page(pageInfo,queryWrapper);
        return Result.success(pageInfo);
    }

    /**
     * 根据id删除分类
     * @param id
     * @return
     */
    @DeleteMapping
    public Result<String> delete(Long id){
        log.info("删除分类，id为：{}",id);

        //categoryService.removeById(id);
        categoryService.remove(id);

        return Result.success("分类信息删除成功");
    }

    /**
     * 根据id修改分类信息
     * @param categoryPO
     * @return
     */
    @PutMapping
    public Result<String> update(@RequestBody CategoryPO categoryPO){
        log.info("修改分类信息：{}", categoryPO);

        categoryService.updateById(categoryPO);

        return Result.success("修改分类信息成功");
    }

    /**
     * 根据条件查询分类数据
     * @param categoryPO
     * @return
     */
    @GetMapping("/list")
    public Result<List<CategoryPO>> list(CategoryPO categoryPO){
        //条件构造器
        LambdaQueryWrapper<CategoryPO> queryWrapper = new LambdaQueryWrapper<>();
        //添加条件
        queryWrapper.eq(categoryPO.getType() != null, CategoryPO::getType, categoryPO.getType());
        //添加排序条件
        queryWrapper.orderByAsc(CategoryPO::getSort).orderByDesc(CategoryPO::getUpdateTime);

        List<CategoryPO> list = categoryService.list(queryWrapper);
        return Result.success(list);
    }
}
