package com.hmdp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.entity.User;

import javax.servlet.http.HttpSession;


public interface IUserService extends IService<User> {



    Result login(LoginFormDTO loginForm);

    Result sendCode(String phone);

    Result sign();

    Result signCount();

    Result loginout();
}
