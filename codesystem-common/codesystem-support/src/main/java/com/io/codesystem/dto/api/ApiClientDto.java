package com.io.codesystem.dto.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiClientDto {
    private Integer id;
    private String apiKey;
    private String apiSecretKey;
    private Boolean disabled;
    private Integer createdBy;
    private LocalDateTime createdDate;
}