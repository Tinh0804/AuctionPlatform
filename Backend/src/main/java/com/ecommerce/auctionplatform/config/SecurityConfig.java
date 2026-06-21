package com.ecommerce.auctionplatform.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Value("${app.cors.allowed-origins:http://localhost:5174}")
        private List<String> allowedOrigins;

        @Autowired
        private JwtDecoder jwtDecoder;

        private final String[] AUTH_ENDPOINTS = {
                        "/auth/login",
                        "/auth/register",
                        "/auth/refresh",
                        "/customer/register",
                        "/drivers/register",
                        "/error"
        };

        private final String[] SWAGGER_ENDPOINTS = {
                        "/v3/api-docs", "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**", "/swagger-resources",
                        "/swagger-resources/**", "/configuration/ui", "/configuration/security", "/webjars/**"
        };
        private final String[] SOCKET_ENDPOINTS = {
                        "/ws/**", "/topic/**", "/app/**"
        };
        private final String[] PAYMENT_ENDPOINTS = {
                        "/payments/momo/return",
                        "/payments/vnpay/return",
                        "/payments/momo/notify",
                        "/payments/vnpay/notify",
                        "/payments/momo/callback",
                        "/payments/vnpay/callback",
        };


        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .cors(Customizer.withDefaults())
                                .csrf(Customizer.withDefaults())
                                .headers(headers -> headers
                                                .frameOptions(frameOptions -> frameOptions.sameOrigin())
                                                .xssProtection(Customizer.withDefaults()))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(AUTH_ENDPOINTS).permitAll()
                                                .requestMatchers(SWAGGER_ENDPOINTS).permitAll()
                                                .requestMatchers(SOCKET_ENDPOINTS).permitAll()
                                                .requestMatchers(PAYMENT_ENDPOINTS).permitAll()
                                                .anyRequest().authenticated())
                                .csrf(AbstractHttpConfigurer::disable);
                http
                                .oauth2ResourceServer(oauth2 -> oauth2
                                                .jwt(
                                                                jwtConfigurer -> jwtConfigurer.decoder(jwtDecoder)
                                                                                .jwtAuthenticationConverter(this
                                                                                                .jwtAuthenticationConverter())
                                                )
                                                .authenticationEntryPoint(new JWTAuthentication()))
                                .oauth2Login(Customizer.withDefaults());
                return http.build();
        }

        @Bean
        public JwtAuthenticationConverter jwtAuthenticationConverter() {
                JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
                grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_"); 
                grantedAuthoritiesConverter.setAuthoritiesClaimName("scope"); 
 
                JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
                jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
                return jwtAuthenticationConverter;
        }

        @Bean
        public org.springframework.web.filter.CorsFilter corsFilter() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOrigins(allowedOrigins);
                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "x-auth-token", "ngrok-skip-browser-warning"));
                configuration.setExposedHeaders(Arrays.asList("x-auth-token"));
                configuration.setAllowCredentials(true);
                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return new org.springframework.web.filter.CorsFilter(source);
        }

}
