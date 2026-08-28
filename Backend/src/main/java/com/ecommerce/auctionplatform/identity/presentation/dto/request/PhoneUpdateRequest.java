package com.ecommerce.auctionplatform.identity.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record PhoneUpdateRequest(
        @NotBlank @JsonProperty("firebase_id_token") String firebaseIdToken
) {
}
