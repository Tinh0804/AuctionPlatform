package com.ecommerce.auctionplatform.identity.presentation.mapper;

import com.ecommerce.auctionplatform.identity.presentation.dto.response.AccountResponse;
import com.ecommerce.auctionplatform.identity.presentation.dto.response.AddressResponse;
import com.ecommerce.auctionplatform.identity.presentation.dto.response.AuthenticationResponse;
import com.ecommerce.auctionplatform.identity.presentation.dto.response.UserResponse;
import com.ecommerce.auctionplatform.identity.presentation.dto.response.WalletResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface UserResponseMapper {
    UserResponse toUserResponse(
            com.ecommerce.auctionplatform.identity.application.dto.response.UserResponse source);

    AccountResponse toAccountResponse(
            com.ecommerce.auctionplatform.identity.application.dto.response.AccountResponse source);

    AddressResponse toAddressResponse(
            com.ecommerce.auctionplatform.identity.application.dto.response.AddressResponse source);

    WalletResponse toWalletResponse(
            com.ecommerce.auctionplatform.identity.application.dto.response.WalletResponse source);

    AuthenticationResponse toAuthenticationResponse(
            com.ecommerce.auctionplatform.identity.application.dto.response.AuthenticationResponse source);
}
