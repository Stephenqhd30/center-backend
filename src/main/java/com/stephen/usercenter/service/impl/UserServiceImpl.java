package com.stephen.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stephen.usercenter.common.ErrorCode;
import com.stephen.usercenter.exception.BusinessException;
import com.stephen.usercenter.mapper.UserMapper;
import com.stephen.usercenter.model.domain.User;
import com.stephen.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.stephen.usercenter.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户服务实现类
 *
 * @author stephen qiu
 * @description 针对表【user(用户表)】的数据库操作Service实现
 * @createDate 2023-12-02 10:50:44
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
		implements UserService {
	
	// Mapper注入
	@Resource
	private UserMapper userMapper;
	
	/**
	 * 盐值 混淆密码
	 */
	private static final String SALT = "Popcorn";
	
	/**
	 * 用户注册
	 *
	 * @param userAccount   用户账户
	 * @param userPassword  用户密码
	 * @param checkPassword 校验密码
	 * @param studentNumber 学号
	 * @return
	 */
	@Override
	public long userRegister(String userAccount, String userPassword, String checkPassword, String studentNumber) {
		// 1. 校验
		if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, studentNumber)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
		}
		// 校验用户账户的长度不小于4
		if (userAccount.length() < 4) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
			
		}
		// 校验密码的长度不小于8
		if (userPassword.length() < 8 || checkPassword.length() < 8) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
		}
		
		// 对学号进行校验
		if (studentNumber.length() > 10) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "学生学号有误");
		}
		
		// 账户不能包含特殊字符 [validPattern 有效模式]
		String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
		Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
		if (matcher.find()) {
			return -1;
		}
		// 密码和校验密码相同
		if (!userPassword.equals(checkPassword)) {
			return -1;
		}
		
		// 校验 用户不重复
		QueryWrapper<User> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("userAccount", userAccount);
		long count = userMapper.selectCount(queryWrapper);
		if (count > 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
		}
		
		// 校验 学号不能重复
		queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("studentNumber", studentNumber);
		count = userMapper.selectCount(queryWrapper);
		if (count > 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "学号重复");
		}
		
		// 2. 密码的加密 [单向的加密]
		String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
		
		// 3. 向用户数据库插入数据
		User user = new User();
		user.setUserAccount(userAccount);
		user.setUserPassword(encryptPassword);
		user.setStudentNumber(studentNumber);
		boolean saveResult = this.save(user);
		
		if (!saveResult) {
			return -1;
		}
		
		return user.getId();
	}
	
	/**
	 * 用户登录
	 *
	 * @param userAccount  用户账户
	 * @param userPassword 用户密码
	 * @param request
	 * @return 脱敏后的用户信息
	 */
	@Override
	public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
		// 1. 校验
		if (StringUtils.isAnyBlank(userAccount, userPassword)) {
			return null;
		}
		if (userAccount.length() < 4) {
			return null;
		}
		if (userPassword.length() < 8) {
			return null;
		}
		// 账户不能包含特殊字符
		String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
		Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
		if (matcher.find()) {
			return null;
		}
		
		// 2. 加密
		String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
		// 查询用户是否存在
		QueryWrapper<User> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("userAccount", userAccount);
		queryWrapper.eq("userPassword", encryptPassword);
		User user = userMapper.selectOne(queryWrapper);
		// 用户不存在
		if (user == null) {
			log.info("user login failed, userAccount cannot match userPassword");
			return null;
		}
		// 3. 用户脱敏
		User safetyUser = getSafetyUser(user);
		// 4. 记录用户的登录态
		request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
		return safetyUser;
	}
	
	
	/**
	 * 用户脱敏
	 *
	 * @param originUser
	 * @return
	 */
	@Override
	public User getSafetyUser(User originUser) {
		if (originUser == null) {
			return null;
		}
		User safetyUser = new User();
		safetyUser.setId(originUser.getId());
		safetyUser.setUsername(originUser.getUsername());
		safetyUser.setUserAccount(originUser.getUserAccount());
		safetyUser.setAvatarUrl(originUser.getAvatarUrl());
		safetyUser.setGender(originUser.getGender());
		safetyUser.setPhone(originUser.getPhone());
		safetyUser.setEmail(originUser.getEmail());
		safetyUser.setUserStatus(originUser.getUserStatus());
		safetyUser.setCreateTime(originUser.getCreateTime());
		safetyUser.setUserRole(originUser.getUserRole());
		safetyUser.setStudentNumber(originUser.getStudentNumber());
		
		return safetyUser;
	}
	
	/**
	 * 用户注销
	 *
	 * @param request
	 */
	@Override
	public int userLogout(HttpServletRequest request) {
		// 移除登录态
		request.getSession().removeAttribute(USER_LOGIN_STATE);
		return 1;
	}
}




