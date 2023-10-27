package com.yufish.yijiu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yufish.yijiu.entity.EmployeePO;
import com.yufish.yijiu.mapper.EmployeeMapper;
import com.yufish.yijiu.service.EmployeeService;
import org.springframework.stereotype.Service;

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, EmployeePO> implements EmployeeService{
}
