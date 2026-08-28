package com.ecommerce.auctionplatform.identity.application.dto.command;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdatePhoneCommand {
    String firebaseIdToken;
}
