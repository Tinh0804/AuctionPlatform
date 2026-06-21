package com.ecommerce.auctionplatform.config;

import com.ecommerce.auctionplatform.dto.respose.APIResponse;
import com.ecommerce.auctionplatform.exception.ErrorCode;
import com.ecommerce.auctionplatform.service.BlackListService;
import com.ecommerce.auctionplatform.service.JwtService;
import com.ecommerce.auctionplatform.utils.SecurityUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;

@Component
public class JWTAuthentication extends OncePerRequestFilter implements AuthenticationEntryPoint {

   @Autowired
   private JwtService jwtService;

   @Autowired
   private BlackListService tokenBlacklistService;

   @Autowired
   private ObjectMapper objectMapper; // inject bean

   @SneakyThrows
   @Override
   protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain) throws ServletException, IOException {
      String token = extractTokenFromRequest(request);

      if (token != null && jwtService.introspect(token)) {
         if (tokenBlacklistService.isBlackListed(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token has been blacklisted (Logged out).");
            return;
         }
         setAuthenticationContext(token, request);
      }
      // Nếu token null hoặc invalid -> vẫn cho đi tiếp (public endpoints)
      filterChain.doFilter(request, response);
   }

   private void setAuthenticationContext(String token, HttpServletRequest request) {
      String username = jwtService.extractUsername(token);
      // Nếu có authorities thì load ở đây
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

   @Override
   public void commence(HttpServletRequest request,
                        HttpServletResponse response,
                        AuthenticationException authException) throws IOException {
      ErrorCode errorCode = ErrorCode.ACCESS_DENIED;
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);

      APIResponse<?> apiResponse = APIResponse.builder()
              .status(errorCode.getStatus())
              .message(errorCode.getMessage())
              .build();

      response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
   }
}