package com.ecommerce.auctionplatform.payment.infrastructure.persistence.entity;

import com.ecommerce.auctionplatform.payment.domain.enums.OrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "auction_record_id")
    private UUID auctionRecordId;

    @Column(name = "buyer_id", nullable = false)
    private UUID buyerId;

    @Column(name = "seller_id", nullable = false)
    private UUID sellerId;

    @Column(name = "total_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "order_status")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private OrderStatus status = OrderStatus.PENDING_PAYMENT;

    @Column(name = "meeting_address", length = 500)
    private String meetingAddress;
    @Column(name = "meeting_time")
    private LocalDateTime meetingTime;
    @Column(name = "met_at")
    private LocalDateTime metAt;
    @Column(length = 500)
    private String note;
    @Column(name = "rating_score")
    private Integer ratingScore;
    @Column(name = "review_content", length = 1000)
    private String reviewContent;
    @Column(name = "review_date")
    private LocalDateTime reviewDate;
    @Column(name = "tracking_code", length = 100)
    private String trackingCode;
    @Column(name = "shipping_provider", length = 100)
    private String shippingProvider;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
