package com.ecommerce.auctionplatform.entity;

import com.ecommerce.auctionplatform.entity.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @ManyToOne
    @JoinColumn(name = "auction_record_id")
    AuctionRecord auctionRecord;

    @ManyToOne(optional = false)
    @JoinColumn(name = "buyer_id", nullable = false)
    User buyer;

    @ManyToOne(optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    User seller;

    @Column(name = "total_amount", nullable = false, precision = 18, scale = 2)
    BigDecimal totalAmount;

    @Builder.Default
    @Column(name = "status", nullable = false, columnDefinition = "order_status")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    OrderStatus status = OrderStatus.PENDING_PAYMENT;

    @Column(name = "meeting_address", length = 500)
    String meetingAddress;

    @Column(name = "meeting_time")
    LocalDateTime meetingTime;

    @Column(name = "met_at")
    LocalDateTime metAt;

    @Column(length = 500)
    String note;

    @Column(name = "rating_score")
    Integer ratingScore;

    @Column(name = "review_content", length = 1000)
    String reviewContent;

    @Column(name = "review_date")
    LocalDateTime reviewDate;

    @Column(name = "tracking_code", length = 100)
    String trackingCode;

    @Column(name = "shipping_provider", length = 100)
    String shippingProvider;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    LocalDateTime updatedAt;
}
