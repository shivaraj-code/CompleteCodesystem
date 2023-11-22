package com.io.codesystem.domain.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;



@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "api_client")
public class ApiClient {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    @Column(name = "api_key")
    private String apiKey;
    @Column(name = "api_secret_key")
    private String apiSecretKey;
    @Column(name = "disabled")
    private Boolean disabled;
    @Column(name = "created_by")
    private Integer createdBy;
    @Column(name = "created_date")
    private LocalDateTime createdDate;
}