package com.ecommerce.auctionplatform.identity.presentation.rest;

import com.ecommerce.auctionplatform.identity.application.dto.command.LoginCommand;
import com.ecommerce.auctionplatform.identity.application.dto.command.RefreshTokenCommand;
import com.ecommerce.auctionplatform.identity.application.dto.command.RegisterCommand;
import com.ecommerce.auctionplatform.identity.presentation.dto.request.LoginRequest;
import com.ecommerce.auctionplatform.identity.presentation.dto.request.RefreshRequest;
import com.ecommerce.auctionplatform.identity.presentation.dto.request.RegisterRequest;
import com.ecommerce.auctionplatform.identity.presentation.dto.response.AuthenticationResponse;
import com.ecommerce.auctionplatform.identity.presentation.dto.response.UserResponse;
import com.ecommerce.auctionplatform.identity.presentation.mapper.UserResponseMapper;
import com.ecommerce.auctionplatform.shared.presentation.response.APIResponse;
import com.ecommerce.auctionplatform.identity.application.port.in.AuthUseCase;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;


import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true,level  = AccessLevel.PRIVATE)
public class AuthenticationController {

    AuthUseCase authenticationUseCase;
    UserResponseMapper responseMapper;

    @PostMapping("/login")
    APIResponse<AuthenticationResponse> login(@Valid @RequestBody LoginRequest request){
        AuthenticationResponse response = responseMapper.toAuthenticationResponse(
                authenticationUseCase.login(LoginCommand.builder()
                        .userName(request.userName())
                        .passWord(request.passWord())
                        .build()));
        return APIResponse.<AuthenticationResponse>builder()
                .result(response)
                .status(200)
                .message("Login successful")
                .build();
    }


    @PostMapping("/register")
    APIResponse<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse user = responseMapper.toUserResponse(
                authenticationUseCase.register(RegisterCommand.builder()
                        .userName(request.userName())
                        .passWord(request.passWord())
                        .fullName(request.fullName())
                        .phone(request.phone())
                        .email(request.email())
                        .build()));
        return APIResponse.<UserResponse>builder()
                .status(200)
                .message("Registration successful")
                .result(user)
                .build();
    }

    @PostMapping("/refresh")
    APIResponse<AuthenticationResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        AuthenticationResponse response = responseMapper.toAuthenticationResponse(
                authenticationUseCase.refreshToken(RefreshTokenCommand.builder()
                        .token(request.token())
                        .refreshToken(request.refreshToken())
                        .build()));
        return APIResponse.<AuthenticationResponse>builder()
                .result(response)
                .status(200)
                .message("Token refreshed successfully")
                .build();
    }



}
