package com.io.codesystem;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented

@Import(BasicSecurityConfig.class)
@Configuration
public @interface EnableSecurity {
}
