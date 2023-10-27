package com.yufish.yijiu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yufish.yijiu.entity.CategoryPO;

public interface CategoryService extends IService<CategoryPO> {

    public void remove(Long id);

}
