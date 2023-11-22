package com.io.codesystem;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ObjectUtils;


@Configuration
public class SwaggerDocumentationConfiguration {
    @Value("${openapi.server.url:}")
    private String url;
    private static final String SCHEME_NAME = "basicAuth";
    private static final String SCHEME = "basic";

    @Bean
    public OpenAPI customOpenAPI() {
        OpenAPI openAPI= new OpenAPI().info(apiInfo());
        openAPI.components(new Components().addSecuritySchemes(SCHEME_NAME, createSecurityScheme()));
        openAPI.addSecurityItem(new SecurityRequirement().addList(SCHEME_NAME));
        if(!ObjectUtils.isEmpty(url)){
            openAPI.addServersItem(serversItem());
        }

        return openAPI;
    }
    private SecurityScheme createSecurityScheme() {
        return new SecurityScheme()
                .name(SCHEME_NAME)
                .type(SecurityScheme.Type.HTTP)
                .scheme(SCHEME);
    }
    private Server serversItem() {
        Server serversItem =new Server();
        serversItem.url(url);
        return serversItem;
    }

    private Info apiInfo() {
        return new Info()
                .title("Codesystem API")
                .description("API from frontend")
                .version("3.0")
                .contact(apiContact())
                .license(apiLicence());
    }

    private License apiLicence() {
        return new License()
                .name("MIT Licence")
                .url("https://opensource.org/licenses/mit-license.php");
    }

    private Contact apiContact() {
        return new Contact()
                .name("Codesystem ")
                .email("codesystem@gmail.com")
                .url("https://github.com/ErwanLT");
    }
}
