package com.io.codesystem.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetails {
    private Long id;
    private String title;
    private String firstName;
    private String middleName;
    private String lastName;
    private String suffix;
    private String preName;
    private String email;
    private String mobile;
    private String orgUnit;
    private String manager;
    private String gender;
    private String birthDate;
    private String occupation;
    private String bloodGroup;
    private String maritalStatus;
    private String address;
    private String city;
    private Integer countryId;
    private Integer stateId;
    private String phone1;
    private String phone2;
    private String notes;
    private String userPic;
    private String countryCode;
    private Long patientId;
    private Integer providerId;
    private String zipCode;
    private String prefix;
    private String fax;
    private Long createdBy;
    private LocalDateTime createdDate;
    private Long updatedBy;
    private LocalDateTime updatedDate;
}