package com.ecommerce.auctionplatform.identity.application.port.in;

import com.ecommerce.auctionplatform.identity.application.dto.command.UpsertAddressCommand;
import com.ecommerce.auctionplatform.identity.application.dto.command.UpdatePhoneCommand;
import com.ecommerce.auctionplatform.identity.application.dto.command.UpdateUserCommand;
import com.ecommerce.auctionplatform.identity.application.dto.response.AddressResponse;
import com.ecommerce.auctionplatform.identity.application.dto.response.UserResponse;
import com.ecommerce.auctionplatform.shared.application.model.FileContent;

import java.util.UUID;

/**
 * Port/In – Use case interface for User domain.
 */
public interface UserUseCase {

    UserResponse getUserInfo();

    UserResponse updateUserInfo(UpdateUserCommand request);

    UserResponse updateAvatar(FileContent file);

    UserResponse updatePhone(UpdatePhoneCommand request);

    AddressResponse addAddress(UpsertAddressCommand request);

    AddressResponse updateAddress(UUID addressId, UpsertAddressCommand request);

    void deleteAddress(UUID addressId);

    boolean isCreatedAuction();
}
