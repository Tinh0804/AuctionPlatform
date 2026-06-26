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
    private final BidRepository bidRepository;
    private final AuctionRegistrationRepository auctionRegistrationRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

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
                .autoExtend(request.getAutoExtend() != null ? request.getAutoExtend() : false)
                .extendMinutes(request.getExtendMinutes() != null ? request.getExtendMinutes() : 0)
                // Need fields for reservePrice and buyNowPrice if they exist in Entity, else omit
                .build();
        auction = auctionRepository.save(auction);

        return AuctionCreationResponse.builder()
                .auctionId(auction.getId())
                .message("Auction created successfully")
                .build();
    }
    public com.ecommerce.auctionplatform.dto.respose.AuctionDetailResponse getAuctionDetail(UUID id) {
        Auction auction = auctionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.AUCTION_NOT_FOUND)); // Assuming ErrorCode.AUCTION_NOT_FOUND exists

        java.util.List<Image> images = imageRepository.findByProductId(auction.getProduct().getId());
        java.util.List<com.ecommerce.auctionplatform.dto.respose.AuctionDetailResponse.ImageResponse> imageResponses = images.stream()
                .map(img -> com.ecommerce.auctionplatform.dto.respose.AuctionDetailResponse.ImageResponse.builder()
                        .url(img.getFileUrl())
                        .isCover(img.getIsCover())
                        .build())
                .toList();

        return com.ecommerce.auctionplatform.dto.respose.AuctionDetailResponse.builder()
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
                .sellerName(auction.getUser().getFullName())
                .images(imageResponses)
                .build();
    }

    public java.util.List<com.ecommerce.auctionplatform.dto.respose.BidResponse> getAuctionBids(UUID id) {
        java.util.List<Bid> bids = bidRepository.findByAuctionIdOrderByBidTimeDesc(id);
        return bids.stream()
                .map(bid -> com.ecommerce.auctionplatform.dto.respose.BidResponse.builder()
                        .id(bid.getId())
                        .bidAmount(bid.getBidAmount())
                        .bidTime(bid.getBidTime())
                        .isWinning(bid.getIsWinning())
                        .bidderId(bid.getUser().getId())
                        .bidderName(bid.getUser().getFullName()) // Maybe mask this later
                        .build())
                .toList();
    }

    @Transactional
    public com.ecommerce.auctionplatform.dto.respose.BidResponse placeBid(UUID auctionId, com.ecommerce.auctionplatform.dto.request.BidRequest request) {
        String profileId = SecurityUtils.getCurrentProfileId()
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        User user = userRepository.findById(UUID.fromString(profileId))
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new AppException(ErrorCode.AUCTION_NOT_FOUND));

        if (auction.getStatus() != AuctionStatus.ACTIVE) {
            throw new AppException(ErrorCode.BAD_REQUEST);
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
                        .type(com.ecommerce.auctionplatform.entity.enums.TransactionType.AUCTION_DEPOSIT)
                        .amount(auction.getDepositAmount())
                        .status(com.ecommerce.auctionplatform.entity.enums.TransactionStatus.SUCCESS)
                        .referenceType("REGISTRATION")
                        .build();
                transactionRepository.save(tx);

                registration = AuctionRegistration.builder()
                        .auction(auction)
                        .user(user)
                        .depositAmount(auction.getDepositAmount())
                        .depositStatus(com.ecommerce.auctionplatform.entity.enums.DepositStatus.PAID)
                        .registrationStatus(com.ecommerce.auctionplatform.entity.enums.RegistrationStatus.APPROVED)
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
            if (java.time.temporal.ChronoUnit.SECONDS.between(LocalDateTime.now(), auction.getEndTime()) < 60) {
                auction.setEndTime(auction.getEndTime().plusMinutes(auction.getExtendMinutes()));
                extended = true;
            }
        }

        auctionRepository.save(auction);

        com.ecommerce.auctionplatform.dto.respose.BidResponse response = com.ecommerce.auctionplatform.dto.respose.BidResponse.builder()
                .id(bid.getId())
                .bidAmount(bid.getBidAmount())
                .bidTime(bid.getBidTime())
                .isWinning(true)
                .bidderId(user.getId())
                .bidderName(user.getFullName())
                .build();

        java.util.Map<String, Object> message = new java.util.HashMap<>();
        message.put("type", "new_bid");
        message.put("bid_amount", bid.getBidAmount());
        message.put("bidder_id", user.getId());
        if (extended) {
            message.put("extended", true);
            message.put("end_time", auction.getEndTime().toString());
        }
        messagingTemplate.convertAndSend("/topic/auction/" + auctionId, message);

        return response;
    }
}
