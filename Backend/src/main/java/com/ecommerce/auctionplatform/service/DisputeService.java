package com.ecommerce.auctionplatform.service;

import com.ecommerce.auctionplatform.dto.request.CreateDisputeRequest;
import com.ecommerce.auctionplatform.dto.request.ResolveDisputeRequest;
import com.ecommerce.auctionplatform.dto.respose.DisputeResponse;
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
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class DisputeService {

    DisputeRepository disputeRepository;
    OrderRepository orderRepository;
    UserRepository userRepository;
    AccountRepository accountRepository;
    WalletRepository walletRepository;
    TransactionRepository transactionRepository;
    ReputationHistoryRepository reputationHistoryRepository;
    ImageRepository imageRepository;
    CloudinaryService cloudinaryService;
    NotificationService notificationService;

    @NonFinal
    @Value("${app.days-to-expire}")
    protected int daysToExpire;

    // Các hằng số (Constants) giúp code dễ bảo trì hơn
    private static final BigDecimal PLATFORM_FEE_RATE = new BigDecimal("0.05");
    private static final int BUYER_WIN_SELLER_PENALTY = -20;
    private static final int SELLER_WIN_BUYER_PENALTY = -10;
    private static final int MAX_EVIDENCE_IMAGES = 5;

    @Transactional
    public DisputeResponse createDispute(CreateDisputeRequest request, MultipartFile[] files) {
        User currentUser = getCurrentUser();
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        validateDisputeEligibility(order, currentUser);

        Dispute dispute = disputeRepository.save(Dispute.builder()
                .order(order)
                .claimant(currentUser)
                .reason(request.getReason())
                .description(request.getDescription())
                .status(DisputeStatus.OPEN)
                .build());

        handleEvidenceUploads(files, dispute.getId());

        order.setStatus(OrderStatus.DISPUTED); // Dirty checking sẽ tự lưu order

        notifyDisputeOpened(order, dispute.getId());

        return mapToResponse(dispute);
    }

    @Transactional(readOnly = true)
    public List<DisputeResponse> getMyDisputes() {
        return disputeRepository.findByClaimantIdOrderByCreatedAtDesc(getCurrentUser().getId())
                .stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<DisputeResponse> getAllDisputes() {
        return disputeRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public DisputeResponse getDisputeDetail(UUID disputeId) {
        return disputeRepository.findById(disputeId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new AppException(ErrorCode.DISPUTE_NOT_FOUND));
    }

    @Transactional
    @PreAuthorize(PredefinedRole.HAS_ROLE_ADMIN)
    public DisputeResponse resolveDispute(UUID disputeId, ResolveDisputeRequest request) {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new AppException(ErrorCode.DISPUTE_NOT_FOUND));

        if (dispute.getStatus() == DisputeStatus.RESOLVED || dispute.getStatus() == DisputeStatus.CLOSED) {
            throw new AppException(ErrorCode.DISPUTE_ALREADY_RESOLVED);
        }

        User admin = getCurrentUser();

        switch (request.getOutcome().toUpperCase()) {
            case "BUYER_WIN" -> processBuyerWin(dispute);
            case "SELLER_WIN" -> processSellerWin(dispute, admin);
            default -> throw new AppException(ErrorCode.INVALID_DISPUTE_OUTCOME);
        }

        dispute.setResolution(request.getResolution());
        dispute.setStatus(DisputeStatus.RESOLVED);
        dispute.setResolvedBy(admin);
        dispute.setResolvedAt(LocalDateTime.now());

        return mapToResponse(dispute); // Dirty checking tự update Dispute
    }


    private void validateDisputeEligibility(Order order, User currentUser) {
        if (!order.getBuyer().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }
        if (order.getStatus() != OrderStatus.PAID && order.getStatus() != OrderStatus.SHIPPING) {
            throw new AppException(ErrorCode.ORDER_NOT_ELIGIBLE_FOR_DISPUTE);
        }
        if (order.getUpdatedAt().plusDays(daysToExpire).isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.DISPUTE_EXPIRED);
        }
        if (disputeRepository.existsByOrderIdAndStatusIn(order.getId(), List.of(DisputeStatus.OPEN, DisputeStatus.UNDER_REVIEW))) {
            throw new AppException(ErrorCode.DISPUTE_ALREADY_EXISTS);
        }
    }

    private void handleEvidenceUploads(MultipartFile[] files, UUID disputeId) {
        if (files == null || files.length == 0) return;

        int limit = Math.min(files.length, MAX_EVIDENCE_IMAGES);
        for (int i = 0; i < limit; i++) {
            try {
                String fileUrl = cloudinaryService.uploadFile(files[i], "disputes/" + disputeId);
                imageRepository.save(Image.builder()
                        .referenceType(ImageReferenceType.DISPUTE)
                        .referenceId(disputeId)
                        .fileUrl(fileUrl)
                        .sortOrder(i)
                        .description("Bằng chứng khiếu nại #" + (i + 1))
                        .build());
            } catch (Exception e) {
                log.error("Failed to upload dispute evidence for Dispute {}: {}", disputeId, e.getMessage());
            }
        }
    }

    private void notifyDisputeOpened(Order order, UUID disputeId) {
        String title = "Đơn hàng bị khiếu nại";
        String message = "Người mua đã mở khiếu nại cho đơn hàng " + order.getTrackingCode();

        notificationService.sendNotification(order.getSeller(), "DISPUTE_OPENED", title, message, "DISPUTE", disputeId);

        userRepository.findFirstByAccount_Role_Name(PredefinedRole.ADMIN.name())
                .ifPresent(
                        admin -> notificationService.sendNotification(
                                admin, "DISPUTE_OPENED", title, message, "DISPUTE", disputeId)
                );
    }

    private void processBuyerWin(Dispute dispute) {
        Order order = dispute.getOrder();
        BigDecimal amount = order.getTotalAmount();
        Wallet sellerWallet = getWalletByUserId(order.getSeller().getId());
        Wallet buyerWallet = getWalletByUserId(order.getBuyer().getId());

        sellerWallet.setFrozenBalance(sellerWallet.getFrozenBalance().subtract(amount));
        buyerWallet.setAvailableBalance(buyerWallet.getAvailableBalance().add(amount));

        saveTransaction(buyerWallet, amount, TransactionType.DISPUTE_REFUND,
                "Hoàn tiền do thắng khiếu nại đơn hàng " + order.getTrackingCode(), dispute);

        updateReputation(order.getSeller(), BUYER_WIN_SELLER_PENALTY, "Thua khiếu nại (bị trừ uy tín mạnh)", dispute);

        order.setStatus(OrderStatus.CANCELLED);

        sendDisputeNotification(order.getBuyer(), "Thắng", "Bạn đã thắng khiếu nại. Tiền đã được hoàn về ví.", dispute.getId());
        sendDisputeNotification(order.getSeller(), "Thua", "Bạn đã thua khiếu nại. Đơn hàng bị huỷ và bạn bị trừ uy tín.", dispute.getId());
    }

    private void processSellerWin(Dispute dispute, User admin) {
        Order order = dispute.getOrder();
        BigDecimal amount = order.getTotalAmount();
        Wallet sellerWallet = getWalletByUserId(order.getSeller().getId());

        sellerWallet.setFrozenBalance(sellerWallet.getFrozenBalance().subtract(amount));

        BigDecimal platformFee = amount.multiply(PLATFORM_FEE_RATE);
        BigDecimal sellerReceived = amount.subtract(platformFee);
        sellerWallet.setAvailableBalance(sellerWallet.getAvailableBalance().add(sellerReceived));

        saveTransaction(sellerWallet, sellerReceived, TransactionType.ESCROW_RELEASE,
                "Giải ngân (Thắng khiếu nại) đơn hàng " + order.getTrackingCode(), dispute);

        if (admin != null) {
            Wallet adminWallet = getOrCreateAdminWallet(admin);
            adminWallet.setAvailableBalance(adminWallet.getAvailableBalance().add(platformFee));
            saveTransaction(adminWallet, platformFee, TransactionType.PLATFORM_FEE,
                    "Phí nền tảng từ đơn hàng " + order.getTrackingCode(), null);
        }

        updateReputation(order.getBuyer(), SELLER_WIN_BUYER_PENALTY, "Mở khiếu nại vô căn cứ", dispute);

        order.setStatus(OrderStatus.COMPLETED);

        sendDisputeNotification(order.getSeller(), "Thắng", "Bạn đã thắng khiếu nại. Tiền bán hàng đã được giải ngân.", dispute.getId());
        sendDisputeNotification(order.getBuyer(), "Thua", "Khiếu nại bị từ chối. Bạn bị trừ uy tín do khiếu nại không hợp lệ.", dispute.getId());
    }

    private Wallet getWalletByUserId(UUID userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));
    }

    private Wallet getOrCreateAdminWallet(User admin) {
        return walletRepository.findByUserId(admin.getId()).orElseGet(() ->
                walletRepository.save(Wallet.builder()
                        .user(admin)
                        .availableBalance(BigDecimal.ZERO)
                        .frozenBalance(BigDecimal.ZERO)
                        .status(WalletStatus.ACTIVE)
                        .createdAt(LocalDateTime.now())
                        .build())
        );
    }

    private void saveTransaction(Wallet wallet, BigDecimal amount, TransactionType type, String note, Dispute dispute) {
        Transaction tx = Transaction.builder()
                .wallet(wallet)
                .amount(amount)
                .type(type)
                .status(TransactionStatus.SUCCESS)
                .note(note)
                .referenceType(dispute != null ? "DISPUTE" : null)
                .referenceId(dispute != null ? dispute.getId() : null)
                .build();
        transactionRepository.save(tx);
    }

    private void updateReputation(User user, int scoreChange, String reason, Dispute dispute) {
        user.setReputationScore(user.getReputationScore() + scoreChange);
        reputationHistoryRepository.save(ReputationHistory.builder()
                .user(user)
                .scoreChange(scoreChange)
                .reason(reason)
                .dispute(dispute)
                .build());
    }

    private void sendDisputeNotification(User user, String result, String message, UUID disputeId) {
        notificationService.sendNotification(user, "DISPUTE_RESOLVED", "Kết quả khiếu nại: " + result, message, "DISPUTE", disputeId);
    }

    private DisputeResponse mapToResponse(Dispute dispute) {
        String productName = null;
        String productImageUrl = null;
        BigDecimal orderAmount = null;
        String sellerName = null;
        String buyerName = null;

        if (dispute.getOrder() != null) {
            Order order = dispute.getOrder();
            orderAmount = order.getTotalAmount();
            sellerName = order.getSeller() != null ? order.getSeller().getName() : null;
            buyerName = order.getBuyer() != null ? order.getBuyer().getName() : null;

            if (order.getAuctionRecord() != null && order.getAuctionRecord().getAuction() != null) {
                Product product = order.getAuctionRecord().getAuction().getProduct();
                if (product != null) {
                    productName = product.getName();
                    productImageUrl = imageRepository.findFirstByProductIdOrderByIsCoverDesc(product.getId())
                            .map(Image::getFileUrl).orElse(null);
                }
            }
        }

        List<ImageResponse> evidences = imageRepository.findByDisputeId(dispute.getId()).stream()
                .map(img -> ImageResponse.builder()
                        .url(img.getFileUrl())
                        .isCover(img.getIsCover())
                        .build())
                .toList();

        return DisputeResponse.builder()
                .id(dispute.getId())
                .orderId(dispute.getOrder() != null ? dispute.getOrder().getId() : null)
                .productName(productName)
                .productImageUrl(productImageUrl)
                .claimantName(dispute.getClaimant() != null ? dispute.getClaimant().getName() : null)
                .sellerName(sellerName)
                .buyerName(buyerName)
                .orderAmount(orderAmount)
                .reason(dispute.getReason())
                .description(dispute.getDescription())
                .evidences(evidences)
                .status(dispute.getStatus())
                .resolvedByName(dispute.getResolvedBy() != null ? dispute.getResolvedBy().getName() : null)
                .resolution(dispute.getResolution())
                .createdAt(dispute.getCreatedAt())
                .resolvedAt(dispute.getResolvedAt())
                .build();
    }

    private User getCurrentUser() {
        UUID userProfileId = UUID.fromString(SecurityUtils.getCurrentProfileId()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED)));
        return userRepository.findById(userProfileId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }
}