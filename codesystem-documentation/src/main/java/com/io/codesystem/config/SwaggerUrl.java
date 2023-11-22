package com.io.codesystem.config;

import lombok.Data;

@Data
public class SwaggerUrl {
    public SwaggerUrl(String name, String url) {
        this.name = name;
        this.url = url;
    }

    private String url;
    private String name;
}
