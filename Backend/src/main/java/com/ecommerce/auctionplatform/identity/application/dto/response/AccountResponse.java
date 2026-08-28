package com.ecommerce.auctionplatform.identity.application.dto.response;

import com.ecommerce.auctionplatform.identity.domain.enums.PredefinedRole;
import com.ecommerce.auctionplatform.identity.domain.enums.ProviderType;
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
