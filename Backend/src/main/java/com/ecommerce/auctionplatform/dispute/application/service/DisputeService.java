package com.ecommerce.auctionplatform.dispute.application.service;

import com.ecommerce.auctionplatform.dispute.application.dto.command.CreateDisputeCommand;
import com.ecommerce.auctionplatform.dispute.application.dto.command.ResolveDisputeCommand;
import com.ecommerce.auctionplatform.dispute.application.dto.response.DisputeResponse;
import com.ecommerce.auctionplatform.dispute.application.dto.response.EvidenceResponse;
import com.ecommerce.auctionplatform.dispute.application.port.in.DisputeUseCase;
import com.ecommerce.auctionplatform.dispute.application.port.out.DisputeContextPort;
import com.ecommerce.auctionplatform.dispute.application.port.out.DisputeNotificationPort;
import com.ecommerce.auctionplatform.dispute.application.port.out.DisputeOrderView;
import com.ecommerce.auctionplatform.dispute.application.port.out.DisputePolicyPort;
import com.ecommerce.auctionplatform.dispute.domain.enums.DisputeStatus;
import com.ecommerce.auctionplatform.dispute.domain.model.Dispute;
import com.ecommerce.auctionplatform.dispute.domain.model.DisputeEvidence;
import com.ecommerce.auctionplatform.dispute.domain.repository.DisputeRepository;
import com.ecommerce.auctionplatform.dispute.domain.repository.DisputeEvidenceRepository;
import com.ecommerce.auctionplatform.shared.application.exception.AppException;
import com.ecommerce.auctionplatform.shared.application.exception.ErrorCode;
import com.ecommerce.auctionplatform.shared.application.exception.FileStorageException;
import com.ecommerce.auctionplatform.shared.application.model.FileContent;
import com.ecommerce.auctionplatform.shared.application.port.out.CurrentUserProvider;
import com.ecommerce.auctionplatform.shared.application.port.out.FileStoragePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DisputeService implements DisputeUseCase {
    private static final int BUYER_WIN_SELLER_PENALTY = 20;
    private static final int SELLER_WIN_BUYER_PENALTY = 10;
    private static final int MAX_EVIDENCE_IMAGES = 5;

    private final DisputeRepository disputeRepository;
    private final DisputeContextPort contextPort;
    private final DisputeEvidenceRepository evidenceRepository;
    private final DisputeNotificationPort notificationPort;
    private final DisputePolicyPort policyPort;
    private final FileStoragePort fileStoragePort;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public DisputeResponse createDispute(CreateDisputeCommand request, List<FileContent> files) {
        UUID claimantId = currentUserId();
        validateCreateRequest(request);
        DisputeOrderView order = contextPort.findOrder(request.getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        validateEligibility(order, claimantId);

        Dispute dispute = disputeRepository.save(Dispute.builder()
                .orderId(order.id())
                .claimantId(claimantId)
                .reason(request.getReason())
                .description(request.getDescription())
                .status(DisputeStatus.OPEN)
                .build());

        uploadEvidence(files, dispute.getId());
        contextPort.markOrderDisputed(order.id());
        notifyOpened(order, dispute.getId());
        return toResponse(dispute);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DisputeResponse> getMyDisputes() {
        return disputeRepository.findByClaimantIdOrderByCreatedAtDesc(currentUserId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DisputeResponse> getAllDisputes() {
        return disputeRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DisputeResponse getDisputeDetail(UUID disputeId) {
        return disputeRepository.findById(disputeId)
                .map(this::toResponse)
                .orElseThrow(() -> new AppException(ErrorCode.DISPUTE_NOT_FOUND));
    }

    @Override
    @Transactional
    public DisputeResponse resolveDispute(UUID disputeId, ResolveDisputeCommand request) {
        validateResolveRequest(request);
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new AppException(ErrorCode.DISPUTE_NOT_FOUND));
        if (dispute.getStatus() == DisputeStatus.RESOLVED || dispute.getStatus() == DisputeStatus.CLOSED) {
            throw new AppException(ErrorCode.DISPUTE_ALREADY_RESOLVED);
        }

        UUID adminId = currentUserId();
        DisputeOrderView order = contextPort.findOrder(dispute.getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        switch (request.getOutcome().toUpperCase()) {
            case "BUYER_WIN" -> resolveBuyerWin(order, dispute);
            case "SELLER_WIN" -> resolveSellerWin(order, dispute, adminId);
            default -> throw new AppException(ErrorCode.INVALID_DISPUTE_OUTCOME);
        }

        dispute.resolve(adminId, request.getResolution());
        return toResponse(disputeRepository.save(dispute));
    }

    private void validateEligibility(DisputeOrderView order, UUID claimantId) {
        if (!order.buyerId().equals(claimantId)) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }
        if (!order.eligibleForDispute()) {
            throw new AppException(ErrorCode.ORDER_NOT_ELIGIBLE_FOR_DISPUTE);
        }
        if (order.updatedAt().plusDays(policyPort.daysToExpire()).isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.DISPUTE_EXPIRED);
        }
        if (disputeRepository.existsByOrderIdAndStatusIn(
                order.id(), List.of(DisputeStatus.OPEN, DisputeStatus.UNDER_REVIEW))) {
            throw new AppException(ErrorCode.DISPUTE_ALREADY_EXISTS);
        }
    }

    private void resolveBuyerWin(DisputeOrderView order, Dispute dispute) {
        contextPort.settleBuyerWin(order, dispute.getId());
        contextPort.decreaseReputation(order.sellerId(), BUYER_WIN_SELLER_PENALTY,
                "Thua khiếu nại", dispute.getId());
        notifyResult(order.buyerId(), "Thắng", "Bạn đã thắng khiếu nại. Tiền đã được hoàn về ví.", dispute.getId());
        notifyResult(order.sellerId(), "Thua", "Bạn đã thua khiếu nại. Đơn hàng bị huỷ và bạn bị trừ uy tín.", dispute.getId());
    }

    private void resolveSellerWin(DisputeOrderView order, Dispute dispute, UUID adminId) {
        contextPort.settleSellerWin(order, dispute.getId(), adminId);
        contextPort.decreaseReputation(order.buyerId(), SELLER_WIN_BUYER_PENALTY,
                "Mở khiếu nại không hợp lệ", dispute.getId());
        notifyResult(order.sellerId(), "Thắng", "Bạn đã thắng khiếu nại. Tiền bán hàng đã được giải ngân.", dispute.getId());
        notifyResult(order.buyerId(), "Thua", "Khiếu nại bị từ chối. Bạn bị trừ uy tín.", dispute.getId());
    }

    private void uploadEvidence(List<FileContent> files, UUID disputeId) {
        if (files == null) {
            return;
        }
        for (int index = 0; index < Math.min(files.size(), MAX_EVIDENCE_IMAGES); index++) {
            try {
                String url = fileStoragePort.uploadFile(files.get(index), "disputes/" + disputeId);
                evidenceRepository.save(DisputeEvidence.builder()
                        .disputeId(disputeId)
                        .fileUrl(url)
                        .sortOrder(index)
                        .description("Bằng chứng khiếu nại #" + (index + 1))
                        .build());
            } catch (FileStorageException exception) {
                log.warn("Unable to upload evidence {} for dispute {}", index, disputeId, exception);
            }
        }
    }

    private void notifyOpened(DisputeOrderView order, UUID disputeId) {
        String message = "Người mua đã mở khiếu nại cho đơn hàng " + order.trackingCode();
        notificationPort.notify(order.sellerId(), "DISPUTE_OPENED", "Đơn hàng bị khiếu nại", message, disputeId);
        contextPort.findAdminId().ifPresent(adminId -> notificationPort.notify(
                adminId, "DISPUTE_OPENED", "Đơn hàng bị khiếu nại", message, disputeId));
    }

    private void notifyResult(UUID userId, String result, String message, UUID disputeId) {
        notificationPort.notify(userId, "DISPUTE_RESOLVED", "Kết quả khiếu nại: " + result, message, disputeId);
    }

    private DisputeResponse toResponse(Dispute dispute) {
        DisputeOrderView order = dispute.getOrderId() == null
                ? null
                : contextPort.findOrder(dispute.getOrderId()).orElse(null);
        return DisputeResponse.builder()
                .id(dispute.getId())
                .orderId(dispute.getOrderId())
                .productName(order == null ? null : order.productName())
                .productImageUrl(order == null ? null : order.productImageUrl())
                .claimantName(contextPort.findUserName(dispute.getClaimantId()).orElse(null))
                .sellerName(order == null ? null : contextPort.findUserName(order.sellerId()).orElse(null))
                .buyerName(order == null ? null : contextPort.findUserName(order.buyerId()).orElse(null))
                .orderAmount(order == null ? null : order.amount())
                .reason(dispute.getReason())
                .description(dispute.getDescription())
                .evidences(evidenceRepository.findByDisputeIdOrderBySortOrder(dispute.getId()).stream()
                        .map(evidence -> EvidenceResponse.builder()
                                .url(evidence.getFileUrl())
                                .isCover(false)
                                .build())
                        .toList())
                .status(dispute.getStatus())
                .resolvedByName(dispute.getResolvedById() == null
                        ? null
                        : contextPort.findUserName(dispute.getResolvedById()).orElse(null))
                .resolution(dispute.getResolution())
                .createdAt(dispute.getCreatedAt())
                .resolvedAt(dispute.getResolvedAt())
                .build();
    }

    private UUID currentUserId() {
        return currentUserProvider.currentProfileId()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
    }

    private void validateCreateRequest(CreateDisputeCommand request) {
        if (request == null || request.getOrderId() == null || request.getReason() == null
                || request.getReason().isBlank() || request.getReason().length() > 255
                || request.getDescription() != null && request.getDescription().length() > 2000) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }
    }

    private void validateResolveRequest(ResolveDisputeCommand request) {
        if (request == null || request.getOutcome() == null || request.getOutcome().isBlank()
                || request.getResolution() == null || request.getResolution().isBlank()) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }
    }
}
