package com.ecommerce.auctionplatform.identity.domain.model;

import com.ecommerce.auctionplatform.identity.domain.enums.ProviderType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Account {
    UUID id;

    String username;

    String password;

    Role role;

    @Builder.Default
    Boolean isActive = true;

    @Builder.Default
    LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    ProviderType provider = ProviderType.LOCAL;

    String providerId;
}
