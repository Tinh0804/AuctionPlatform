package com.ecommerce.auctionplatform.identity.application.port.in;

import com.ecommerce.auctionplatform.identity.application.dto.response.AuthenticationResponse;
import com.ecommerce.auctionplatform.identity.application.dto.command.LoginCommand;
import com.ecommerce.auctionplatform.identity.application.dto.command.RefreshTokenCommand;
import com.ecommerce.auctionplatform.identity.application.dto.command.RegisterCommand;
import com.ecommerce.auctionplatform.identity.application.dto.response.UserResponse;

/**
 * Port/In – authentication capability of the User bounded context.
 * Controllers depend on this interface, NOT on AuthenticationService directly.
 */
public interface AuthUseCase {

    UserResponse register(RegisterCommand request);

    AuthenticationResponse login(LoginCommand request);

    AuthenticationResponse refreshToken(RefreshTokenCommand request);

    void logout(String refreshToken);

    boolean introspect(String token);
}
