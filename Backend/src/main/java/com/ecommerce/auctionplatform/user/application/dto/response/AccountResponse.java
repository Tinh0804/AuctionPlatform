package com.ecommerce.auctionplatform.user.application.dto.response;

import com.ecommerce.auctionplatform.user.domain.enums.PredefinedRole;
import com.ecommerce.auctionplatform.user.domain.enums.ProviderType;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountResponse {
    UUID id;
    String username;
    String email;
    PredefinedRole role;
    ProviderType providerType;
    Boolean active;
}
