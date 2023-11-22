package com.io.codesystem.config;

import com.io.codesystem.domain.api.ApiClient;
import com.io.codesystem.repository.api.ApiClientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class UserDetailsService implements ReactiveUserDetailsService {
    Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    public ApiClientRepository clientRepository;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        logger.info("user name : {}",username);
        ApiClient client=clientRepository.findByApiKeyIgnoreCase(username);
        logger.info("api client obj :  {}",(client==null));
        if(client==null){
            return Mono.just(new CustomUser());
        }
        CustomUser usrObj=new CustomUser(client);
        logger.info("usrObj : {}",usrObj.toString());
        return Mono.just(usrObj);
    };
}