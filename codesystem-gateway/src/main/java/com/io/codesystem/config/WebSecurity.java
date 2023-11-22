package com.io.codesystem.config;

import com.google.common.collect.ImmutableList;
import com.io.codesystem.domain.api.ApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class WebSecurity {
    Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private UserDetailsService userDetailsService;


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    protected ReactiveAuthenticationManager reactiveAuthenticationManager() {
        return authentication -> {
            String apiKey=authentication.getPrincipal().toString();
            logger.info("API Key : {} ",apiKey);
            Mono<UserDetails> clientObj= userDetailsService.findByUsername(apiKey);
            if(clientObj==null){
                return Mono.error( new UsernameNotFoundException("User not found"));
            }
            return clientObj
                    .switchIfEmpty(Mono.error( new UsernameNotFoundException("Invalid API Key Or Secret Key!")))
                    .flatMap(user->{
                        final String username = authentication.getPrincipal().toString();
                        final CharSequence rawPassword  =   authentication.getCredentials().toString();
                        if( passwordEncoder().matches(rawPassword, user.getPassword())){
                            logger.info("User has been authenticated {}", username);
                            return Mono.just( new UsernamePasswordAuthenticationToken(username, user.getPassword(), user.getAuthorities()) );
                        }
                        logger.info("Password not matched {}", username);
                        //This constructor can be safely used by any code that wishes to create a UsernamePasswordAuthenticationToken, as the isAuthenticated() will return false.
                        return Mono.just( new UsernamePasswordAuthenticationToken(username, authentication.getCredentials()));
                    });
        };
    }
    private String[] openURL(){
        return new String[]{"/ui/**", "/actuator/**","/*/actuator/**","/*/","/swagger/**","/config/**","/eureka/**","/*/swagger-ui/**","/*/swagger-resources/**","/*/v3/api-docs/**"};
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(ImmutableList.of("*"));
        config.setAllowedMethods(ImmutableList.of("HEAD", "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(ImmutableList.of("*"));
        config.setAllowCredentials(false);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http.csrf(ServerHttpSecurity.CsrfSpec::disable);
        http
                //require that all
                .headers()
                .frameOptions().disable()
                .and()
                .cors().configurationSource(corsConfigurationSource()).and()
                .authorizeExchange(exchanges ->exchanges.pathMatchers(HttpMethod.OPTIONS,"/**").permitAll().pathMatchers(openURL()).permitAll().anyExchange().authenticated())
                .httpBasic(withDefaults());
                //this allows js to read cookie
                //.csrf(csrf -> csrf.csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse()));
        return http.build();
    }
}
