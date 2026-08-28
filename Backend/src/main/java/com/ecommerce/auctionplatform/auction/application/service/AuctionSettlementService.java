package com.ecommerce.auctionplatform.auction.application.service;

import com.ecommerce.auctionplatform.auction.application.port.in.AuctionSettlementUseCase;
import com.ecommerce.auctionplatform.auction.application.port.out.AuctionCatalogPort;
import com.ecommerce.auctionplatform.auction.application.port.out.AuctionImageView;
import com.ecommerce.auctionplatform.auction.application.port.out.AuctionProductView;
import com.ecommerce.auctionplatform.auction.domain.enums.DepositStatus;
import com.ecommerce.auctionplatform.auction.domain.model.Auction;
import com.ecommerce.auctionplatform.auction.domain.model.AuctionRecord;
import com.ecommerce.auctionplatform.auction.domain.model.AuctionRegistration;
import com.ecommerce.auctionplatform.auction.domain.repository.AuctionRecordRepository;
import com.ecommerce.auctionplatform.auction.domain.repository.AuctionRegistrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuctionSettlementService implements AuctionSettlementUseCase {
    private final AuctionRecordRepository auctionRecordRepository;
    private final AuctionRegistrationRepository auctionRegistrationRepository;
    private final AuctionCatalogPort auctionCatalogPort;

    @Override
    @Transactional(readOnly = true)
    public Optional<AuctionRecordSummary> findRecord(UUID auctionRecordId) {
        return auctionRecordRepository.findById(auctionRecordId).map(this::toSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AuctionRecordSummary> findRecord(UUID auctionId, UUID userId) {
        return auctionRecordRepository.findByAuctionIdAndUserId(auctionId, userId).map(this::toSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuctionRegistrationSummary> findRegistrations(UUID auctionId) {
        return auctionRegistrationRepository.findByAuctionId(auctionId).stream()
                .map(this::toSummary)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AuctionRegistrationSummary> findRegistration(UUID auctionId, UUID userId) {
        return auctionRegistrationRepository.findByAuctionIdAndUserId(auctionId, userId).map(this::toSummary);
    }

    @Override
    @Transactional
    public void markRegistrationRefunded(UUID registrationId) {
        auctionRegistrationRepository.findById(registrationId).ifPresent(registration -> {
            registration.refundDeposit();
            auctionRegistrationRepository.save(registration);
        });
    }

    @Override
    @Transactional
    public void markRecordWon(UUID auctionRecordId) {
        auctionRecordRepository.findById(auctionRecordId).ifPresent(record -> {
            record.markWon();
            auctionRecordRepository.save(record);
        });
    }

    private AuctionRecordSummary toSummary(AuctionRecord record) {
        Auction auction = record.getAuction();
        UUID productId = auction == null ? null : auction.getProductId();
        AuctionProductView product = productId == null
                ? null
                : auctionCatalogPort.findProduct(productId).orElse(null);
        List<AuctionImageView> images = productId == null
                ? List.of()
                : auctionCatalogPort.findImages(productId);
        String imageUrl = images.stream()
                .filter(AuctionImageView::cover)
                .findFirst()
                .or(() -> images.stream().findFirst())
                .map(AuctionImageView::url)
                .orElse(null);

        return new AuctionRecordSummary(
                record.getId(),
                auction == null ? null : auction.getId(),
                auction == null ? null : auction.getUserId(),
                productId,
                product == null ? null : product.name(),
                imageUrl,
                auction == null ? null : auction.getDepositAmount(),
                auction == null ? null : auction.getPlatformFee(),
                record.getFinalPrice(),
                record.getExpiryTime());
    }

    private AuctionRegistrationSummary toSummary(AuctionRegistration registration) {
        return new AuctionRegistrationSummary(
                registration.getId(),
                registration.getAuction() == null ? null : registration.getAuction().getId(),
                registration.getUserId(),
                registration.getDepositAmount(),
                registration.getDepositStatus() == DepositStatus.PAID);
    }
}
