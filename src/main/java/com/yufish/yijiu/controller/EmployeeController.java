package com.yufish.yijiu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yufish.yijiu.common.BaseContext;
import com.yufish.yijiu.common.Result;
import com.yufish.yijiu.entity.EmployeePO;
import com.yufish.yijiu.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    /**
     * 员工登录
     * @param request
     * @param employeePO
     * @return
     */
    @PostMapping("/login")
    public Result<EmployeePO> login(HttpServletRequest request, @RequestBody EmployeePO employeePO){
        //1、将页面提交的密码password进行md5加密处理
        String password = employeePO.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //2、根据页面提交的用户名username查询数据库
        LambdaQueryWrapper<EmployeePO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EmployeePO::getUsername, employeePO.getUsername());
        EmployeePO emp = employeeService.getOne(queryWrapper);

        //3、如果没有查询到则返回登录失败结果
        if(emp == null){
            return Result.error("登录失败");
        }

        //4、密码比对，如果不一致则返回登录失败结果
        if(!emp.getPassword().equals(password)){
            return Result.error("登录失败");
        }

        //5、查看员工状态，如果为已禁用状态，则返回员工已禁用结果
        if(emp.getStatus() == 0){
            return Result.error("账号已禁用");
        }

        //6、登录成功，将员工id存入Session并返回登录成功结果
        request.getSession().setAttribute("employee",emp.getId());
        return Result.success(emp);
    }

    /**
     * 员工退出
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request){
        //清理Session中保存的当前登录员工的id
        request.getSession().removeAttribute("employee");
        BaseContext.remove();
        return Result.success("退出成功");
    }

    /**
     * 新增员工
     * @param employeePO
     * @return
     */
    @PostMapping
    public Result<String> save(HttpServletRequest request, @RequestBody EmployeePO employeePO){
        log.info("新增员工，员工信息：{}", employeePO.toString());

        //设置初始密码123456，需要进行md5加密处理
        employeePO.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        //employee.setCreateTime(LocalDateTime.now());
        //employee.setUpdateTime(LocalDateTime.now());

        //获得当前登录用户的id
        //Long empId = (Long) request.getSession().getAttribute("employee");

        //employee.setCreateUser(empId);
        //employee.setUpdateUser(empId);

        employeeService.save(employeePO);

        return Result.success("新增员工成功");
    }

    /**
     * 员工信息分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public Result<Page> page(int page, int pageSize, String name){
        log.info("page = {},pageSize = {},name = {}" ,page,pageSize,name);

        //构造分页构造器
        Page pageInfo = new Page(page,pageSize);

        //构造条件构造器
        LambdaQueryWrapper<EmployeePO> queryWrapper = new LambdaQueryWrapper();
        //添加过滤条件
        queryWrapper.like(StringUtils.isNotEmpty(name), EmployeePO::getName,name);
        //添加排序条件
        queryWrapper.orderByDesc(EmployeePO::getUpdateTime);

        //执行查询
        employeeService.page(pageInfo,queryWrapper);

        return Result.success(pageInfo);
    }

    /**
     * 根据id修改员工信息
     * @param employeePO
     * @return
     */
    @PutMapping
    public Result<String> update(HttpServletRequest request, @RequestBody EmployeePO employeePO){
        log.info(employeePO.toString());

        long id = Thread.currentThread().getId();
        log.info("线程id为：{}",id);
        //Long empId = (Long)request.getSession().getAttribute("employee");
        //employee.setUpdateTime(LocalDateTime.now());
        //employee.setUpdateUser(empId);
        employeeService.updateById(employeePO);

        return Result.success("员工信息修改成功");
    }

    /**
     * 根据id查询员工信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<EmployeePO> getById(@PathVariable Long id){
        log.info("根据id查询员工信息...");
        EmployeePO employeePO = employeeService.getById(id);
        if(employeePO != null){
            return Result.success(employeePO);
        }
        return Result.error("没有查询到对应员工信息");
    }
}
