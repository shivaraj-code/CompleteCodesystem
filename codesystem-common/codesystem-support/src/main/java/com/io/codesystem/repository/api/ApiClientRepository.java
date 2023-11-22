package com.io.codesystem.repository.api;

import com.io.codesystem.domain.api.ApiClient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiClientRepository extends JpaRepository<ApiClient,Integer> {
    ApiClient findByApiKeyIgnoreCase(String apiKey);
}
