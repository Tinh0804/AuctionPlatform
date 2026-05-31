package com.ecommerce.auctionplatform.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @Column(length = 50)
    String type;

    @Column(nullable = false, length = 255)
    String title;

    @Column(length = 1000)
    String content;

    @Column(name = "reference_type", length = 20)
    String referenceType;

    @Column(name = "reference_id")
    UUID referenceId;

    @Builder.Default
    @Column(name = "is_read")
    Boolean isRead = false;

    @Column(name = "read_at")
    LocalDateTime readAt;

    @Builder.Default
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt = LocalDateTime.now();
}
