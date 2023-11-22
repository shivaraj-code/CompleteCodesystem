package com.io.codesystem.config;

import com.io.codesystem.domain.api.ApiClient;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
public class CustomUser implements UserDetails {

    private String username;
    private String password;
    private int enabled;

    public CustomUser(){
    }

    public CustomUser(ApiClient user){
        this.setUsername(user.getApiKey());
        this.setPassword(user.getApiSecretKey());
        this.setEnabled(user.getDisabled()?0:1);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_API_CLIENT"));
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled == 1;
    }

}