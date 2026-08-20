package com.ecommerce.auctionplatform.auth.presentation.rest;

import com.ecommerce.auctionplatform.auth.presentation.dto.request.LoginRequest;
import com.ecommerce.auctionplatform.auth.presentation.dto.request.RefreshRequest;
import com.ecommerce.auctionplatform.auth.presentation.dto.request.RegisterRequest;
import com.ecommerce.auctionplatform.shared.presentation.response.APIResponse;
import com.ecommerce.auctionplatform.auth.application.dto.AuthenticationResponse;
import com.ecommerce.auctionplatform.user.application.dto.response.UserResponse;
import com.ecommerce.auctionplatform.auth.application.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.text.ParseException;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true,level  = AccessLevel.PRIVATE)
public class AuthenticationController {

    AuthenticationService authenticationService;

    @PostMapping("/login")
    APIResponse<AuthenticationResponse> login(@RequestBody LoginRequest request){
        AuthenticationResponse response = authenticationService.login(request);
        return APIResponse.<AuthenticationResponse>builder()
                .result(response)
                .status(200)
                .message("Login successful")
                .build();
    }


    @PostMapping("/register")
    APIResponse<UserResponse> register(@RequestBody RegisterRequest request) {
        UserResponse user = authenticationService.register(request);
        return APIResponse.<UserResponse>builder()
                .status(200)
                .message("Registration successful")
                .result(user)
                .build();
    }

    @PostMapping("/refresh")
    APIResponse<AuthenticationResponse> refresh(@RequestBody RefreshRequest request) throws ParseException, JOSEException {
        AuthenticationResponse response = authenticationService.refreshToken(request);
        return APIResponse.<AuthenticationResponse>builder()
                .result(response)
                .status(200)
                .message("Token refreshed successfully")
                .build();
    }



}
