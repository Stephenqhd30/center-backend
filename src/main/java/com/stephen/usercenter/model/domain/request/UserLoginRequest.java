package com.stephen.usercenter.model.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author: stephen qiu
 * @create: 2023-12-02 20:23
 *
 * Serializable 实现序列化接口
 **/
@Data
public class UserLoginRequest implements Serializable {

  /**
   * 用户账号
   */
  private String userAccount;

  /**
   * 用户密码
   */
  private String userPassword;

}
