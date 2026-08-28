package com.ecommerce.auctionplatform.shared.application.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    USER_NOT_FOUND(404, "User not found"),
    INVALID_CREDENTIALS(401, "Invalid credentials"),
    ACCESS_DENIED(403, "Access denied"),
    INTERNAL_SERVER_ERROR(500, "Internal server error"),
    AUCTION_NOT_FOUND(404, "Auction not found"),
    BID_TOO_LOW(400, "Bid amount is too low"),
    AUCTION_ENDED(400, "Auction has already ended"),
    CANNOT_BID_OWN_AUCTION(1014, "You cannot bid on your own auction"),
    ALREADY_LEADING(1025, "Bạn đang dẫn đầu phiên đấu giá này"),
    INVALID_TOKEN(401, "Invalid token"),
    UNVERIFIED_USER(403, "User is not verified"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    BAD_REQUEST(400, "Bad request"),
    INVALID_EKYC_IMAGE(400, "Ảnh không hợp lệ hoặc bị mờ, vui lòng chụp lại CCCD rõ nét"),
    EKYC_ID_NOT_FOUND(400, "Không tìm thấy số CCCD trong ảnh"),
    UNCATEGORIZED_EXCEPTION(1000, "Uncategorized error"),
    TOKEN_BLACKLISTED(401, "Token is blacklisted"),
    TOKEN_EXPIRED(401, "Token has expired"),
    TOKEN_INVALID(401, "Token is invalid"),
    TOKEN_NOT_FOUND(404, "Token not found"),
    ACCOUNT_LOCKED(403, "Account is locked"),
    UNAUTHENTACATED(401, "Unauthenticated"),
    ACCOUNT_INACTIVE(403, "Account is inactive"),
    ROLE_NOT_FOUND(404, "Role not found"),
    USERNAME_EXISTED(400, "Username already exists"),
    EMAIL_EXISTED(400, "Email already exists"),
    PHONE_EXISTED(400, "Phone number already exists"),
    LOW_REPUTATION(403, "User reputation is too low to perform this action"),
    ROLE_NOT_EXISTS(404, "Role not exists"),
    USER_OR_PASSWORD_INCORRECT(401, "Username or password is incorrect"),
    NOT_AUCTON_OWNER(403, "User is not the owner of the auction"),
    USER_UNDERAGE(403, "You must be at least 18 years old to perform this action"),
    WALLET_NOT_FOUND(404, "Wallet not found"),
    INVALID_PIN(400, "Invalid PIN"),
    WALLET_FROZEN(403, "Wallet is frozen"),
    INSUFFICIENT_BALANCE(400, "Insufficient balance"),
    TRANSACTION_NOT_FOUND(404, "Transaction not found"),
    ORDER_ALREADY_REVIEWED(400, "Order has already been reviewed"),
    ORDER_NOT_ELIGIBLE_FOR_REVIEW(400, "Order is not eligible for review"),
    ORDER_NOT_FOUND(404, "Order not found"),
    WALLET_PIN_NOT_SET(400, "Wallet PIN is not set"),
    WALLET_PIN_WRONG(400, "Wallet PIN is incorrect"),
    DISPUTE_NOT_FOUND(404, "Dispute not found"),
    DISPUTE_ALREADY_EXISTS(400, "An active dispute already exists for this order"),
    DISPUTE_ALREADY_RESOLVED(400, "Dispute has already been resolved"),
    ORDER_NOT_ELIGIBLE_FOR_DISPUTE(400, "Order is not eligible for dispute"),
    INVALID_DISPUTE_OUTCOME(400, "Invalid dispute outcome"),
    DISPUTE_EXPIRED(400, "Dispute period has expired"),
    PAYMENT_METHOD_NOT_SUPPORTED(400, "Payment method not supported"),
    CATEGORY_NOT_FOUND(404, "Category not found"),
    ADDRESS_NOT_FOUND(404, "Address not found"),
    NOTIFICATION_NOT_FOUND(404, "Notification not found"),
    INVALID_STATUS(400, "Invalid status");

    private final String message;
    private final int status;

    ErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public static ErrorCode from(com.ecommerce.auctionplatform.shared.domain.enums.DomainErrorCode domainErrorCode) {
        if (domainErrorCode == null) {
            return INTERNAL_SERVER_ERROR;
        }
        try {
            return ErrorCode.valueOf(domainErrorCode.name());
        } catch (IllegalArgumentException e) {
            return INTERNAL_SERVER_ERROR;
        }
    }
}
