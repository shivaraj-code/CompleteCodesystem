package com.io.codesystem.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long Id;
    private String name;
    private String username;
    private Boolean active;
    private String role;
    private UserDetails userDetails;
    private String roleCode;
    private Boolean archive;
    private String context;
    private Long clientId;
    private Boolean passwordReset;
    private String mobileNumber;
    private Long patientId;
    private Long createdBy;
    private LocalDateTime createdDate;
    private Long updatedBy;
    private LocalDateTime updatedDate;

    private UserDto(String name, Long id){
        this.name=name;
        this.Id =id;
    }
}
