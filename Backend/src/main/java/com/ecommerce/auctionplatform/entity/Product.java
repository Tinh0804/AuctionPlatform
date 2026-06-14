package com.ecommerce.auctionplatform.entity;

import com.ecommerce.auctionplatform.entity.enums.ProductCondition;
import com.ecommerce.auctionplatform.entity.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;

    @ManyToOne
    @JoinColumn(name = "category_id")
    Category category;

    @Column(nullable = false, length = 255)
    String name;

    @Column(name = "condition", nullable = false, columnDefinition = "product_condition")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    ProductCondition condition;

    @Column(length = 2000)
    String description;

    String origin;

    @Column(name = "provenance_file_url", length = 500)
    String provenanceFileUrl;

    @Column(name = "manufacture_year", length = 10)
    String manufactureYear;

    @Builder.Default
    @Column(name = "has_certificate")
    Boolean hasCertificate = false;

    @Builder.Default
    @Column(name = "status", nullable = false, columnDefinition = "product_status")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    ProductStatus status = ProductStatus.PENDING;

    @Builder.Default
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    LocalDateTime updatedAt;
}
