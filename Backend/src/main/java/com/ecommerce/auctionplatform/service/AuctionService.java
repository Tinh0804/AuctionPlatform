package com.ecommerce.auctionplatform.service;

import com.ecommerce.auctionplatform.dto.request.AuctionCreationRequest;
import com.ecommerce.auctionplatform.dto.respose.AuctionCreationResponse;
import com.ecommerce.auctionplatform.entity.*;
import com.ecommerce.auctionplatform.entity.enums.AuctionStatus;
import com.ecommerce.auctionplatform.entity.enums.ProductCondition;
import com.ecommerce.auctionplatform.entity.enums.ProductStatus;
import com.ecommerce.auctionplatform.exception.AppException;
import com.ecommerce.auctionplatform.exception.ErrorCode;
import com.ecommerce.auctionplatform.repository.*;
import com.ecommerce.auctionplatform.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuctionService {

    private final AuctionRepository auctionRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ImageRepository imageRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    @Transactional
    public AuctionCreationResponse createAuction(AuctionCreationRequest request) throws IOException {

        // Get Current User
        String profileId = SecurityUtils.getCurrentProfileId()
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        User user = userRepository.findById(UUID.fromString(profileId))
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Category category = categoryRepository.findById(UUID.fromString(request.getCategoryId()))
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        // Save Product
        Product product = Product.builder()
                .user(user)
                .category(category)
                .name(request.getName())
                .condition(ProductCondition.valueOf(request.getCondition()))
                .description(request.getDescription())
                .origin(request.getOrigin())
                .status(ProductStatus.PENDING)
                .build();
        product = productRepository.save(product);

        // Upload and Save Images
        if (request.getFiles() != null && request.getFiles().length > 0) {
            boolean isCover = true;
            for (MultipartFile file : request.getFiles()) {
                String fileUrl = cloudinaryService.uploadFile(file,"products/" + product.getId());
                Image image = Image.builder()
                        .product(product)
                        .fileUrl(fileUrl)
                        .isCover(isCover)
                        .build();
                imageRepository.save(image);
                isCover = false; // Only first image is cover
            }
        }

        // Create Auction
        Auction auction = Auction.builder()
                .user(user)
                .product(product)
                .startPrice(request.getStartPrice())
                .currentPrice(request.getStartPrice())
                .stepPrice(request.getStepPrice())
                .depositAmount(request.getDepositAmount())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(AuctionStatus.PENDING)
                // Need fields for reservePrice and buyNowPrice if they exist in Entity, else omit
                .build();
        auction = auctionRepository.save(auction);

        return AuctionCreationResponse.builder()
                .auctionId(auction.getId())
                .message("Auction created successfully")
                .build();
    }
}
