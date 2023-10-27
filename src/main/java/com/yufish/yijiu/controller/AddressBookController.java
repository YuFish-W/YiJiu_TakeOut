package com.yufish.yijiu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yufish.yijiu.common.BaseContext;
import com.yufish.yijiu.common.Result;
import com.yufish.yijiu.entity.AddressBookPO;
import com.yufish.yijiu.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 地址簿管理
 */
@Slf4j
@RestController
@RequestMapping("/addressBook")
public class AddressBookController {


    private final AddressBookService addressBookService;

    @Autowired
    public AddressBookController(AddressBookService addressBookService) {
        this.addressBookService = addressBookService;
    }

    /**
     * 新增
     */
    @PostMapping
    public Result<AddressBookPO> save(@RequestBody AddressBookPO addressBookPO) {
        addressBookPO.setUserId(BaseContext.getCurrentId());
        log.info("addressBook:{}", addressBookPO);
        addressBookService.save(addressBookPO);
        return Result.success(addressBookPO);
    }

    /**
     * 设置默认地址
     */
    @PutMapping("default")
    public Result<AddressBookPO> setDefault(@RequestBody AddressBookPO addressBookPO) {
        log.info("addressBook:{}", addressBookPO);
        LambdaUpdateWrapper<AddressBookPO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(AddressBookPO::getUserId, BaseContext.getCurrentId());
        wrapper.set(AddressBookPO::getIsDefault, 0);
        //SQL:update address_book set is_default = 0 where user_id = ?
        addressBookService.update(wrapper);

        addressBookPO.setIsDefault(1);
        //SQL:update address_book set is_default = 1 where id = ?
        addressBookService.updateById(addressBookPO);
        return Result.success(addressBookPO);
    }

    /**
     * 根据id查询地址
     */
    @GetMapping("/{id}")
    public Result get(@PathVariable Long id) {
        AddressBookPO addressBookPO = addressBookService.getById(id);
        if (addressBookPO != null) {
            return Result.success(addressBookPO);
        } else {
            return Result.error("没有找到该对象");
        }
    }

    /**
     * 查询默认地址
     */
    @GetMapping("default")
    public Result<AddressBookPO> getDefault() {
        LambdaQueryWrapper<AddressBookPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBookPO::getUserId, BaseContext.getCurrentId());
        queryWrapper.eq(AddressBookPO::getIsDefault, 1);

        //SQL:select * from address_book where user_id = ? and is_default = 1
        AddressBookPO addressBookPO = addressBookService.getOne(queryWrapper);

        if (null == addressBookPO) {
            return Result.error("没有找到该对象");
        } else {
            return Result.success(addressBookPO);
        }
    }

    /**
     * 查询指定用户的全部地址
     */
    @GetMapping("/list")
    public Result<List<AddressBookPO>> list(AddressBookPO addressBookPO) {
        addressBookPO.setUserId(BaseContext.getCurrentId());
        log.info("addressBook:{}", addressBookPO);

        //条件构造器
        LambdaQueryWrapper<AddressBookPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(null != addressBookPO.getUserId(), AddressBookPO::getUserId, addressBookPO.getUserId());
        queryWrapper.orderByDesc(AddressBookPO::getUpdateTime);

        //SQL:select * from address_book where user_id = ? order by update_time desc
        return Result.success(addressBookService.list(queryWrapper));
    }
}
