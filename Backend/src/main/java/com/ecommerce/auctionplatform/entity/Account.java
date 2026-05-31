package com.ecommerce.auctionplatform.entity;

import com.ecommerce.auctionplatform.entity.enums.ProviderType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(nullable = false, unique = true, length = 100)
    String username;

    @JsonIgnore
    @Column(nullable = false, length = 255)
    String password;

    @ToString.Exclude
    @ManyToOne(optional = false)
    @JoinColumn(name = "role_id", referencedColumnName = "id", nullable = false)
    Role role;

    @Builder.Default
    @Column(name = "is_active")
    Boolean isActive = true;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "provider", length = 20)
    @Enumerated(EnumType.STRING)
    ProviderType provider = ProviderType.LOCAL;

    @Column(name = "provider_id", length = 255)
    String providerId;
}
