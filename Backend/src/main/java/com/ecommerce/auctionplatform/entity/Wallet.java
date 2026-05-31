package com.ecommerce.auctionplatform.entity;

import com.ecommerce.auctionplatform.entity.enums.WalletStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "wallets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    User user;

    @Builder.Default
    @Column(name = "available_balance", precision = 18, scale = 2)
    BigDecimal availableBalance = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "frozen_balance", precision = 18, scale = 2)
    BigDecimal frozenBalance = BigDecimal.ZERO;

    @Column(name = "pin_code", length = 255)
    String pinCode;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    WalletStatus status = WalletStatus.ACTIVE;

    String notes;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    LocalDateTime updatedAt;
}
