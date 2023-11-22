package com.io.codesystem.controller;


import com.io.codesystem.config.ServiceDefinitionsContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class ServiceDefinitionController {
    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private ServiceDefinitionsContext definitionContext;
    private static final String DEFAULT_SWAGGER_URL = "/v3/api-docs";
    private static final String KEY_SWAGGER_URL = "swagger_url";

    @GetMapping("/service/{servicename}")
    public String getServiceDefinition(@PathVariable("servicename") String serviceName) {

        return definitionContext.getSwaggerDefinition(serviceName);

    }

    @GetMapping("/v3/api-docs/swagger-config")
    public Map<String, Object> swaggerConfig() {
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("urls", definitionContext.getSwaggerDefinitions());
        return config;
    }

    private String getSwaggerURL(ServiceInstance instance) {
        String swaggerURL = instance.getMetadata().get(KEY_SWAGGER_URL);
        return swaggerURL != null ? instance.getUri() + swaggerURL : instance.getUri() + DEFAULT_SWAGGER_URL;
    }

}