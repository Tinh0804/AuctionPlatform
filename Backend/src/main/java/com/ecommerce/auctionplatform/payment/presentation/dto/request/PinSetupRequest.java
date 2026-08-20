package com.ecommerce.auctionplatform.payment.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PinSetupRequest {
    @JsonProperty("firebase_id_token")
    String firebaseIdToken;
    
    @JsonProperty("new_pin")
    String newPin;
}
