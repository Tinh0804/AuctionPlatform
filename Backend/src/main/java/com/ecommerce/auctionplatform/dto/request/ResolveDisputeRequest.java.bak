package com.ecommerce.auctionplatform.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResolveDisputeRequest {
    @NotBlank(message = "Outcome is required (BUYER_WIN or SELLER_WIN)")
    String outcome; // BUYER_WIN or SELLER_WIN
    
    @NotBlank(message = "Resolution note is required")
    String resolution;
}
