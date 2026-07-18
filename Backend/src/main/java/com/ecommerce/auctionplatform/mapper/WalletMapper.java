package com.ecommerce.auctionplatform.mapper;

import com.ecommerce.auctionplatform.dto.respose.WalletResponse;
import com.ecommerce.auctionplatform.entity.Wallet;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WalletMapper {
    WalletResponse toWalletResponse(Wallet wallet);
}
