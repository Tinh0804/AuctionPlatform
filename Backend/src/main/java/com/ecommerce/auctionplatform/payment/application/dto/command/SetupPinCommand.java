package com.ecommerce.auctionplatform.payment.application.dto.command;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SetupPinCommand {
    String firebaseIdToken;
    
    String newPin;
}
