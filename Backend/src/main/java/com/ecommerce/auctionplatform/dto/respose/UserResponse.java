package com.ecommerce.auctionplatform.dto.respose;

import com.ecommerce.auctionplatform.entity.Account;
import com.ecommerce.auctionplatform.entity.enums.VerificationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
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
    WalletResponse wallet;
}
