package com.ecommerce.auctionplatform.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PhoneUpdateRequest {
    @NotBlank(message = "Firebase ID Token is required")
    @JsonProperty("firebase_id_token")
    String firebaseIdToken;
}
