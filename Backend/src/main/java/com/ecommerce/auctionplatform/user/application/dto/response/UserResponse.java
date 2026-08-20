package com.ecommerce.auctionplatform.user.application.dto.response;

import com.ecommerce.auctionplatform.user.domain.model.Account;
import com.ecommerce.auctionplatform.user.domain.enums.VerificationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
    Account account;

    String name;

    String phone;

    String email;

    String identityCard;

    Boolean gender;

    Integer reputationScore = 100;

    @Builder.Default
    VerificationStatus verificationStatus = VerificationStatus.UNVERIFIED;
    String identityFrontImage;
    String identityBackImage;
    String avatarImage;
    LocalDate dob;
    List<AddressDto> addresses;
    WalletResponse wallet;
}
