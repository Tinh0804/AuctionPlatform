package com.ecommerce.auctionplatform.payment.domain.model;

import com.ecommerce.auctionplatform.payment.domain.enums.OrderStatus;
import com.ecommerce.auctionplatform.payment.domain.exception.OrderAlreadyReviewedException;
import com.ecommerce.auctionplatform.payment.domain.exception.OrderNotEligibleForReviewException;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Order {
    public void markAsPaid() { this.status = OrderStatus.PAID; touch(); }
    public void markAsCompleted() { this.status = OrderStatus.COMPLETED; touch(); }
    public void markAsShipping() { this.status = OrderStatus.SHIPPING; touch(); }
    public void cancel() { this.status = OrderStatus.CANCELLED; touch(); }
    public void markAsDisputed() { this.status = OrderStatus.DISPUTED; touch(); }
    public void updateShippingInfo(String tracking, String provider) {
        this.trackingCode = tracking;
        this.shippingProvider = provider;
        touch();
    }
    public void addReview(int rating, String review) {
        if (rating < 1 || rating > 5) {
            throw new OrderNotEligibleForReviewException();
        }
        if (this.ratingScore != null) {
            throw new OrderAlreadyReviewedException();
        }
        this.ratingScore = rating;
        this.reviewContent = review;
        this.reviewDate = LocalDateTime.now();
        touch();
    }

    private void touch() { this.updatedAt = LocalDateTime.now(); }

    UUID id;

    UUID auctionRecordId;

    UUID buyerId;

    UUID sellerId;

    BigDecimal totalAmount;

    @Builder.Default
    OrderStatus status = OrderStatus.PENDING_PAYMENT;

    String meetingAddress;

    LocalDateTime meetingTime;

    LocalDateTime metAt;

    String note;

    Integer ratingScore;

    String reviewContent;

    LocalDateTime reviewDate;

    String trackingCode;

    String shippingProvider;

    @Builder.Default
    LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    LocalDateTime updatedAt = LocalDateTime.now();
}
