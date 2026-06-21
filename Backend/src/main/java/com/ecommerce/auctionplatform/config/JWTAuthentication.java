package com.ecommerce.auctionplatform.config;

import com.ecommerce.auctionplatform.dto.respose.APIResponse;
import com.ecommerce.auctionplatform.exception.ErrorCode;
import com.ecommerce.auctionplatform.service.BlackListService;
import com.ecommerce.auctionplatform.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JWTAuthentication extends OncePerRequestFilter implements AuthenticationEntryPoint {

   @Autowired
   private JwtService jwtService;

   @Autowired
   private BlackListService tokenBlacklistService;

   @Autowired
   private ObjectMapper objectMapper = new ObjectMapper(); // inject bean

   @Autowired
   private JwtDecoder jwtDecoder;

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
      Jwt jwt = jwtDecoder.decode(token);
      List<SimpleGrantedAuthority> authorities = new ArrayList<>();
      try {
         String scope = jwt.getClaimAsString("scope");
         if (scope != null && !scope.trim().isEmpty()) {
            for (String role : scope.split(" ")) {
               authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
            }
         }
      } catch (Exception e) {
         // ignore
      }
      JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt, authorities);
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