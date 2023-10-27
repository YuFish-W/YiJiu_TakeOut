package com.yufish.yijiu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yufish.yijiu.common.Result;
import com.yufish.yijiu.dto.DishDTO;
import com.yufish.yijiu.entity.CategoryPO;
import com.yufish.yijiu.entity.DishPO;
import com.yufish.yijiu.entity.DishFlavorPO;
import com.yufish.yijiu.service.CategoryService;
import com.yufish.yijiu.service.DishFlavorService;
import com.yufish.yijiu.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 菜品管理
 */
@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 新增菜品
     *
     * @param dishDto
     * @return
     */
    @PostMapping
    public Result<String> save(@RequestBody DishDTO dishDto) {
        log.info(dishDto.toString());

        dishService.saveWithFlavor(dishDto);

        //清理所有菜品的缓存数据
        //Set keys = redisTemplate.keys("dish_*");
        //redisTemplate.delete(keys);

        //清理某个分类下面的菜品缓存数据
        String key = "dish_" + dishDto.getCategoryId() + "_1";
        redisTemplate.delete(key);

        return Result.success("新增菜品成功");
    }

    /**
     * 菜品信息分页查询
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public Result<Page> page(int page, int pageSize, String name) {

        //构造分页构造器对象
        Page<DishPO> pageInfo = new Page<>(page, pageSize);
        Page<DishDTO> dishDtoPage = new Page<>();

        //条件构造器
        LambdaQueryWrapper<DishPO> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件
        queryWrapper.like(name != null, DishPO::getName, name);
        //添加排序条件
        queryWrapper.orderByDesc(DishPO::getUpdateTime);

        //执行分页查询
        dishService.page(pageInfo, queryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(pageInfo, dishDtoPage, "records");

        List<DishPO> records = pageInfo.getRecords();

        List<DishDTO> list = records.stream().map((item) -> {
            DishDTO dishDto = new DishDTO();

            BeanUtils.copyProperties(item, dishDto);

            Long categoryId = item.getCategoryId();//分类id
            //根据id查询分类对象
            CategoryPO categoryPO = categoryService.getById(categoryId);

            if (categoryPO != null) {
                String categoryName = categoryPO.getName();
                dishDto.setCategoryName(categoryName);
            }
            return dishDto;
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(list);

        return Result.success(dishDtoPage);
    }

    /**
     * 根据id查询菜品信息和对应的口味信息
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<DishDTO> get(@PathVariable Long id) {

        DishDTO dishDto = dishService.getByIdWithFlavor(id);

        return Result.success(dishDto);
    }

    /**
     * 修改菜品
     *
     * @param dishDto
     * @return
     */
    @PutMapping
    public Result<String> update(@RequestBody DishDTO dishDto) {
        log.info(dishDto.toString());

        dishService.updateWithFlavor(dishDto);

        //清理所有菜品的缓存数据
        //Set keys = redisTemplate.keys("dish_*");
        //redisTemplate.delete(keys);

        //清理某个分类下面的菜品缓存数据
        String key = "dish_" + dishDto.getCategoryId() + "_1";
        redisTemplate.delete(key);

        return Result.success("修改菜品成功");
    }

    /**
     * 根据条件查询对应的菜品数据
     *
     * @param dishPO
     * @return
     */
    /*@GetMapping("/list")
    public R<List<Dish>> list(Dish dish){
        //构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null ,Dish::getCategoryId,dish.getCategoryId());
        //添加条件，查询状态为1（起售状态）的菜品
        queryWrapper.eq(Dish::getStatus,1);

        //添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(queryWrapper);

        return R.success(list);
    }*/
    @GetMapping("/list")
    public Result<List<DishDTO>> list(DishPO dishPO) {
        List<DishDTO> dishDtoList = null;

        //动态构造key
        String key = "dish_" + dishPO.getCategoryId() + "_" + dishPO.getStatus();//dish_1397844391040167938_1

        //先从redis中获取缓存数据
        dishDtoList = (List<DishDTO>) redisTemplate.opsForValue().get(key);

        if (dishDtoList != null) {
            //如果存在，直接返回，无需查询数据库
            return Result.success(dishDtoList);
        }

        //构造查询条件
        LambdaQueryWrapper<DishPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dishPO.getCategoryId() != null, DishPO::getCategoryId, dishPO.getCategoryId());
        //添加条件，查询状态为1（起售状态）的菜品
        queryWrapper.eq(DishPO::getStatus, 1);

        //添加排序条件
        queryWrapper.orderByAsc(DishPO::getSort).orderByDesc(DishPO::getUpdateTime);

        List<DishPO> list = dishService.list(queryWrapper);

        dishDtoList = list.stream().map((item) -> {
            DishDTO dishDto = new DishDTO();

            BeanUtils.copyProperties(item, dishDto);

            Long categoryId = item.getCategoryId();//分类id
            //根据id查询分类对象
            CategoryPO categoryPO = categoryService.getById(categoryId);

            if (categoryPO != null) {
                String categoryName = categoryPO.getName();
                dishDto.setCategoryName(categoryName);
            }

            //当前菜品的id
            Long dishId = item.getId();
            LambdaQueryWrapper<DishFlavorPO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(DishFlavorPO::getDishId, dishId);
            //SQL:select * from dish_flavor where dish_id = ?
            List<DishFlavorPO> dishFlavorPOList = dishFlavorService.list(lambdaQueryWrapper);
            dishDto.setFlavors(dishFlavorPOList);
            return dishDto;
        }).collect(Collectors.toList());

        //如果不存在，需要查询数据库，将查询到的菜品数据缓存到Redis
        redisTemplate.opsForValue().set(key, dishDtoList, 60, TimeUnit.MINUTES);

        return Result.success(dishDtoList);
    }


    /**
     * 根据传过来的菜品id和状态，将对应菜品状态码修改
     *
     * @param code 状态码
     * @param ids  菜品id
     * @return
     */
    @PostMapping("/status/{code}")
    public Result<String> updateStatus(@PathVariable("code") Integer code, @RequestParam("ids") List<Long> ids) {
//        String[] split = ids.split(",");
        for (Long id : ids) {
            DishPO dishPO = dishService.getById(id);

            if (dishPO == null) {
                return Result.error("菜品id= " + id + " 不存在");
            }

            dishPO.setStatus(code);
            //dish菜品类是有乐观锁的，如果两个人同时修改菜品的状态，可能会有冲突修改失败
            //也有可能被别人删除了
            boolean res = dishService.updateById(dishPO);
            while (!res) {
                dishPO = dishService.getById(id);
                if (dishPO == null) {
                    return Result.error("菜品id= " + id + " 不存在");
                }
                dishPO.setStatus(code);
                res = dishService.updateById(dishPO);
            }
        }
        return Result.success("状态修改成功");
    }

    /**
     * 根据id删除菜品
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    public Result<String> deleteById(@RequestParam List<Long> ids) {
//        String[] split = ids.split(",");
        for (Long id : ids) {
            DishPO dishPO = dishService.getById(id);

            if (dishPO == null) {
                return Result.error("菜品id= " + id + " 不存在");
            }

            if (dishPO.getStatus() == 1) {
                return Result.error(dishPO.getName() + " 正在销售，无法删除");
            }

            boolean res = dishService.removeById(id);
            if (!res) {
                return Result.error("菜品id= " + id + " 不存在");
            }
        }
        return Result.success("删除成功");
    }
}
