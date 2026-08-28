package com.ecommerce.auctionplatform.payment.application.service;

import com.ecommerce.auctionplatform.payment.application.dto.command.PayEscrowCommand;
import com.ecommerce.auctionplatform.payment.application.dto.command.InitiateOrderPaymentCommand;
import com.ecommerce.auctionplatform.payment.application.dto.command.CompleteOrderCommand;
import com.ecommerce.auctionplatform.payment.application.dto.command.UpdateShippingCommand;
import com.ecommerce.auctionplatform.payment.application.dto.response.OrderPaymentResponse;
import com.ecommerce.auctionplatform.payment.application.dto.response.OrderResponse;
import com.ecommerce.auctionplatform.payment.application.mapper.OrderMapper;
import com.ecommerce.auctionplatform.payment.application.port.in.OrderUseCase;
import com.ecommerce.auctionplatform.payment.application.port.out.AuctionQueryPort;
import com.ecommerce.auctionplatform.payment.application.port.out.AuctionRecordView;
import com.ecommerce.auctionplatform.payment.application.port.out.AuctionRegistrationView;
import com.ecommerce.auctionplatform.payment.application.port.out.PaymentGatewayPort;
import com.ecommerce.auctionplatform.payment.application.port.out.UserPort;
import com.ecommerce.auctionplatform.payment.application.port.out.UserView;
import com.ecommerce.auctionplatform.payment.domain.enums.OrderStatus;
import com.ecommerce.auctionplatform.payment.domain.enums.PaymentMethod;
import com.ecommerce.auctionplatform.payment.domain.enums.TransactionStatus;
import com.ecommerce.auctionplatform.payment.domain.enums.TransactionType;
import com.ecommerce.auctionplatform.payment.application.event.OrderCompletedEvent;
import com.ecommerce.auctionplatform.payment.domain.model.Order;
import com.ecommerce.auctionplatform.payment.domain.model.Transaction;
import com.ecommerce.auctionplatform.payment.domain.model.Wallet;
import com.ecommerce.auctionplatform.payment.domain.repository.OrderRepository;
import com.ecommerce.auctionplatform.payment.domain.repository.TransactionRepository;
import com.ecommerce.auctionplatform.payment.domain.repository.WalletRepository;
import com.ecommerce.auctionplatform.shared.application.event.DomainEventPublisher;
import com.ecommerce.auctionplatform.shared.application.exception.AppException;
import com.ecommerce.auctionplatform.shared.application.exception.ErrorCode;
import com.ecommerce.auctionplatform.shared.application.port.out.CurrentUserProvider;
import com.ecommerce.auctionplatform.shared.application.port.out.PasswordCodec;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderService implements OrderUseCase {
    OrderRepository orderRepository;
    WalletRepository walletRepository;
    TransactionRepository transactionRepository;
    AuctionQueryPort auctionQueryPort;
    UserPort userPort;
    PasswordCodec passwordCodec;
    CurrentUserProvider currentUserProvider;
    OrderMapper orderMapper;
    List<PaymentGatewayPort> paymentGateways;
    DomainEventPublisher domainEventPublisher;

    @Override
    public List<OrderResponse> getMyPurchases() {
        UUID buyerId = currentUser().id();
        return orderRepository.findByBuyerIdOrderByCreatedAtDesc(buyerId).stream()
                .map(orderMapper::toOrderResponse)
                .toList();
    }

    @Override
    public List<OrderResponse> getMySales() {
        UUID sellerId = currentUser().id();
        return orderRepository.findBySellerIdOrderByCreatedAtDesc(sellerId).stream()
                .map(orderMapper::toOrderResponse)
                .toList();
    }

    @Override
    public OrderResponse getOrderDetail(UUID orderId) {
        UserView user = currentUser();
        Order order = order(orderId);
        if (!order.getBuyerId().equals(user.id()) && !order.getSellerId().equals(user.id())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return orderMapper.toOrderResponse(order);
    }

    @Override
    @Transactional
    public OrderPaymentResponse initiateOrderPayment(UUID orderId, InitiateOrderPaymentCommand request) {
        UserView buyer = currentUser();
        Order order = payableOrder(orderId, buyer.id());
        AuctionRecordView record = record(order);
        BigDecimal depositAmount = depositAmount(record.auctionId(), buyer.id());
        BigDecimal amountToPay = positive(order.getTotalAmount().subtract(depositAmount));
        PaymentMethod method = paymentMethod(request.getPaymentMethod());

        if (method == PaymentMethod.WALLET) {
            payWithWallet(order, record, buyer.id(), request.getPinCode(), depositAmount);
            return OrderPaymentResponse.builder()
                    .status("PAID")
                    .order(orderMapper.toOrderResponse(order))
                    .build();
        }

        Wallet buyerWallet = walletRepository.findByUserId(buyer.id())
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));
        Transaction pendingTransaction = transactionRepository.save(Transaction.builder()
                .walletId(buyerWallet.getId())
                .type(TransactionType.ORDER_PAYMENT)
                .amount(amountToPay)
                .status(TransactionStatus.PENDING)
                .gatewayProvider(method.name())
                .referenceType("ORDER")
                .referenceId(order.getId())
                .note("Thanh toán đơn hàng " + order.getId())
                .build());

        PaymentGatewayPort.GatewayPaymentResult response = gateway(method).createPayment(
                new PaymentGatewayPort.GatewayPaymentRequest(
                        pendingTransaction.getId(),
                        amountToPay,
                        "Thanh toan don hang " + orderId,
                        request.getReturnUrl(),
                        null));

        return OrderPaymentResponse.builder()
                .status("PENDING_GATEWAY")
                .paymentUrl(response.paymentUrl())
                .build();
    }

    @Override
    @Transactional
    public void handleGatewayPaymentSuccess(UUID orderId, BigDecimal paidAmount) {
        Order order = order(orderId);
        if (order.getStatus() == OrderStatus.PAID) {
            log.warn("Order {} is already paid; ignoring duplicate callback", orderId);
            return;
        }
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        AuctionRecordView record = record(order);
        BigDecimal depositAmount = depositAmount(record.auctionId(), order.getBuyerId());
        BigDecimal expectedAmount = positive(order.getTotalAmount().subtract(depositAmount));
        if (paidAmount == null || paidAmount.compareTo(expectedAmount) < 0) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        walletRepository.findByUserIdForUpdate(order.getBuyerId()).ifPresent(wallet -> {
            if (depositAmount.signum() > 0) {
                wallet.deductFrozenBalance(depositAmount);
                walletRepository.save(wallet);
            }
        });

        Wallet sellerWallet = walletRepository.findByUserIdForUpdate(order.getSellerId())
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));
        sellerWallet.addFrozenBalance(order.getTotalAmount());
        walletRepository.save(sellerWallet);
        saveTransaction(sellerWallet.getId(), TransactionType.ESCROW_HOLD,
                order.getTotalAmount(), orderId, "Tạm giữ tiền đơn hàng " + orderId);

        order.markAsPaid();
        orderRepository.save(order);
        auctionQueryPort.markRecordWon(record.id());
    }

    @Override
    @Transactional
    public OrderResponse payOrderWithEscrow(UUID orderId, PayEscrowCommand request) {
        UserView buyer = currentUser();
        Order order = payableOrder(orderId, buyer.id());
        AuctionRecordView record = record(order);
        BigDecimal depositAmount = depositAmount(record.auctionId(), buyer.id());
        payWithWallet(order, record, buyer.id(), request.getPinCode(), depositAmount);
        return orderMapper.toOrderResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse confirmDeliveryAndReleaseEscrow(UUID orderId) {
        UserView buyer = currentUser();
        Order order = order(orderId);
        requireBuyer(order, buyer.id());
        if (order.getStatus() != OrderStatus.PAID && order.getStatus() != OrderStatus.SHIPPING) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        Wallet sellerWallet = walletRepository.findByUserIdForUpdate(order.getSellerId())
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));
        releaseEscrow(sellerWallet, order.getTotalAmount());
        saveTransaction(sellerWallet.getId(), TransactionType.ESCROW_RELEASE,
                order.getTotalAmount(), orderId, "Giải phóng tiền đơn hàng " + orderId);

        order.markAsCompleted();
        orderRepository.save(order);
        return orderMapper.toOrderResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse updateShippingInfo(UUID orderId, UpdateShippingCommand request) {
        UserView seller = currentUser();
        Order order = order(orderId);
        if (!order.getSellerId().equals(seller.id())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        if (order.getStatus() != OrderStatus.PAID) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        order.updateShippingInfo(request.getTrackingCode(), request.getShippingProvider());
        order.markAsShipping();
        orderRepository.save(order);
        return orderMapper.toOrderResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse completeOrderWithReview(UUID orderId, CompleteOrderCommand request) {
        UserView buyer = currentUser();
        Order order = order(orderId);
        requireBuyer(order, buyer.id());
        if (order.getStatus() != OrderStatus.PAID && order.getStatus() != OrderStatus.SHIPPING) {
            throw new AppException(ErrorCode.ORDER_NOT_ELIGIBLE_FOR_REVIEW);
        }
        if (order.getRatingScore() != null) {
            throw new AppException(ErrorCode.ORDER_ALREADY_REVIEWED);
        }

        order.addReview(request.getRating(), request.getComment());
        int scoreChange = reputationChange(request.getRating());
        userPort.changeReputation(
                order.getSellerId(),
                scoreChange,
                "Đánh giá " + request.getRating() + " sao từ đơn hàng #" + shortId(orderId),
                orderId
        );

        AuctionRecordView record = record(order);
        BigDecimal platformFee = record.platformFee() == null ? BigDecimal.ZERO : record.platformFee();
        BigDecimal netAmount = positive(order.getTotalAmount().subtract(platformFee));
        Wallet sellerWallet = walletRepository.findByUserIdForUpdate(order.getSellerId())
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));
        releaseEscrow(sellerWallet, order.getTotalAmount());
        if (platformFee.signum() > 0) {
            sellerWallet.deductBalance(platformFee);
            walletRepository.save(sellerWallet);
        }
        saveTransaction(sellerWallet.getId(), TransactionType.ESCROW_RELEASE,
                netAmount, orderId, "Giải phóng tiền đơn hàng " + orderId);

        if (platformFee.signum() > 0) {
            userPort.findAdminUser().flatMap(admin -> walletRepository.findByUserIdForUpdate(admin.id())).ifPresent(adminWallet -> {
                adminWallet.addBalance(platformFee);
                walletRepository.save(adminWallet);
                saveTransaction(adminWallet.getId(), TransactionType.PLATFORM_FEE,
                        platformFee, orderId, "Phí nền tảng đơn hàng " + orderId);
            });
        }

        order.markAsCompleted();
        orderRepository.save(order);
        domainEventPublisher.publish(new OrderCompletedEvent(
                orderId, order.getSellerId(), request.getRating(), netAmount));
        return orderMapper.toOrderResponse(order);
    }

    private void payWithWallet(
            Order order,
            AuctionRecordView record,
            UUID buyerId,
            String pin,
            BigDecimal depositAmount
    ) {
        Wallet buyerWallet = walletRepository.findByUserIdForUpdate(buyerId)
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));
        if (buyerWallet.getPinCode() == null) {
            throw new AppException(ErrorCode.WALLET_PIN_NOT_SET);
        }
        if (!passwordCodec.matches(pin, buyerWallet.getPinCode())) {
            throw new AppException(ErrorCode.WALLET_PIN_WRONG);
        }

        BigDecimal amountToPay = positive(order.getTotalAmount().subtract(depositAmount));
        if (amountToPay.signum() > 0) {
            buyerWallet.deductBalance(amountToPay);
        }
        if (depositAmount.signum() > 0) {
            buyerWallet.deductFrozenBalance(depositAmount);
        }
        walletRepository.save(buyerWallet);

        Wallet sellerWallet = walletRepository.findByUserIdForUpdate(order.getSellerId())
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));
        sellerWallet.addFrozenBalance(order.getTotalAmount());
        walletRepository.save(sellerWallet);

        saveTransaction(buyerWallet.getId(), TransactionType.AUCTION_PAYMENT,
                order.getTotalAmount(), order.getId(), "Thanh toán đơn hàng " + order.getId());
        saveTransaction(sellerWallet.getId(), TransactionType.ESCROW_HOLD,
                order.getTotalAmount(), order.getId(), "Tạm giữ tiền đơn hàng " + order.getId());

        order.markAsPaid();
        orderRepository.save(order);
        auctionQueryPort.markRecordWon(record.id());
    }

    private void releaseEscrow(Wallet sellerWallet, BigDecimal amount) {
        if (sellerWallet.getFrozenBalance().compareTo(amount) < 0) {
            throw new AppException(ErrorCode.INSUFFICIENT_BALANCE);
        }
        sellerWallet.unfreezeBalance(amount);
        walletRepository.save(sellerWallet);
    }

    private void saveTransaction(UUID walletId, TransactionType type, BigDecimal amount, UUID orderId, String note) {
        transactionRepository.save(Transaction.builder()
                .walletId(walletId)
                .type(type)
                .amount(amount)
                .status(TransactionStatus.SUCCESS)
                .referenceType("ORDER")
                .referenceId(orderId)
                .note(note)
                .build());
    }

    private Order payableOrder(UUID orderId, UUID buyerId) {
        Order order = order(orderId);
        requireBuyer(order, buyerId);
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }
        return order;
    }

    private void requireBuyer(Order order, UUID buyerId) {
        if (!order.getBuyerId().equals(buyerId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

    private Order order(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
    }

    private AuctionRecordView record(Order order) {
        if (order.getAuctionRecordId() == null) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }
        return auctionQueryPort.findRecord(order.getAuctionRecordId())
                .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST));
    }

    private BigDecimal depositAmount(UUID auctionId, UUID buyerId) {
        return auctionQueryPort.findRegistration(auctionId, buyerId)
                .map(AuctionRegistrationView::depositAmount)
                .orElse(BigDecimal.ZERO);
    }

    private PaymentGatewayPort gateway(PaymentMethod method) {
        return paymentGateways.stream()
                .filter(candidate -> candidate.gatewayType() == method)
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_METHOD_NOT_SUPPORTED));
    }

    private PaymentMethod paymentMethod(String value) {
        try {
            PaymentMethod method = PaymentMethod.valueOf(value.toUpperCase());
            if (method == PaymentMethod.WALLET || method == PaymentMethod.MOMO || method == PaymentMethod.VNPAY) {
                return method;
            }
        } catch (RuntimeException ignored) {
            // Converted to a stable application error below.
        }
        throw new AppException(ErrorCode.PAYMENT_METHOD_NOT_SUPPORTED);
    }

    private UserView currentUser() {
        UUID userId = currentUserProvider.currentProfileId()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
        return userPort.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private BigDecimal positive(BigDecimal amount) {
        return amount.signum() < 0 ? BigDecimal.ZERO : amount;
    }

    private int reputationChange(int rating) {
        return switch (rating) {
            case 5 -> 5;
            case 4 -> 3;
            case 3 -> 0;
            case 2 -> -5;
            case 1 -> -10;
            default -> throw new AppException(ErrorCode.BAD_REQUEST);
        };
    }

    private String shortId(UUID id) {
        return id.toString().substring(0, 8);
    }
}
