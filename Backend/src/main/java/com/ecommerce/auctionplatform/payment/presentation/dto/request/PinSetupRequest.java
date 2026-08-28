package com.ecommerce.auctionplatform.payment.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record PinSetupRequest(
        @NotBlank @JsonProperty("firebase_id_token") String firebaseIdToken,
        @NotBlank @JsonProperty("new_pin") String newPin
) {
}
