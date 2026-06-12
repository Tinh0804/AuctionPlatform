package com.ecommerce.auctionplatform.controller;

import com.ecommerce.auctionplatform.dto.request.LoginRequest;
import com.ecommerce.auctionplatform.dto.respose.APIResponse;
import com.ecommerce.auctionplatform.dto.respose.AuthenticationResponse;
import com.ecommerce.auctionplatform.service.AuthenticationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true,level  = AccessLevel.PRIVATE)
public class AuthenticationController {

    AuthenticationService authenticationService;

    @PostMapping("/login")
    APIResponse<AuthenticationResponse> login(@RequestBody LoginRequest request){
        // Implement login logic here
        AuthenticationResponse response = authenticationService.login(request);
        // Set response fields based on login result
        return APIResponse.<AuthenticationResponse>builder()
                .result(response)
                .status(200) // Set appropriate status code
                .message("Login successful") // Set appropriate message
                .build();
    }



}
