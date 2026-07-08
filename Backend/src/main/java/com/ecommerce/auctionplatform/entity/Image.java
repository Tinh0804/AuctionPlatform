package com.ecommerce.auctionplatform.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.ecommerce.auctionplatform.entity.enums.ImageReferenceType;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "images", indexes = {
        @Index(name = "idx_images_reference", columnList = "reference_type, reference_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(name = "reference_type", nullable = false, columnDefinition = "image_reference_type")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    ImageReferenceType referenceType;

     // ID của thực thể liên quan (product_id, dispute_id, user_id, ...).
    @Column(name = "reference_id", nullable = false)
    UUID referenceId;

    @Column(name = "file_url", nullable = false, length = 500)
    String fileUrl;

    @Builder.Default
    @Column(name = "is_cover")
    Boolean isCover = false;

     // Thứ tự hiển thị.
    @Builder.Default
    @Column(name = "sort_order")
    Integer sortOrder = 0;

    @Column(name = "description", length = 255)
    String description;

    @Builder.Default
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt = LocalDateTime.now();
}
