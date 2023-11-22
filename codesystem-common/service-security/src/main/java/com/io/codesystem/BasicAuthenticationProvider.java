package com.io.codesystem;

import com.io.codesystem.domain.api.ApiClient;
import com.io.codesystem.repository.api.ApiClientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class BasicAuthenticationProvider implements AuthenticationProvider {

    Logger logger = LoggerFactory.getLogger(BasicAuthenticationProvider.class);

    private ApiClientRepository repository;

    private PasswordEncoder encoder;

    public BasicAuthenticationProvider(ApiClientRepository repository, PasswordEncoder encoder) {
        this.encoder = encoder;
        this.repository = repository;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        ApiClient apiObj = repository.findByApiKeyIgnoreCase(username);
        if (apiObj == null) {
            throw new BadCredentialsException("Invalid Api Key!");
        }

        if (encoder.matches(password, apiObj.getApiSecretKey())) {
            logger.info("Successfully Authenticated the API Request");
            return new UsernamePasswordAuthenticationToken(username, password, getRoles("API_CLIENT"));
        } else {
            throw new BadCredentialsException("Invalid Api SecretKey!");
        }
    }

    private List<GrantedAuthority> getRoles(String studentRoles) {
        List<GrantedAuthority> grantedAuthorityList = new ArrayList<>();
        String[] roles = studentRoles.split(",");
        for (String role : roles) {
            logger.info("Role: " + role);
            grantedAuthorityList.add(new SimpleGrantedAuthority(role.replaceAll("\\s+", "")));
        }
        return grantedAuthorityList;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}