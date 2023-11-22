package com.io.codesystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class CodesystemConfigApplication {

	public static void main(String[] args) {
		SpringApplication.run(CodesystemConfigApplication.class, args);
	}

}
