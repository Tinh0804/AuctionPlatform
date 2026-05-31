package com.ecommerce.auctionplatform.config;

import com.ecommerce.auctionplatform.service.BlackListService;
import com.ecommerce.auctionplatform.service.JwtService;
import com.ecommerce.auctionplatform.utils.SecurityUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Configuration
public class JWTAuthentication extends OncePerRequestFilter {

   @Autowired
   private JwtService jwtService;

   @Autowired
   private BlackListService tokenBlacklistService;

   @SneakyThrows
   @Override
   protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain) throws ServletException, IOException {

      String token =extractTokenFromRequest(request);

      if (token != null && jwtService.introspect(token)) {

         if (tokenBlacklistService.isBlackListed(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token has been blacklisted (Logged out).");
            return; // Chặn luôn, không cho đi tiếp xuống Controller
         }

         setAuthenticationContext(token, request);
      }

      filterChain.doFilter(request, response);
   }

    private void setAuthenticationContext(String token, HttpServletRequest request) {
       String username = jwtService.extractUsername(token);


       UsernamePasswordAuthenticationToken authentication =
               new UsernamePasswordAuthenticationToken(username, null, new ArrayList<>());

       authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

       SecurityContextHolder.getContext().setAuthentication(authentication);
    }

   private String extractTokenFromRequest(HttpServletRequest request) {
      String bearerToken = request.getHeader("Authorization");
      if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
         return bearerToken.substring(7);
      }
      return null;
   }
}
