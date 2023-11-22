package com.io.codesystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer
@SpringBootApplication
public class CodesystemRegistryApplication {

	public static void main(String[] args) {
		SpringApplication.run(CodesystemRegistryApplication.class, args);
	}

}
