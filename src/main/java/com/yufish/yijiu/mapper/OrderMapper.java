package com.yufish.yijiu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yufish.yijiu.entity.OrdersPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<OrdersPO> {

}