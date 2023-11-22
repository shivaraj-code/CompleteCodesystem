package com.io.codesystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableSecurity
@SpringBootApplication(scanBasePackages = {"com.io.codesystem","com.io.codesystem.common.swagger"})
public class CodesystemManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(CodesystemManagementApplication.class, args);
	}

}
