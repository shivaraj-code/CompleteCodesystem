package com.io.codesystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@EnableAsync
@Slf4j
public class CodesystemMaintenanceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CodesystemMaintenanceApplication.class, args);
		log.info(">>>> Logger in Main");
	}

}