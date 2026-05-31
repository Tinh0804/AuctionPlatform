package com.ecommerce.auctionplatform.dto.respose;

import com.ecommerce.auctionplatform.entity.Role;
import com.ecommerce.auctionplatform.entity.enums.ProviderType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountResponse {
    UUID id;
    String username;
    Role role;
    Boolean isActive = true;
    LocalDateTime createdAt = LocalDateTime.now();
    ProviderType provider = ProviderType.LOCAL;
    String providerId;
}
