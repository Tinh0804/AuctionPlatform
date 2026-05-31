package com.ecommerce.auctionplatform.utils;

import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Optional;
import org.springframework.security.oauth2.jwt.Jwt;

public class SecurityUtils {
    public static Optional<String> getCurrentProfileId(){
        var authentication =  SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return Optional.ofNullable(jwt.getClaimAsString("profile_id"));
        }
        return Optional.empty();

    }
    public static Optional<String> getCurrentAccountId(){
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication !=null && authentication.getPrincipal() instanceof Jwt jwt){
            return Optional.ofNullable(jwt.getSubject());
        }
        return Optional.empty();
    }
    public static Optional<String> getCurrentRole(){
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication !=null && authentication.getPrincipal() instanceof Jwt jwt){
            return Optional.ofNullable(jwt.getClaimAsString("scope"));
        }
        return Optional.empty();
    }

    public static Optional<String> getCurrentToken(){
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication !=null && authentication.getPrincipal() instanceof Jwt jwt){
            return Optional.ofNullable(jwt.getTokenValue());
        }
        return Optional.empty();
    }


}
