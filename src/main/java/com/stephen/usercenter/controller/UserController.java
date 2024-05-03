package com.stephen.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.stephen.usercenter.common.BaseResponse;
import com.stephen.usercenter.common.ErrorCode;
import com.stephen.usercenter.common.ResultUtils;
import com.stephen.usercenter.exception.BusinessException;
import com.stephen.usercenter.model.domain.User;
import com.stephen.usercenter.model.domain.request.UserLoginRequest;
import com.stephen.usercenter.model.domain.request.UserRegisterRequest;
import com.stephen.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

import static com.stephen.usercenter.constant.UserConstant.ADMIN_ROLE;
import static com.stephen.usercenter.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户控制器
 *
 * @author: stephen qiu
 * @create: 2023-12-02 19:51
 **/
@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {
	
	@Resource
	private UserService userService;
	
	/**
	 * 用户注册
	 * RequestBody SpringMVC匹配JSON字符串
	 *
	 * @param userRegisterRequest 自己封装的处理前端JSON的请求对象
	 * @return
	 */
	@PostMapping("/register")
	public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
		if (userRegisterRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		
		String userAccount = userRegisterRequest.getUserAccount();
		String userPassword = userRegisterRequest.getUserPassword();
		String checkPassword = userRegisterRequest.getCheckPassword();
		String studentNumber = userRegisterRequest.getStudentNumber();
		if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, studentNumber)) {
			return null;
		}
		long result = userService.userRegister(userAccount, userPassword, checkPassword, studentNumber);
		
		return ResultUtils.success(result);
	}
	
	/**
	 * 用户登录
	 *
	 * @param userLoginRequest
	 * @param request
	 * @return
	 */
	@PostMapping("/login")
	public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
		if (userLoginRequest == null) {
			return ResultUtils.error(ErrorCode.PARAMS_ERROR);
		}
		
		String userAccount = userLoginRequest.getUserAccount();
		String userPassword = userLoginRequest.getUserPassword();
		
		if (StringUtils.isAnyBlank(userAccount, userPassword)) {
			return ResultUtils.error(ErrorCode.PARAMS_ERROR);
		}
		User user = userService.userLogin(userAccount, userPassword, request);
		
		return ResultUtils.success(user);
	}
	
	/**
	 * 用户注销
	 *
	 * @param request
	 * @return
	 */
	@PostMapping("/logout")
	public BaseResponse<Integer> userLogout(@RequestBody HttpServletRequest request) {
		if (request == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		
		int result = userService.userLogout(request);
		return ResultUtils.success(result);
	}
	
	
	/**
	 * 获取当前用户
	 *
	 * @param request
	 * @return
	 */
	@GetMapping("/current")
	public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
		// 得到用户的登录态
		User currentUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
		if (currentUser == null) {
			throw new BusinessException(ErrorCode.NOT_LOGIN);
		}
		
		long userId = currentUser.getId();
		User user = userService.getById(userId);
		User safetyUser = userService.getSafetyUser(user);
		return ResultUtils.success(safetyUser);
	}
	
	
	/**
	 * 查询用户名
	 *
	 * @param username 用户名
	 * @return user对象
	 */
	@GetMapping("/search")
	public BaseResponse<List<User>> searchUsers(String username, HttpServletRequest request) {
		if (!isAdmin(request)) {
			throw new BusinessException(ErrorCode.NO_AUTH, "缺少管理员权限");
		}
		
		QueryWrapper<User> queryWrapper = new QueryWrapper<>();
		
		if (StringUtils.isNotBlank(username)) {
			queryWrapper.like("username", username);
		}
		// 设置脱敏 返回给前端的时候不带上密码
		List<User> userList = userService.list(queryWrapper);
		List<User> list = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
		return ResultUtils.success(list);
	}
	
	/**
	 * Mybatis-plus 开启了逻辑删除之后就会自动使用逻辑删除
	 *
	 * @param id
	 * @return
	 */
	@PostMapping("/delete")
	public BaseResponse<Boolean> deleteUser(@RequestBody long id, HttpServletRequest request) {
		
		if (!isAdmin(request)) {
			throw new BusinessException(ErrorCode.NO_AUTH);
		}
		
		if (id <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		boolean result = userService.removeById(id);
		return ResultUtils.success(result);
	}
	
	/**
	 * 是否为管理员
	 *
	 * @param request
	 * @return
	 */
	public boolean isAdmin(HttpServletRequest request) {
		// 身份鉴权  仅管理员可查询
		
		// 拿到了用户信息
		User user = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
		return user != null && user.getUserRole() == ADMIN_ROLE;
	}
	
	
}
