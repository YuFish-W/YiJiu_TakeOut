package com.yufish.yijiu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yufish.yijiu.common.BaseContext;
import com.yufish.yijiu.common.Result;
import com.yufish.yijiu.entity.UserPO;
import com.yufish.yijiu.service.UserService;
import com.yufish.yijiu.utils.EmailUtils;
import com.yufish.yijiu.utils.ValidateCodeUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;


import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@RestController
@RequestMapping("/user")
@Slf4j
@Api(tags = "用户相关接口")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 接收传过来的{email=''}对象
     * @param map
     * @return
     */
    @PostMapping("/sendMsg")
    @ApiOperation(value = "发送验证码接口")
    public Result<String> sendMsg(/*@RequestBody User user,*/@RequestBody Map map){
        //获取手机号
//        String phone = user.getPhone();

        String  email = (String)map.get("email");
        if(StringUtils.isNotEmpty(email)){
            //从redis中取出验证码，看是否存在
            Object codeInRedis = redisTemplate.opsForValue().get(email);
            if (codeInRedis!=null){
                Long expire = redisTemplate.opsForValue().getOperations().getExpire(email);
                return Result.error("验证码已经发送，" + expire + "秒后可再次请求");
            }
            //生成随机的4位验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("code={}",code);
            EmailUtils.sendMessages(email,code);

            //调用阿里云提供的短信服务API完成发送短信
            //SMSUtils.sendMessage("一酒外卖","",phone,code);

            //需要将生成的验证码保存到Session
            //session.setAttribute(phone,code);

            //将生成的验证码缓存到Redis中，并且设置有效期为5分钟
//            redisTemplate.opsForValue().set(phone,code,5,TimeUnit.MINUTES);
            redisTemplate.opsForValue().set(email,code,60,TimeUnit.SECONDS);

            return Result.success("手机验证码短信发送成功");
        }

        return Result.error("短信发送失败");
    }

    /**
     * 移动端用户登录
     * @param map 用来接收请求参数，这里分别是手机号和验证码
     * @param session
     * @return
     */
    @PostMapping("/login")
    @ApiOperation(value = "登陆接口")
    public Result<UserPO> login(@RequestBody Map map, HttpSession session){
        log.info(map.toString());

        //获取手机号
        String phone = map.get("phone").toString();

        //获取验证码
        String code = map.get("code").toString();

        //获取邮箱
        String email = map.get("email").toString();

        //从Session中获取保存的验证码
        //Object codeInSession = session.getAttribute(phone);

        //从Redis中获取缓存的验证码
        Object codeInSession = redisTemplate.opsForValue().get(email);

        //进行验证码的比对（页面提交的验证码和Session中保存的验证码比对）
        if(codeInSession != null && codeInSession.equals(code)){
            //如果能够比对成功，说明登录成功

            LambdaQueryWrapper<UserPO> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserPO::getPhone,phone);
            queryWrapper.eq(UserPO::getEmail,email);

            UserPO userPO = userService.getOne(queryWrapper);
            if(userPO == null){
                //判断当前手机号对应的用户是否为新用户，如果是新用户就自动完成注册
                userPO = new UserPO();
                userPO.setPhone(phone);
                userPO.setEmail(email);
                userPO.setStatus(1);
                userService.save(userPO);
            }
            session.setAttribute("user", userPO.getId());

            //如果用户登录成功，删除Redis中缓存的验证码
            redisTemplate.delete(email);

            return Result.success(userPO);
        }
        return Result.error("验证码错误");
    }

    /**
     * 退出登录
     * @param session 获取用户登陆信息
     * @return 返回自定义状态类
     */
    @PostMapping("/loginout")
    @ApiOperation(value = "登出接口")
    public Result<String> loginout(HttpSession session){
        session.removeAttribute("user");
        BaseContext.remove();
        return Result.success("登出成功");
    }
}
