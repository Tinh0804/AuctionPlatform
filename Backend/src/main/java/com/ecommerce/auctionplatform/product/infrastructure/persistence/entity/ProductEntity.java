package com.ecommerce.auctionplatform.product.infrastructure.persistence.entity;

import com.ecommerce.auctionplatform.product.domain.enums.ProductCondition;
import com.ecommerce.auctionplatform.product.domain.enums.ProductStatus;
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

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private CategoryEntity category;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "condition", nullable = false, columnDefinition = "product_condition")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private ProductCondition condition;

    @Column(length = 2000)
    private String description;

    private String origin;

    @Column(name = "provenance_file_url", length = 500)
    private String provenanceFileUrl;

    @Column(name = "manufacture_year", length = 10)
    private String manufactureYear;

    @Builder.Default
    @Column(name = "has_certificate")
    private Boolean hasCertificate = false;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "product_status")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private ProductStatus status = ProductStatus.PENDING;

    @Builder.Default
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
