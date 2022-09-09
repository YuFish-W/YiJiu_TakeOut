package com.yufish.yijiu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yufish.yijiu.entity.Category;

public interface CategoryService extends IService<Category> {

    public void remove(Long id);

}
