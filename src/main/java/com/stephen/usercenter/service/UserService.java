package com.stephen.usercenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.stephen.usercenter.model.domain.User;

import javax.servlet.http.HttpServletRequest;

/**
 * @author stephen qiu
 * @description 针对表【user(用户表)】的数据库操作Service
 * @createDate 2024-04-21 18:27:58
 */


/**
 * @author stephen qiu
 * @description 针对表【user(用户表)】的数据库操作Service
 * @createDate 2024-04-21 18:27:58
 */
public interface UserService extends IService<User> {
	
	long userRegister(String userAccount, String userPassword, String checkPassword, String studentNumber);
	
	User userLogin(String userAccount, String userPassword, HttpServletRequest request);
	
	User getSafetyUser(User originUser);
	
	int userLogout(HttpServletRequest request);
}

