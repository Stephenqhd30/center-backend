package com.stephen.usercenter;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

@SpringBootTest
class UserCenterApplicationTests {


  @Test
  public void testDigest() {
    String newPassword = DigestUtils.md5DigestAsHex(("abcd" + "mypassword").getBytes());
    System.out.println(newPassword);

  }

  @Test
  void contextLoads() {
  }

}
