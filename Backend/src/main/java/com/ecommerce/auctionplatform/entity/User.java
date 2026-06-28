package com.ecommerce.auctionplatform.entity;

import com.ecommerce.auctionplatform.entity.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @OneToOne(cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "account_id", referencedColumnName = "id", nullable = false)
    Account account;

    @Column(name = "name", nullable = false, length = 100)
    String name;

    @Column(name = "phone_number", nullable = false, length = 20)
    String phone;

    @Column(nullable = false, unique = true, length = 100)
    String email;

    @Column(name = "identity_card", unique = true, length = 20)
    String identityCard;

    Boolean gender;

    LocalDate dob;

    @Builder.Default
    @Column(name = "reputation_score")
    Integer reputationScore = 100;

    @Builder.Default
    @Column(name = "verification_status",nullable = false, columnDefinition = "verification_status")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    VerificationStatus verificationStatus = VerificationStatus.UNVERIFIED;

    @Column(name = "identity_front_image", length = 500)
    String identityFrontImage;

    @Column(name = "identity_back_image", length = 500)
    String identityBackImage;

    @Column(name = "avatar_image", length = 500)
    String avatarImage;

}
