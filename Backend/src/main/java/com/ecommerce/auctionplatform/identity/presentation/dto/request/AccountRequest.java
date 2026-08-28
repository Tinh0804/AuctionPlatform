package com.ecommerce.auctionplatform.identity.presentation.dto.request;

import com.ecommerce.auctionplatform.identity.domain.model.Role;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class AccountRequest {
    String userName;
    String passWord;
    Role roleNo;
    boolean isActive=true;

}
