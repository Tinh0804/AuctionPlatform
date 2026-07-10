package com.ecommerce.auctionplatform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class ConfigurationCommon {
    @Bean
    PasswordEncoder passwordEncoder(){

        return new BCryptPasswordEncoder(10);
    }
}
