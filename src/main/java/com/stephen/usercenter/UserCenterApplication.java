package com.stephen.usercenter;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author stephen qiu
 */
@SpringBootApplication
@MapperScan("com.stephen.usercenter.mapper")
@EnableKnife4j
public class UserCenterApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(UserCenterApplication.class, args);
	}
	
}
