package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.lang.hash.Hash;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexUtils;
import com.hmdp.utils.UserHolder;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.*;
import static com.hmdp.utils.SystemConstants.USER_NICK_NAME_PREFIX;


@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    //添加一个线程池来记住用户登录时的token便于退出登录
    private static final ThreadLocal<String> currentToken = new ThreadLocal<>();
    /**
     * 发送验证码，通过redis来存储验证码并设置自动到期时间，节省内存
     * @param phone
     * @return
     */
    @Override
    public Result sendCode(String phone) {
        //1.校验手机号
        if (RegexUtils.isPhoneInvalid(phone)) {
            //2.不符合就返回错误信息
            return Result.fail("手机号码无效");
        }

        //3.符合就生成验证码
        String verificationCode = RandomUtil.randomNumbers(6);

        //4.保存在redis中方便后续校验
        stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY+phone,verificationCode,LOGIN_CODE_TTL, TimeUnit.SECONDS);

        //5.发送验证码
        log.debug("发送短信验证码成功，验证码：{}",verificationCode);
        return Result.ok();
    }
    //实现签到功能
    @Override
    public Result sign() {
        //获取当前登录用户
        Long userId = UserHolder.getUser().getId();
        //获取日期
        LocalDateTime now = LocalDateTime.now();
        //拼接key
        String keySuffix = now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
        String key=SIGN_KEY+userId+keySuffix;
        //获取今天是本月的第几天
        int dayOfMonth = now.getDayOfMonth();
        //写入redis
        stringRedisTemplate.opsForValue().setBit(key,dayOfMonth-1,true);
        return Result.ok();
    }
    //统计连续签到天数
    @Override
    public Result signCount() {
        //获取当前登录用户
        Long userId = UserHolder.getUser().getId();
        //获取日期
        LocalDateTime now = LocalDateTime.now();
        //拼接key
        String keySuffix = now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
        String key=SIGN_KEY+userId+keySuffix;
        //获取今天是本月的第几天
        int dayOfMonth = now.getDayOfMonth();
        //获取本月截止今天为止的签到记录，返回的是一个十进制的数字
        List<Long> result = stringRedisTemplate.opsForValue().bitField(key,
                BitFieldSubCommands.create().get(BitFieldSubCommands.BitFieldType.unsigned(dayOfMonth)).valueAt(0));
        if (result == null || result.isEmpty()) {
            return Result.ok(0);
        }
        Long num = result.get(0);
        if (num == null || num==0) {
            return Result.ok(0);
        }
        //循环遍历
        int cnt=0;
        while(true){
            //让这个数字与1做与运算，得到数字的最后一个bit位
            if((num&1)==0){
                //判断bit位是否为0，为0说明未签到，结束。
                break;
            }
            else{
                //不为0说明已签到，计数器++
                cnt++;
            }
            num>>>=1;

        }
        return Result.ok(cnt);
        //把数字右移一位，抛弃最后一个bit位
    }

    /**
     * 登出功能
     */
    @Override
    public Result loginout() {
        Long userId = UserHolder.getUser().getId();
        String token = stringRedisTemplate.opsForValue().get(USER_LOGIN_TOKEN + userId);
        stringRedisTemplate.delete(LOGIN_USER_KEY+token);
        stringRedisTemplate.delete(USER_LOGIN_TOKEN + userId);
        return Result.ok("成功退出登录！");
    }

    /**
     * 登录
     * @param loginForm
     * @return
     */
    @Override
    public Result login(LoginFormDTO loginForm) {
        String phone = loginForm.getPhone();
        //1.校验手机号
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("手机号码无效");
        }

        //2.从redis获取验证码
        String code = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY+phone);
        if(code==null || !loginForm.getCode().equals(code)){
            //3.不一致就报错
            return Result.fail("验证码不一致");
        }
        //4.一致就根据手机号来查找用户
        User user = query().eq("phone", phone).one();

        //5.查询用户是否存在
        if (user == null) {
            //6.未查询到用户则创建新的用户并保存到数据库和session中
            user = createUserWithPhone(phone);
        }
        //7.查询到了用户则登录成功并保存到redis中
        //7.1随机生成token作为登录令牌
        String token = UUID.randomUUID().toString(true);
        Long userId = user.getId();
        //将token存储进去方便后续登出
        stringRedisTemplate.opsForValue().set(USER_LOGIN_TOKEN+userId,token,LOGIN_USER_TTL,TimeUnit.MINUTES);
//        currentToken.set(token);
        //7.2将User对象转换为Hash存储在redis中
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO,new HashMap<>(),
                CopyOptions.create().
                        setIgnoreNullValue(true).
                        setFieldValueEditor((fieldName,fieldValue)->fieldValue.toString()));
        //7.3存储
        stringRedisTemplate.opsForHash().putAll(LOGIN_USER_KEY+token,userMap);
        //7.4设置token有效期
        stringRedisTemplate.expire(LOGIN_USER_KEY+token,LOGIN_USER_TTL,TimeUnit.MINUTES);
        //8.返回token
        return Result.ok(token);
    }


    /**
     * 相当于注册
     * @param phone
     * @return
     */
    private User createUserWithPhone(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setNickName(USER_NICK_NAME_PREFIX+RandomUtil.randomString(10));
        save(user);
        return user;
    }
}
