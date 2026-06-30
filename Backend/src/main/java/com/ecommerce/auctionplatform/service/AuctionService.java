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
import lombok.extern.slf4j.Slf4j;

import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
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
    ScheduleService scheduleService;
    AuctionRecordRepository auctionRecordRepository;
    OrderRepository orderRepository;

    @NonFinal
    protected int DEDUCT_REPUTATION_SCORE = 20;



    @Transactional
    public AuctionCreationResponse createAuction(AuctionCreationRequest request) throws IOException {
        User user = getCurrentUser();

        if (user.getDob() == null || Period.between(user.getDob(), LocalDate.now()).getYears() < 18) {
            throw new AppException(ErrorCode.USER_UNDERAGE);
        }

        if(user.getVerificationStatus() == null || !user.getVerificationStatus().name().equals("VERIFIED")){
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
            Auction oldAuction = getAuction(request.getRelistId());

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

        AuctionStatus initialStatus = AuctionStatus.APPROVED;
        if (request.getStartPrice().compareTo(new BigDecimal("50000000")) >= 0) {
            initialStatus = AuctionStatus.PENDING;
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
                .status(initialStatus)
                .autoExtend(request.getAutoExtend() != null ? request.getAutoExtend() : false)
                .extendMinutes(request.getExtendMinutes() != null ? request.getExtendMinutes() : 0)
                //  reservePrice and buyNowPrice if neeeded
                .build();
        auction = auctionRepository.save(auction);

        if (initialStatus == AuctionStatus.APPROVED) {
            scheduleService.scheduleAuctionActivation(auction.getId().toString(), auction.getStartTime());
            scheduleService.scheduleAuctionClosure(auction.getId().toString(), auction.getEndTime());
        }

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
        Auction auction = getAuction(id.toString()); 

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
                        .bidderName(bid.getUser().getName()) 
                        .build())
                .toList();
    }

    @Transactional
    public BidResponse placeBid(UUID auctionId, BidRequest request) {

        User user = getCurrentUser();
        if (user.getVerificationStatus() != VerificationStatus.VERIFIED) {
            throw new AppException(ErrorCode.UNVERIFIED_USER);
        }

        if (user.getDob() == null || java.time.Period.between(user.getDob(), java.time.LocalDate.now()).getYears() < 18) {
            throw new AppException(ErrorCode.USER_UNDERAGE);
        }

        Auction auction = auctionRepository.findByIdWithLock(auctionId)
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
                scheduleService.scheduleAuctionClosure(auctionId.toString(), auction.getEndTime());
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

    @Transactional
    public void activateAuction(String auctionId) {
        Auction auction = getAuction(auctionId);

        if (auction.getStatus() == AuctionStatus.APPROVED || auction.getStatus() == AuctionStatus.PENDING) {
            auction.setStatus(AuctionStatus.ACTIVE);
            auctionRepository.save(auction);

            Map<String, Object> message = new HashMap<>();
            message.put("type", "auction_activated");
            message.put("auction_id", auctionId);
            message.put("status", "ACTIVE");
            messagingTemplate.convertAndSend("/topic/auction/" + auctionId, (Object) message);

        }
    }

    @Transactional
    public void closeAuction(String auctionId) {
        Auction auction = getAuction(auctionId);

        if (auction.getStatus() != AuctionStatus.ACTIVE && auction.getStatus() != AuctionStatus.EXTENDED) {
            return;
        }

        List<Bid> bids = bidRepository.findByAuctionIdOrderByBidAmountDesc(auction.getId());

        if (bids.isEmpty()) {
            // No bids: auction failed
            auction.setStatus(AuctionStatus.FAILED);
            auctionRepository.save(auction);
        } else {
            auction.setStatus(AuctionStatus.CLOSED);
            auctionRepository.save(auction);

            int maxRanks = Math.min(3, bids.size());
            LocalDateTime paymentDeadline = LocalDateTime.now().plusHours(48);

            // Deduplicate: keep only top bid per user
            List<Bid> uniqueTopBids = new ArrayList<>();
            Set<UUID> seenUsers = new LinkedHashSet<>();
            for (Bid b : bids) {
                if (seenUsers.add(b.getUser().getId())) {
                    uniqueTopBids.add(b);
                    if (uniqueTopBids.size() >= maxRanks) break;
                }
            }

            for (int i = 0; i < uniqueTopBids.size(); i++) {
                Bid topBid = uniqueTopBids.get(i);
                int rank = i + 1;
                AuctionRecordStatus recordStatus = (rank == 1)
                        ? AuctionRecordStatus.PENDING_PAYMENT
                        : AuctionRecordStatus.LOSE; // rank 2,3 are standby, treated as LOSE until rank 1 bungs

                AuctionRecord record = AuctionRecord.builder()
                        .auction(auction)
                        .user(topBid.getUser())
                        .bid(topBid)
                        .winningRank(rank)
                        .finalPrice(topBid.getBidAmount())
                        .status(recordStatus)
                        .expiryTime(paymentDeadline)
                        .build();
                auctionRecordRepository.save(record);

                if (rank == 1) {
                    Order order = Order.builder()
                            .auctionRecord(record)
                            .buyer(topBid.getUser())
                            .seller(auction.getUser())
                            .totalAmount(topBid.getBidAmount())
                            .status(OrderStatus.PENDING_PAYMENT)
                            .build();
                    orderRepository.save(order);
                }
            }

            // --- Refund deposits for all losers (those NOT in top ranks) ---
            List<AuctionRegistration> registrations = auctionRegistrationRepository
                    .findByAuctionId(auction.getId());

            // Collect winner user IDs (top ranks – still hold deposit until payment)
            Set<UUID> topRankUserIds = uniqueTopBids.stream()
                    .map(b -> b.getUser().getId())
                    .collect(Collectors.toSet());

            for (AuctionRegistration reg : registrations) {
                // Skip top rank holders – their deposit stays frozen until they pay or bung
                if (topRankUserIds.contains(reg.getUser().getId())) continue;
                if (reg.getDepositStatus() != DepositStatus.PAID) continue;

                // Refund deposit
                Wallet wallet = walletRepository.findByUser(reg.getUser()).orElse(null);
                if (wallet == null) continue;

                wallet.setFrozenBalance(wallet.getFrozenBalance().subtract(reg.getDepositAmount()));
                wallet.setAvailableBalance(wallet.getAvailableBalance().add(reg.getDepositAmount()));
                walletRepository.save(wallet);

                reg.setDepositStatus(DepositStatus.REFUNDED);
                auctionRegistrationRepository.save(reg);

                Transaction refundTx = Transaction.builder()
                        .wallet(wallet)
                        .type(TransactionType.AUCTION_DEPOSIT_REFUND)
                        .amount(reg.getDepositAmount())
                        .status(TransactionStatus.SUCCESS)
                        .referenceType("REGISTRATION")
                        .referenceId(reg.getId())
                        .note("Hoàn cọc sau khi kết thúc phiên đấu giá")
                        .build();
                transactionRepository.save(refundTx);
            }
        }

        // Broadcast to all viewers
        Map<String, Object> message = new HashMap<>();
        message.put("type", "auction_closed");
        message.put("auction_id", auctionId);
        message.put("status", auction.getStatus().name());
        messagingTemplate.convertAndSend("/topic/auction/" + auctionId, (Object) message);
    }

    private User getCurrentUser(){
        UUID profileId = UUID.fromString(SecurityUtils.getCurrentProfileId().orElseThrow(
                ()-> new AppException(ErrorCode.UNAUTHORIZED)));
        return userRepository.findById(profileId).orElseThrow(
                ()->new AppException(ErrorCode.USER_NOT_FOUND)
        );

    }
    private Auction getAuction(String auctionId){
        return auctionRepository.findById(UUID.fromString(auctionId))
                .orElseThrow(() -> new AppException(ErrorCode.AUCTION_NOT_FOUND));
    }

    @Transactional
    public void processAbandonedOrders() {
        List<AuctionRecord> expired = auctionRecordRepository
                .findByStatusAndExpiryTimeBefore(AuctionRecordStatus.PENDING_PAYMENT, LocalDateTime.now());

        for (AuctionRecord record : expired) {
            try {
                handleOneAbandonedRecord(record);
            } catch (Exception e) {
                log.error("Failed to process abandoned record {}: {}", record.getId(), e.getMessage());
            }
        }
    }

    private void handleOneAbandonedRecord(AuctionRecord record) {
        Auction auction = record.getAuction();
        User abandoner = record.getUser();

        // Mark record as CANCELLED
        record.setStatus(AuctionRecordStatus.CANCELLED);
        auctionRecordRepository.save(record);

        // Forfeit the abandoner's deposit
        AuctionRegistration reg = auctionRegistrationRepository
                .findByAuctionIdAndUserId(auction.getId(), abandoner.getId())
                .orElse(null);

        if (reg != null && reg.getDepositStatus() == DepositStatus.PAID) {
            Wallet wallet = walletRepository.findByUser(abandoner).orElse(null);
            if (wallet != null) {
                // Remove from frozen balance (deposit is forfeited, not returned)
                wallet.setFrozenBalance(wallet.getFrozenBalance().subtract(reg.getDepositAmount()));
                walletRepository.save(wallet);

                reg.setDepositStatus(DepositStatus.FORFEITED);
                auctionRegistrationRepository.save(reg);

                Transaction forfeitTx = Transaction.builder()
                        .wallet(wallet)
                        .type(TransactionType.AUCTION_DEPOSIT_FORFEIT)
                        .amount(reg.getDepositAmount())
                        .status(TransactionStatus.SUCCESS)
                        .referenceType("AUCTION_RECORD")
                        .referenceId(record.getId())
                        .note("Tịch thu cọc do bùng hàng phiên đấu giá " + auction.getId())
                        .build();
                transactionRepository.save(forfeitTx);
            }
        }

        // Deduct reputation score 
        int newScore = Math.max(0, (abandoner.getReputationScore() != null ? abandoner.getReputationScore() : 100) - DEDUCT_REPUTATION_SCORE);
        abandoner.setReputationScore(newScore);
        userRepository.save(abandoner);

        // Find next standby bidder (rank 2, then 3, etc.)
        List<AuctionRecord> standbyRecords = auctionRecordRepository
                .findByAuctionIdAndStatusOrderByWinningRankAsc(auction.getId(), AuctionRecordStatus.LOSE);

        if (!standbyRecords.isEmpty()) {
            // Promote next rank
            AuctionRecord nextRecord = standbyRecords.get(0);
            LocalDateTime newDeadline = LocalDateTime.now().plusHours(48);
            nextRecord.setStatus(AuctionRecordStatus.PENDING_PAYMENT);
            nextRecord.setExpiryTime(newDeadline);
            auctionRecordRepository.save(nextRecord);

            // Create new Order for the promoted winner
            Order order = Order.builder()
                    .auctionRecord(nextRecord)
                    .buyer(nextRecord.getUser())
                    .seller(auction.getUser())
                    .totalAmount(nextRecord.getFinalPrice())
                    .status(OrderStatus.PENDING_PAYMENT)
                    .build();
            orderRepository.save(order);

            log.info("Auction {}: rank {} promoted to winner after bung hang. New deadline: {}",
                    auction.getId(), nextRecord.getWinningRank(), newDeadline);

            // Notify winner via WebSocket
            Map<String, Object> msg = new HashMap<>();
            msg.put("type", "winner_promoted");
            msg.put("auction_id", auction.getId().toString());
            msg.put("new_winner_id", nextRecord.getUser().getId().toString());
            msg.put("payment_deadline", newDeadline.toString());
            messagingTemplate.convertAndSend("/topic/auction/" + auction.getId(), (Object) msg);

        } else {
            // No more standby bidders → auction ultimately failed
            auction.setStatus(AuctionStatus.FAILED);
            auctionRepository.save(auction);

            log.info("Auction {} set to FAILED — no more standby bidders after bung hang.", auction.getId());

            Map<String, Object> msg = new HashMap<>();
            msg.put("type", "auction_failed");
            msg.put("auction_id", auction.getId().toString());
            msg.put("reason", "no_more_bidders");
            messagingTemplate.convertAndSend("/topic/auction/" + auction.getId(), (Object) msg);
        }
    }

}

