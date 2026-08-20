package com.ecommerce.auctionplatform.auction.domain.model;
import com.ecommerce.auctionplatform.user.domain.model.User;

import com.ecommerce.auctionplatform.auction.domain.enums.DepositStatus;
import com.ecommerce.auctionplatform.auction.domain.enums.RegistrationStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "auction_registrations", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"auction_id", "user_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuctionRegistration {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "auction_id", nullable = false)
    Auction auction;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @Column(name = "deposit_amount", nullable = false, precision = 18, scale = 2)
    BigDecimal depositAmount;

    @Builder.Default
    @Column(name = "deposit_status", nullable = false, columnDefinition = "deposit_status")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    DepositStatus depositStatus = DepositStatus.PENDING;

    @Builder.Default
    @Column(name = "registration_status", nullable = false, columnDefinition = "registration_status")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    RegistrationStatus registrationStatus = RegistrationStatus.PENDING;

    @Builder.Default
    @Column(name = "registered_at", nullable = false, updatable = false)
    LocalDateTime registeredAt = LocalDateTime.now();

    @Column(name = "approved_at")
    LocalDateTime approvedAt;

    @Column(length = 500)
    String note;
}
