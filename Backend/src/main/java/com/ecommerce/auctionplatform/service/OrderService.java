package com.ecommerce.auctionplatform.service;

import com.ecommerce.auctionplatform.dto.request.EscrowPaymentRequest;
import com.ecommerce.auctionplatform.dto.request.ShippingUpdateRequest;
import com.ecommerce.auctionplatform.dto.respose.OrderResponse;
import com.ecommerce.auctionplatform.mapper.OrderMapper;
import com.ecommerce.auctionplatform.entity.*;
import com.ecommerce.auctionplatform.entity.enums.*;
import com.ecommerce.auctionplatform.exception.AppException;
import com.ecommerce.auctionplatform.exception.ErrorCode;
import com.ecommerce.auctionplatform.repository.*;
import com.ecommerce.auctionplatform.utils.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderService {
    OrderRepository orderRepository;
    WalletRepository walletRepository;
    TransactionRepository transactionRepository;
    AuctionRegistrationRepository auctionRegistrationRepository;
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    AuctionRecordRepository auctionRecordRepository;
    OrderMapper orderMapper;

    private User getCurrentUser() {
        UUID userProfileId = UUID.fromString(SecurityUtils.getCurrentProfileId().orElseThrow(()->
                new AppException(ErrorCode.UNAUTHORIZED)));
        return userRepository.findById(userProfileId).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    @Transactional
    public OrderResponse payOrderWithEscrow(UUID orderId, EscrowPaymentRequest request) {
        User buyer = getCurrentUser();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST)); 

        if (!order.getBuyer().getId().equals(buyer.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new AppException(ErrorCode.BAD_REQUEST); 
        }

        Wallet buyerWallet = walletRepository.findByUser(buyer)
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));

        if (buyerWallet.getPinCode() == null) {
            throw new AppException(ErrorCode.BAD_REQUEST); 
        }

        if (!passwordEncoder.matches(request.getPinCode(), buyerWallet.getPinCode())) {
            throw new AppException(ErrorCode.BAD_REQUEST); 
        }

        AuctionRecord record = order.getAuctionRecord();
        Auction auction = record.getAuction();

        AuctionRegistration registration = auctionRegistrationRepository
                .findByAuctionIdAndUserId(auction.getId(), buyer.getId())
                .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST));

        BigDecimal depositAmount = registration.getDepositAmount();
        BigDecimal totalAmount = order.getTotalAmount();
        BigDecimal amountToPay = totalAmount.subtract(depositAmount);

        if (amountToPay.compareTo(BigDecimal.ZERO) > 0) {
            if (buyerWallet.getAvailableBalance().compareTo(amountToPay) < 0) {
                throw new AppException(ErrorCode.BAD_REQUEST); 
            }
            buyerWallet.setAvailableBalance(buyerWallet.getAvailableBalance().subtract(amountToPay));
        }

        // Deduct deposit from frozen balance
        buyerWallet.setFrozenBalance(buyerWallet.getFrozenBalance().subtract(depositAmount));
        walletRepository.save(buyerWallet);

        // Add to seller's frozen balance
        User seller = order.getSeller();
        Wallet sellerWallet = walletRepository.findByUser(seller)
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));

        sellerWallet.setFrozenBalance(sellerWallet.getFrozenBalance().add(totalAmount));
        walletRepository.save(sellerWallet);

        // Save transactions
        Transaction buyerTransaction = Transaction.builder()
                .wallet(buyerWallet)
                .type(TransactionType.AUCTION_PAYMENT)
                .amount(totalAmount)
                .status(TransactionStatus.SUCCESS)
                .note("Paid for Order: " + orderId + " (Escrow)")
                .build();
        transactionRepository.save(buyerTransaction);

        Transaction sellerTransaction = Transaction.builder()
                .wallet(sellerWallet)
                .type(TransactionType.ESCROW_HOLD)
                .amount(totalAmount)
                .status(TransactionStatus.SUCCESS)
                .note("Escrow hold for Order: " + orderId)
                .build();
        transactionRepository.save(sellerTransaction);

        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);

        record.setStatus(AuctionRecordStatus.WIN);
        auctionRecordRepository.save(record);

        log.info("Order {} paid using escrow. Buyer deducted: {}, Seller frozen: {}", orderId, totalAmount, totalAmount);
        
        return orderMapper.toOrderResponse(order);
    }

    @Transactional
    public OrderResponse confirmDeliveryAndReleaseEscrow(UUID orderId) {
        User buyer = getCurrentUser();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST)); 

        if (!order.getBuyer().getId().equals(buyer.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (order.getStatus() != OrderStatus.PAID && order.getStatus() != OrderStatus.SHIPPING) {
            throw new AppException(ErrorCode.BAD_REQUEST); 
        }

        BigDecimal totalAmount = order.getTotalAmount();
        User seller = order.getSeller();
        Wallet sellerWallet = walletRepository.findByUser(seller)
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));

        if (sellerWallet.getFrozenBalance().compareTo(totalAmount) < 0) {
            log.error("Seller frozen balance is less than order amount for Order ID: {}", orderId);
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        // Move from frozen to available
        sellerWallet.setFrozenBalance(sellerWallet.getFrozenBalance().subtract(totalAmount));
        sellerWallet.setAvailableBalance(sellerWallet.getAvailableBalance().add(totalAmount));
        walletRepository.save(sellerWallet);

        Transaction releaseTransaction = Transaction.builder()
                .wallet(sellerWallet)
                .type(TransactionType.ESCROW_RELEASE)
                .amount(totalAmount)
                .status(TransactionStatus.SUCCESS)
                .note("Escrow released for Order: " + orderId)
                .build();
        transactionRepository.save(releaseTransaction);

        order.setStatus(OrderStatus.COMPLETED);
        orderRepository.save(order);

        log.info("Order {} completed. Escrow released to seller.", orderId);
        
        return orderMapper.toOrderResponse(order);
    }

    @Transactional
    public OrderResponse updateShippingInfo(UUID orderId, ShippingUpdateRequest request) {
        User seller = getCurrentUser();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST));

        if (!order.getSeller().getId().equals(seller.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (order.getStatus() != OrderStatus.PAID) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        order.setTrackingCode(request.getTrackingCode());
        order.setShippingProvider(request.getShippingProvider());
        order.setStatus(OrderStatus.SHIPPING);
        
        orderRepository.save(order);
        log.info("Order {} shipping info updated by seller.", orderId);
        
        return orderMapper.toOrderResponse(order);
    }
}
