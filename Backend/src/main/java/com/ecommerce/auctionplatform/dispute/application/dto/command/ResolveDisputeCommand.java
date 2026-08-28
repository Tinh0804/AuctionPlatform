package com.ecommerce.auctionplatform.dispute.application.dto.command;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResolveDisputeCommand {
    String outcome; // BUYER_WIN or SELLER_WIN
    
    String resolution;
}
