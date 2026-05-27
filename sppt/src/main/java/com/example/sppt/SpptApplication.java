package com.example.sppt;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.sppt.mapper")
public class SpptApplication {
	public static void main(String[] args) {
		SpringApplication.run(SpptApplication.class, args);
	}

}
