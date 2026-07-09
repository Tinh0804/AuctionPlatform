package com.ecommerce.auctionplatform.service;

import com.ecommerce.auctionplatform.dto.request.CreateDisputeRequest;
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
    NotificationRepository notificationRepository;

    @NonFinal
    @Value("${app.days-to-expire}")
    protected int daysToExpire;

    @Transactional
    public DisputeResponse createDispute(CreateDisputeRequest request, MultipartFile[] files) {
        User currentUser = getCurrentUser();
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getBuyer().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        if (order.getStatus() != OrderStatus.PAID && order.getStatus() != OrderStatus.SHIPPING) {
            throw new AppException(ErrorCode.ORDER_NOT_ELIGIBLE_FOR_DISPUTE);
        }

        // 7 days limit rule
        if (order.getUpdatedAt().plusDays(daysToExpire).isBefore(LocalDateTime.now())) {
             throw new AppException(ErrorCode.DISPUTE_EXPIRED);
        }

        if (disputeRepository.existsByOrderIdAndStatusIn(order.getId(), List.of(DisputeStatus.OPEN, DisputeStatus.UNDER_REVIEW))) {
            throw new AppException(ErrorCode.DISPUTE_ALREADY_EXISTS);
        }

        Dispute dispute = Dispute.builder()
                .order(order)
                .claimant(currentUser)
                .reason(request.getReason())
                .description(request.getDescription())
                .status(DisputeStatus.OPEN)
                .build();
        dispute = disputeRepository.save(dispute);

        if (files != null && files.length > 0) {
            int sortOrder = 0;
            // Limit up to 5 images
            int limit = Math.min(files.length, 5);
            for (int i = 0; i < limit; i++) {
                MultipartFile file = files[i];
                try {
                    String fileUrl = cloudinaryService.uploadFile(file, "disputes/" + dispute.getId());
                    Image image = Image.builder()
                            .referenceType(ImageReferenceType.DISPUTE)
                            .referenceId(dispute.getId())
                            .fileUrl(fileUrl)
                            .sortOrder(sortOrder)
                            .description("Bằng chứng khiếu nại #" + (i + 1))
                            .build();
                    imageRepository.save(image);
                    sortOrder++;
                } catch (Exception e) {
                    log.error("Failed to upload dispute evidence", e);
                }
            }
        }

        order.setStatus(OrderStatus.DISPUTED);
        orderRepository.save(order);

        // Notify seller and admin
        Notification notification = Notification.builder()
                .user(order.getSeller())
                .type("DISPUTE_OPENED")
                .title("Đơn hàng bị khiếu nại")
                .content("Người mua đã mở khiếu nại cho đơn hàng " + order.getTrackingCode())
                .referenceType("DISPUTE")
                .referenceId(dispute.getId())
                .build();
        notificationRepository.save(notification);

        return mapToResponse(dispute);
    }

    public List<DisputeResponse> getMyDisputes() {
        User currentUser = getCurrentUser();
        return disputeRepository.findByClaimantIdOrderByCreatedAtDesc(currentUser.getId())
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }



    public DisputeResponse getDisputeDetail(UUID disputeId) {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new AppException(ErrorCode.DISPUTE_NOT_FOUND));
        return mapToResponse(dispute);
    }


    private DisputeResponse mapToResponse(Dispute dispute) {
        String productName = null;
        String productImageUrl = null;
        BigDecimal orderAmount = null;

        if (dispute.getOrder() != null) {
            orderAmount = dispute.getOrder().getTotalAmount();
            if (dispute.getOrder().getAuctionRecord() != null && dispute.getOrder().getAuctionRecord().getAuction() != null) {
                Product product = dispute.getOrder().getAuctionRecord().getAuction().getProduct();
                if (product != null) {
                    productName = product.getName();
                    List<Image> images = imageRepository.findByProductIdOrderByIsCoverDesc(product.getId());
                    if (!images.isEmpty()) {
                        productImageUrl = images.get(0).getFileUrl();
                    }
                }
            }
        }

        List<Image> evidenceImages = imageRepository.findByDisputeId(dispute.getId());
        List<ImageResponse> evidences = evidenceImages.stream().map(img -> ImageResponse.builder()
                .url(img.getFileUrl())
                .isCover(img.getIsCover())
                .build()).collect(Collectors.toList());

        return DisputeResponse.builder()
                .id(dispute.getId())
                .orderId(dispute.getOrder() != null ? dispute.getOrder().getId() : null)
                .productName(productName)
                .productImageUrl(productImageUrl)
                .claimantName(dispute.getClaimant() != null ? dispute.getClaimant().getName() : null)
                .sellerName(dispute.getOrder() != null && dispute.getOrder().getSeller() != null ? dispute.getOrder().getSeller().getName() : null)
                .buyerName(dispute.getOrder() != null && dispute.getOrder().getBuyer() != null ? dispute.getOrder().getBuyer().getName() : null)
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
        UUID userProfileId = UUID.fromString(SecurityUtils.getCurrentProfileId().orElseThrow(()->
                new AppException(ErrorCode.UNAUTHORIZED)));
        return userRepository.findById(userProfileId).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_FOUND));
    }
}
