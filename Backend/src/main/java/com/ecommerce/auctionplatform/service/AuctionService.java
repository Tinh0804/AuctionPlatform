package com.ecommerce.auctionplatform.service;

import com.ecommerce.auctionplatform.dto.request.AuctionCreationRequest;
import com.ecommerce.auctionplatform.dto.request.BidRequest;
import com.ecommerce.auctionplatform.dto.respose.AuctionCreationResponse;
import com.ecommerce.auctionplatform.dto.respose.AuctionDetailResponse;
import com.ecommerce.auctionplatform.dto.respose.BidResponse;
import com.ecommerce.auctionplatform.dto.respose.ImageResponse;
import com.ecommerce.auctionplatform.entity.*;
import com.ecommerce.auctionplatform.entity.enums.*;
import com.ecommerce.auctionplatform.exception.AppException;
import com.ecommerce.auctionplatform.exception.ErrorCode;
import com.ecommerce.auctionplatform.repository.*;
import com.ecommerce.auctionplatform.utils.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuctionService {

    AuctionRepository auctionRepository;
    ProductRepository productRepository;
    CategoryRepository categoryRepository;
    ImageRepository imageRepository;
    UserRepository userRepository;
    CloudinaryService cloudinaryService;
    BidRepository bidRepository;
    AuctionRegistrationRepository auctionRegistrationRepository;
    WalletRepository walletRepository;
    TransactionRepository transactionRepository;
    SimpMessagingTemplate messagingTemplate;

    @Transactional
    public AuctionCreationResponse createAuction(AuctionCreationRequest request) throws IOException {

        String profileId = SecurityUtils.getCurrentProfileId()
                .orElseThrow(() -> new AppException(ErrorCode.TOKEN_INVALID));

        User user = userRepository.findById(UUID.fromString(profileId))
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (user.getVerificationStatus() != VerificationStatus.VERIFIED) {
            throw new AppException(ErrorCode.UNVERIFIED_USER);
        }

        if (user.getReputationScore() == null || user.getReputationScore() < 50) {
            throw new AppException(ErrorCode.LOW_REPUTATION);
        }

        Category category = categoryRepository.findById(UUID.fromString(request.getCategoryId()))
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

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
        } else if (request.getRelistId() != null && !request.getRelistId().isEmpty()) {
            Auction oldAuction = auctionRepository.findById(UUID.fromString(request.getRelistId()))
                    .orElseThrow(() -> new AppException(ErrorCode.AUCTION_NOT_FOUND));

            if (!oldAuction.getUser().getId().equals(user.getId())) {
                throw new AppException(ErrorCode.NOT_AUCTON_OWNER);
            }

            List<Image> oldImages = imageRepository.findByProductId(oldAuction.getProduct().getId());
            for (Image oldImg : oldImages) {
                Image newImg = Image.builder()
                        .product(product)
                        .fileUrl(oldImg.getFileUrl())
                        .isCover(oldImg.getIsCover())
                        .build();
                imageRepository.save(newImg);
            }
        }

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
                .autoExtend(request.getAutoExtend() != null ? request.getAutoExtend() : false)
                .extendMinutes(request.getExtendMinutes() != null ? request.getExtendMinutes() : 0)
                //  reservePrice and buyNowPrice if neeeded
                .build();
        auction = auctionRepository.save(auction);

        return AuctionCreationResponse.builder()
                .auctionId(auction.getId())
                .message("Auction created successfully")
                .build();
    }
    public Page<AuctionDetailResponse> getAllAuctions(String statusStr, String categoryIdStr, Pageable pageable) {
        AuctionStatus status = null;
        if (statusStr != null && !statusStr.isBlank()) {
            try {
                status = AuctionStatus.valueOf(statusStr);
            } catch (IllegalArgumentException e) {
                // ignore
            }
        }
        UUID categoryId = null;
        if (categoryIdStr != null && !categoryIdStr.isBlank()) {
            try {
                categoryId = UUID.fromString(categoryIdStr);
            } catch (IllegalArgumentException e) {
                // ignore
            }
        }

        final AuctionStatus finalStatus = status;
        final UUID finalCategoryId = categoryId;

        Specification<Auction> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (finalStatus != null) {
                predicates.add(cb.equal(root.get("status"), finalStatus));
            }
            if (finalCategoryId != null) {
                predicates.add(cb.equal(root.get("product").get("category").get("id"), finalCategoryId));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Auction> auctions = auctionRepository.findAll(spec, pageable);
        return auctions.map(auction -> {
            List<Image> images = imageRepository.findByProductId(auction.getProduct().getId());
            List<ImageResponse> imageResponses = images.stream()
                    .map(img -> ImageResponse.builder()
                            .url(img.getFileUrl())
                            .isCover(img.getIsCover())
                            .build())
                    .toList();

            return AuctionDetailResponse.builder()
                    .id(auction.getId())
                    .productName(auction.getProduct().getName())
                    .description(auction.getDescription())
                    .status(auction.getStatus().name())
                    .startPrice(auction.getStartPrice())
                    .currentPrice(auction.getCurrentPrice())
                    .stepPrice(auction.getStepPrice())
                    .depositAmount(auction.getDepositAmount())
                    .startTime(auction.getStartTime())
                    .endTime(auction.getEndTime())
                    .autoExtend(auction.getAutoExtend())
                    .extendMinutes(auction.getExtendMinutes())
                    .sellerName(auction.getUser().getName())
                    .sellerId(auction.getUser().getId())
                    .images(imageResponses)
                    .build();
        });
    }

    public AuctionDetailResponse getAuctionDetail(UUID id) {
        Auction auction = auctionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.AUCTION_NOT_FOUND)); // Assuming ErrorCode.AUCTION_NOT_FOUND exists

        List<Image> images = imageRepository.findByProductId(auction.getProduct().getId());
        List<ImageResponse> imageResponses = images.stream()
                .map(img -> ImageResponse.builder()
                        .url(img.getFileUrl())
                        .isCover(img.getIsCover())
                        .build())
                .toList();

        return AuctionDetailResponse.builder()
                .id(auction.getId())
                .productName(auction.getProduct().getName())
                .description(auction.getDescription())
                .status(auction.getStatus().name())
                .startPrice(auction.getStartPrice())
                .currentPrice(auction.getCurrentPrice())
                .stepPrice(auction.getStepPrice())
                .depositAmount(auction.getDepositAmount())
                .startTime(auction.getStartTime())
                .endTime(auction.getEndTime())
                .autoExtend(auction.getAutoExtend())
                .extendMinutes(auction.getExtendMinutes())
                .sellerName(auction.getUser().getName())
                .sellerId(auction.getUser().getId())
                .images(imageResponses)
                .build();
    }

    public List<BidResponse> getAuctionBids(UUID id) {
        List<Bid> bids = bidRepository.findByAuctionIdOrderByBidTimeDesc(id);
        return bids.stream()
                .map(bid -> BidResponse.builder()
                        .id(bid.getId())
                        .bidAmount(bid.getBidAmount())
                        .bidTime(bid.getBidTime())
                        .isWinning(bid.getIsWinning())
                        .bidderId(bid.getUser().getId())
                        .bidderName(bid.getUser().getName()) // Maybe mask this later
                        .build())
                .toList();
    }

    @Transactional
    public BidResponse placeBid(UUID auctionId, BidRequest request) {
        String profileId = SecurityUtils.getCurrentProfileId()
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        User user = userRepository.findById(UUID.fromString(profileId))
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new AppException(ErrorCode.AUCTION_NOT_FOUND));

        if (auction.getStatus() != AuctionStatus.ACTIVE) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        if (auction.getUser().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.CANNOT_BID_OWN_AUCTION);
        }

        if (auction.getDepositAmount().compareTo(BigDecimal.ZERO) > 0) {
            AuctionRegistration registration = auctionRegistrationRepository
                    .findByAuctionIdAndUserId(auctionId, user.getId())
                    .orElse(null);

            if (registration == null) {
                Wallet wallet = walletRepository.findByUser(user)
                        .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST));

                if (wallet.getAvailableBalance().compareTo(auction.getDepositAmount()) < 0) {
                    throw new AppException(ErrorCode.BAD_REQUEST);
                }

                wallet.setAvailableBalance(wallet.getAvailableBalance().subtract(auction.getDepositAmount()));
                wallet.setFrozenBalance(wallet.getFrozenBalance().add(auction.getDepositAmount()));
                walletRepository.save(wallet);

                Transaction tx = Transaction.builder()
                        .wallet(wallet)
                        .type(TransactionType.AUCTION_DEPOSIT)
                        .amount(auction.getDepositAmount())
                        .status(TransactionStatus.SUCCESS)
                        .referenceType("REGISTRATION")
                        .build();
                transactionRepository.save(tx);

                registration = AuctionRegistration.builder()
                        .auction(auction)
                        .user(user)
                        .depositAmount(auction.getDepositAmount())
                        .depositStatus(DepositStatus.PAID)
                        .registrationStatus(RegistrationStatus.APPROVED)
                        .build();
                registration = auctionRegistrationRepository.save(registration);
                
                tx.setReferenceId(registration.getId());
                transactionRepository.save(tx);
            }
        }

        BigDecimal minBid = auction.getCurrentPrice().add(auction.getStepPrice());
        if (bidRepository.findByAuctionIdOrderByBidTimeDesc(auctionId).isEmpty()) {
            minBid = auction.getStartPrice();
        }
        if (request.getBidAmount().compareTo(minBid) < 0) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        Bid bid = Bid.builder()
                .auction(auction)
                .user(user)
                .bidAmount(request.getBidAmount())
                .isWinning(true)
                .build();
        bid = bidRepository.save(bid);

        auction.setCurrentPrice(request.getBidAmount());

        boolean extended = false;
        if (Boolean.TRUE.equals(auction.getAutoExtend()) && auction.getExtendMinutes() != null && auction.getExtendMinutes() > 0) {
            if (ChronoUnit.SECONDS.between(LocalDateTime.now(), auction.getEndTime()) < 60) {
                auction.setEndTime(auction.getEndTime().plusMinutes(auction.getExtendMinutes()));
                extended = true;
            }
        }

        auctionRepository.save(auction);

        BidResponse response = BidResponse.builder()
                .id(bid.getId())
                .bidAmount(bid.getBidAmount())
                .bidTime(bid.getBidTime())
                .isWinning(true)
                .bidderId(user.getId())
                .bidderName(user.getName())
                .build();

        Map<String, Object> message = new HashMap<>();
        message.put("type", "new_bid");
        message.put("bid_amount", bid.getBidAmount());
        message.put("bidder_id", user.getId());
        if (extended) {
            message.put("extended", true);
            message.put("end_time", auction.getEndTime().toString());
        }
        messagingTemplate.convertAndSend("/topic/auction/" + auctionId, (Object) message);

        return response;
    }
}
