package com.ecommerce.auctionplatform.identity.infrastructure.persistence.entity;

import com.ecommerce.auctionplatform.identity.domain.enums.VerificationStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private AccountEntity account;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phone;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "identity_card", unique = true, length = 20)
    private String identityCard;

    private Boolean gender;

    private LocalDate dob;

    @Builder.Default
    @Column(name = "reputation_score")
    private Integer reputationScore = 100;

    @Builder.Default
    @Column(name = "verification_status", nullable = false, columnDefinition = "verification_status")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private VerificationStatus verificationStatus = VerificationStatus.UNVERIFIED;

    @Column(name = "identity_front_image", length = 500)
    private String identityFrontImage;

    @Column(name = "identity_back_image", length = 500)
    private String identityBackImage;

    @Column(name = "avatar_image", length = 500)
    private String avatarImage;
}
