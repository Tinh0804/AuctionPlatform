package com.ecommerce.auctionplatform.dto.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class LoginRequest {
    String userName;
    String passWord;

}
