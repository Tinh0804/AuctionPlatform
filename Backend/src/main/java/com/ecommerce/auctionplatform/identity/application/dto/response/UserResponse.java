package com.ecommerce.auctionplatform.identity.application.dto.response;

import com.ecommerce.auctionplatform.identity.domain.enums.VerificationStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    UUID id;
    AccountResponse account;

    String name;

    String phone;

    String email;

    String identityCard;

    Boolean gender;

    @Builder.Default
    Integer reputationScore = 100;

    @Builder.Default
    VerificationStatus verificationStatus = VerificationStatus.UNVERIFIED;
    String identityFrontImage;
    String identityBackImage;
    String avatarImage;
    LocalDate dob;
    List<AddressResponse> addresses;
    WalletResponse wallet;
}
