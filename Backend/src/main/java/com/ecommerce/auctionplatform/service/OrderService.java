package com.ecommerce.auctionplatform.service;

import com.ecommerce.auctionplatform.dto.request.EscrowPaymentRequest;
import com.ecommerce.auctionplatform.dto.request.OrderPaymentRequest;
import com.ecommerce.auctionplatform.dto.request.PaymentRequest;
import com.ecommerce.auctionplatform.dto.request.ReviewRequest;
import com.ecommerce.auctionplatform.dto.request.ShippingUpdateRequest;
import com.ecommerce.auctionplatform.dto.respose.OrderPaymentResponse;
import com.ecommerce.auctionplatform.dto.respose.OrderResponse;
import com.ecommerce.auctionplatform.dto.respose.PaymentResponse;
import com.ecommerce.auctionplatform.entity.enums.PaymentMethod;
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
import java.time.LocalDateTime;
import java.util.List;
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
    MoMoService moMoService;
    VNPayService vnPayService;
    ReputationHistoryRepository reputationHistoryRepository;
    NotificationRepository notificationRepository;
    RoleRepository roleRepository;

    private User getCurrentUser() {
        UUID userProfileId = UUID.fromString(SecurityUtils.getCurrentProfileId().orElseThrow(()->
                new AppException(ErrorCode.UNAUTHORIZED)));
        return userRepository.findById(userProfileId).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    public List<OrderResponse> getMyPurchases() {
        User buyer = getCurrentUser();
        return orderRepository.findByBuyerIdOrderByCreatedAtDesc(buyer.getId())
                .stream()
                .map(orderMapper::toOrderResponse)
                .toList();
    }

    public List<OrderResponse> getMySales() {
        User seller = getCurrentUser();
        return orderRepository.findBySellerIdOrderByCreatedAtDesc(seller.getId())
                .stream()
                .map(orderMapper::toOrderResponse)
                .toList();
    }

    public OrderResponse getOrderDetail(UUID orderId) {
        User user = getCurrentUser();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST));
        if (!order.getBuyer().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return orderMapper.toOrderResponse(order);
    }

    @Transactional
    public OrderPaymentResponse initiateOrderPayment(UUID orderId, OrderPaymentRequest request) {
        User buyer = getCurrentUser();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST));

        if (!order.getBuyer().getId().equals(buyer.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
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
        if (amountToPay.compareTo(BigDecimal.ZERO) < 0) amountToPay = BigDecimal.ZERO;

        String method = request.getPaymentMethod().toUpperCase();

        if ("WALLET".equals(method)) {
            // --- Wallet PIN flow ---
            Wallet buyerWallet = walletRepository.findByUser(buyer)
                    .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));
            if (buyerWallet.getPinCode() == null) {
                throw new AppException(ErrorCode.BAD_REQUEST);
            }
            if (!passwordEncoder.matches(request.getPinCode(), buyerWallet.getPinCode())) {
                throw new AppException(ErrorCode.INVALID_PIN);
            }
            if (amountToPay.compareTo(BigDecimal.ZERO) > 0) {
                if (buyerWallet.getAvailableBalance().compareTo(amountToPay) < 0) {
                    throw new AppException(ErrorCode.INSUFFICIENT_BALANCE);
                }
                buyerWallet.setAvailableBalance(buyerWallet.getAvailableBalance().subtract(amountToPay));
            }
            buyerWallet.setFrozenBalance(buyerWallet.getFrozenBalance().subtract(depositAmount));
            walletRepository.save(buyerWallet);

            User seller = order.getSeller();
            Wallet sellerWallet = walletRepository.findByUser(seller)
                    .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));
            sellerWallet.setFrozenBalance(sellerWallet.getFrozenBalance().add(totalAmount));
            walletRepository.save(sellerWallet);

            transactionRepository.save(Transaction.builder()
                    .wallet(buyerWallet).type(TransactionType.AUCTION_PAYMENT)
                    .amount(totalAmount).status(TransactionStatus.SUCCESS)
                    .referenceType("ORDER").referenceId(order.getId())
                    .note("Paid for Order: " + orderId + " via Wallet").build());
            transactionRepository.save(Transaction.builder()
                    .wallet(sellerWallet).type(TransactionType.ESCROW_HOLD)
                    .amount(totalAmount).status(TransactionStatus.SUCCESS)
                    .referenceType("ORDER").referenceId(order.getId())
                    .note("Escrow hold for Order: " + orderId).build());

            order.setStatus(OrderStatus.PAID);
            orderRepository.save(order);
            record.setStatus(AuctionRecordStatus.WIN);
            auctionRecordRepository.save(record);

            return OrderPaymentResponse.builder()
                    .status("PAID")
                    .order(orderMapper.toOrderResponse(order))
                    .build();

        } else if ("MOMO".equals(method) || "VNPAY".equals(method)) {
            // --- Gateway flow: pre-create Transaction, return payment URL ---
            Wallet buyerWallet = walletRepository.findByUser(buyer)
                    .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));

            // Pre-save a PENDING transaction so callback can find it
            Transaction pendingTx = Transaction.builder()
                    .wallet(buyerWallet)
                    .type(TransactionType.ORDER_PAYMENT)
                    .amount(amountToPay)
                    .status(TransactionStatus.PENDING)
                    .referenceType("ORDER")
                    .referenceId(order.getId())
                    .note("Gateway payment for Order: " + orderId)
                    .build();
            pendingTx = transactionRepository.save(pendingTx);

            String returnUrl = request.getReturnUrl() != null ? request.getReturnUrl() : "";
            PaymentRequest payReq = PaymentRequest.builder()
                    .referenceId(pendingTx.getId().toString())
                    .amount(amountToPay.doubleValue())
                    .orderInfo("Thanh toan don hang " + orderId)
                    .method(PaymentMethod.valueOf(method))
                    .returnUrl(returnUrl)
                    .build();

            PaymentResponse payResponse;
            if ("MOMO".equals(method)) {
                payResponse = moMoService.createPayment(payReq);
            } else {
                payResponse = vnPayService.createPayment(payReq);
            }

            return OrderPaymentResponse.builder()
                    .status("PENDING_GATEWAY")
                    .paymentUrl(payResponse.getPaymentUrl())
                    .build();
        } else {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }
    }

    /**
     * Called by MoMo/VNPay callback after successful gateway payment for an Order.
     */
    @Transactional
    public void handleGatewayPaymentSuccess(UUID orderId, BigDecimal paidAmount) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        if (order.getStatus() == OrderStatus.PAID) {
            log.warn("Order {} already PAID, skipping duplicate gateway callback", orderId);
            return;
        }

        AuctionRecord record = order.getAuctionRecord();
        Auction auction = record.getAuction();
        User buyer = order.getBuyer();

        AuctionRegistration registration = auctionRegistrationRepository
                .findByAuctionIdAndUserId(auction.getId(), buyer.getId())
                .orElse(null);

        BigDecimal depositAmount = registration != null ? registration.getDepositAmount() : BigDecimal.ZERO;
        BigDecimal totalAmount = order.getTotalAmount();

        // Release deposit from buyer frozen balance
        Wallet buyerWallet = walletRepository.findByUser(buyer).orElse(null);
        if (buyerWallet != null && depositAmount.compareTo(BigDecimal.ZERO) > 0) {
            buyerWallet.setFrozenBalance(buyerWallet.getFrozenBalance().subtract(depositAmount));
            walletRepository.save(buyerWallet);
        }

        // Hold total in seller's escrow (frozen)
        User seller = order.getSeller();
        Wallet sellerWallet = walletRepository.findByUser(seller).orElse(null);
        if (sellerWallet != null) {
            sellerWallet.setFrozenBalance(sellerWallet.getFrozenBalance().add(totalAmount));
            walletRepository.save(sellerWallet);
            transactionRepository.save(Transaction.builder()
                    .wallet(sellerWallet).type(TransactionType.ESCROW_HOLD)
                    .amount(totalAmount).status(TransactionStatus.SUCCESS)
                    .referenceType("ORDER").referenceId(orderId)
                    .note("Escrow hold (gateway) for Order: " + orderId).build());
        }

        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);
        record.setStatus(AuctionRecordStatus.WIN);
        auctionRecordRepository.save(record);
        log.info("Order {} marked as PAID via gateway payment", orderId);
    }

    @Transactional
    public OrderResponse payOrderWithEscrow(UUID orderId, EscrowPaymentRequest request) {
        User buyer = getCurrentUser();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getBuyer().getId().equals(buyer.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new AppException(ErrorCode.BAD_REQUEST); 
        }

        Wallet buyerWallet = walletRepository.findByUser(buyer)
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));

        if (buyerWallet.getPinCode() == null) {
            throw new AppException(ErrorCode.WALLET_PIN_NOT_SET);
        }

        if (!passwordEncoder.matches(request.getPinCode(), buyerWallet.getPinCode())) {
            throw new AppException(ErrorCode.WALLET_PIN_WRONG);
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

    /**
     * Buyer đánh giá + xác nhận hoàn thành đơn hàng.
     * Flow: Lưu rating → Tính uy tín seller → Giải ngân Escrow (trừ phí nền tảng vào ví Admin) → Gửi thông báo.
     */
    @Transactional
    public OrderResponse completeOrderWithReview(UUID orderId, ReviewRequest request) {
        User buyer = getCurrentUser();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST));

        // 1. Validate
        if (!order.getBuyer().getId().equals(buyer.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        if (order.getStatus() != OrderStatus.PAID && order.getStatus() != OrderStatus.SHIPPING) {
            throw new AppException(ErrorCode.ORDER_NOT_ELIGIBLE_FOR_REVIEW);
        }
        if (order.getRatingScore() != null) {
            throw new AppException(ErrorCode.ORDER_ALREADY_REVIEWED);
        }

        // 2. Lưu đánh giá vào Order
        order.setRatingScore(request.getRating());
        order.setReviewContent(request.getComment());
        order.setReviewDate(LocalDateTime.now());

        // 3. Tính uy tín cho seller
        User seller = order.getSeller();
        int scoreChange = calculateReputationChange(request.getRating());
        int newScore = (seller.getReputationScore() != null ? seller.getReputationScore() : 100) + scoreChange;
        if (newScore < 0) newScore = 0;
        seller.setReputationScore(newScore);
        userRepository.save(seller);

        // Ghi log uy tín
        reputationHistoryRepository.save(ReputationHistory.builder()
                .user(seller)
                .scoreChange(scoreChange)
                .reason("Đánh giá " + request.getRating() + " sao từ đơn hàng #" + orderId.toString().substring(0, 8))
                .order(order)
                .build());

        // 4. Giải ngân Escrow – trừ phí nền tảng
        BigDecimal totalAmount = order.getTotalAmount();
        BigDecimal platformFee = BigDecimal.ZERO;

        // Lấy phí nền tảng từ phiên đấu giá (nếu có)
        if (order.getAuctionRecord() != null && order.getAuctionRecord().getAuction() != null) {
            Auction auction = order.getAuctionRecord().getAuction();
            if (auction.getPlatformFee() != null && auction.getPlatformFee().compareTo(BigDecimal.ZERO) > 0) {
                platformFee = auction.getPlatformFee();
            }
        }

        BigDecimal netAmount = totalAmount.subtract(platformFee);
        if (netAmount.compareTo(BigDecimal.ZERO) < 0) netAmount = BigDecimal.ZERO;

        Wallet sellerWallet = walletRepository.findByUser(seller)
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));

        if (sellerWallet.getFrozenBalance().compareTo(totalAmount) < 0) {
            log.error("Seller frozen balance ({}) is less than order amount ({}) for Order ID: {}",
                    sellerWallet.getFrozenBalance(), totalAmount, orderId);
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        // Giải ngân: frozen → available (trừ phí)
        sellerWallet.setFrozenBalance(sellerWallet.getFrozenBalance().subtract(totalAmount));
        sellerWallet.setAvailableBalance(sellerWallet.getAvailableBalance().add(netAmount));
        walletRepository.save(sellerWallet);

        // Transaction: ESCROW_RELEASE cho seller
        transactionRepository.save(Transaction.builder()
                .wallet(sellerWallet)
                .type(TransactionType.ESCROW_RELEASE)
                .amount(netAmount)
                .status(TransactionStatus.SUCCESS)
                .referenceType("ORDER").referenceId(orderId)
                .note("Escrow released for Order: " + orderId + " (after platform fee)")
                .build());

        // Phí nền tảng → ví Admin
        if (platformFee.compareTo(BigDecimal.ZERO) > 0) {
            // Tìm ví admin: tìm user có role ADMIN đầu tiên
            Role adminRole = roleRepository.findByName("ADMIN").orElse(null);
            if (adminRole != null) {
                User adminUser = userRepository.findFirstByAccountRoleId(adminRole.getId()).orElse(null);
                if (adminUser != null) {
                    Wallet adminWallet = walletRepository.findByUser(adminUser).orElse(null);
                    if (adminWallet != null) {
                        adminWallet.setAvailableBalance(adminWallet.getAvailableBalance().add(platformFee));
                        walletRepository.save(adminWallet);

                        transactionRepository.save(Transaction.builder()
                                .wallet(adminWallet)
                                .type(TransactionType.PLATFORM_FEE)
                                .amount(platformFee)
                                .status(TransactionStatus.SUCCESS)
                                .referenceType("ORDER").referenceId(orderId)
                                .note("Platform fee from Order: " + orderId)
                                .build());
                        log.info("Platform fee {} transferred to admin wallet for Order {}", platformFee, orderId);
                    }
                }
            }

            // Ghi transaction PLATFORM_FEE trên ví seller (ghi nhận bị trừ)
            transactionRepository.save(Transaction.builder()
                    .wallet(sellerWallet)
                    .type(TransactionType.PLATFORM_FEE)
                    .amount(platformFee)
                    .status(TransactionStatus.SUCCESS)
                    .referenceType("ORDER").referenceId(orderId)
                    .note("Platform fee deducted for Order: " + orderId)
                    .build());
        }

        // 5. Cập nhật trạng thái
        order.setStatus(OrderStatus.COMPLETED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        // 6. Gửi thông báo cho seller
        String ratingStars = "⭐".repeat(request.getRating());
        notificationRepository.save(Notification.builder()
                .user(seller)
                .type("ORDER_COMPLETED")
                .title("Đơn hàng hoàn thành " + ratingStars)
                .content("Đơn hàng #" + orderId.toString().substring(0, 8) + " đã hoàn thành. "
                        + "Bạn nhận được đánh giá " + request.getRating() + " sao. "
                        + "Số tiền " + String.format("%,.0f", netAmount) + "đ đã được giải ngân vào ví.")
                .referenceType("ORDER")
                .referenceId(orderId)
                .build());

        log.info("Order {} completed with review. Rating: {}, Reputation change: {}, Net amount: {}, Platform fee: {}",
                orderId, request.getRating(), scoreChange, netAmount, platformFee);

        return orderMapper.toOrderResponse(order);
    }

    private int calculateReputationChange(int rating) {
        return switch (rating) {
            case 5 -> 5;
            case 4 -> 3;
            case 3 -> 0;
            case 2 -> -5;
            case 1 -> -10;
            default -> 0;
        };
    }
}
