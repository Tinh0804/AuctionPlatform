package com.ecommerce.auctionplatform.mapper;

import com.ecommerce.auctionplatform.dto.request.AccountRequest;
import com.ecommerce.auctionplatform.dto.respose.AccountResponse;
import com.ecommerce.auctionplatform.entity.Account;
import org.mapstruct.Mapper;
import org.springframework.web.bind.annotation.Mapping;


@Mapper(componentModel = "spring")
public interface AccountMapper {
    Account toAccount(AccountRequest request);
    AccountResponse toAccountResponse(Account account);
}
