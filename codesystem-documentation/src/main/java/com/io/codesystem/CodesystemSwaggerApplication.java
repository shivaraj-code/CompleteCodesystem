package com.io.codesystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableSecurity
@SpringBootApplication
public class CodesystemSwaggerApplication {
	public static void main(String[] args) {
		SpringApplication.run(CodesystemSwaggerApplication.class, args);
	}
}
