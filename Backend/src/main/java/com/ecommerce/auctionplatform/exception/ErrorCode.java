package com.ecommerce.auctionplatform.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    USER_NOT_FOUND(404,"User not found", HttpStatus.NOT_FOUND),
    INVALID_CREDENTIALS(401,"Invalid credentials", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED(403,"Access denied", HttpStatus.FORBIDDEN),
    INTERNAL_SERVER_ERROR(500,"Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    AUCTION_NOT_FOUND(404,"Auction not found", HttpStatus.NOT_FOUND),
    BID_TOO_LOW(400,"Bid amount is too low", HttpStatus.BAD_REQUEST),
    AUCTION_ENDED(400,"Auction has already ended", HttpStatus.BAD_REQUEST),
    CANNOT_BID_OWN_AUCTION(1014, "You cannot bid on your own auction", HttpStatus.BAD_REQUEST),
    INVALID_TOKEN(401,"Invalid token", HttpStatus.UNAUTHORIZED),
    UNVERIFIED_USER(403,"User is not verified", HttpStatus.FORBIDDEN),
    UNAUTHORIZED(401,"Unauthorized", HttpStatus.UNAUTHORIZED),
    FORBIDDEN(403,"Forbidden", HttpStatus.FORBIDDEN),
    BAD_REQUEST(400,"Bad request", HttpStatus.BAD_REQUEST),
    INVALID_EKYC_IMAGE(400, "Ảnh không hợp lệ hoặc bị mờ, vui lòng chụp lại CCCD rõ nét", HttpStatus.BAD_REQUEST),
    EKYC_ID_NOT_FOUND(400, "Không tìm thấy số CCCD trong ảnh", HttpStatus.BAD_REQUEST),
    UNCATEGORIZED_EXCEPTION(1000,"Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),

    TOKEN_BLACKLISTED(401,"Token is blacklisted", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED(401,"Token has expired", HttpStatus.UNAUTHORIZED),
    TOKEN_INVALID(401,"Token is invalid", HttpStatus.UNAUTHORIZED),
    TOKEN_NOT_FOUND(404,"Token not found", HttpStatus.NOT_FOUND),
    ACCOUNT_LOCKED(403,"Account is locked", HttpStatus.FORBIDDEN),
    UNAUTHENTACATED(401,"Unauthenticated", HttpStatus.UNAUTHORIZED),
    ACCOUNT_INACTIVE(403,"Account is inactive", HttpStatus.FORBIDDEN),
    ROLE_NOT_FOUND(404,"Role not found", HttpStatus.NOT_FOUND),
    USERNAME_EXISTED(400, "Username already exists", HttpStatus.BAD_REQUEST),
    EMAIL_EXISTED(400, "Email already exists", HttpStatus.BAD_REQUEST),
    PHONE_EXISTED(400, "Phone number already exists", HttpStatus.BAD_REQUEST),
    LOW_REPUTATION(403, "User reputation is too low to perform this action", HttpStatus.FORBIDDEN),
    ROLE_NOT_EXISTS(404,"Role not exists", HttpStatus.NOT_FOUND),
    USER_OR_PASSWORD_INCORRECT(401, "Username or password is incorrect", HttpStatus.UNAUTHORIZED),

    NOT_AUCTON_OWNER(403, "User is not the owner of the auction", HttpStatus.FORBIDDEN),
    USER_UNDERAGE(403, "You must be at least 18 years old to perform this action", HttpStatus.FORBIDDEN),
    WALLET_NOT_FOUND(404, "Wallet not found", HttpStatus.NOT_FOUND),
    INVALID_PIN(400, "Invalid PIN", HttpStatus.BAD_REQUEST),
    WALLET_FROZEN(403, "Wallet is frozen", HttpStatus.FORBIDDEN),
    INSUFFICIENT_BALANCE(400, "Insufficient balance", HttpStatus.BAD_REQUEST),
    TRANSACTION_NOT_FOUND(404, "Transaction not found", HttpStatus.NOT_FOUND),
    ORDER_ALREADY_REVIEWED(400, "Order has already been reviewed", HttpStatus.BAD_REQUEST),
    ORDER_NOT_ELIGIBLE_FOR_REVIEW(400, "Order is not eligible for review", HttpStatus.BAD_REQUEST),
    ORDER_NOT_FOUND(404, "Order not found", HttpStatus.NOT_FOUND),
    WALLET_PIN_NOT_SET(400, "Wallet PIN is not set", HttpStatus.BAD_REQUEST),
    WALLET_PIN_WRONG(400, "Wallet PIN is incorrect", HttpStatus.BAD_REQUEST),

    DISPUTE_NOT_FOUND(404, "Dispute not found", HttpStatus.NOT_FOUND),
    DISPUTE_ALREADY_EXISTS(400, "An active dispute already exists for this order", HttpStatus.BAD_REQUEST),
    DISPUTE_ALREADY_RESOLVED(400, "Dispute has already been resolved", HttpStatus.BAD_REQUEST),
    ORDER_NOT_ELIGIBLE_FOR_DISPUTE(400, "Order is not eligible for dispute", HttpStatus.BAD_REQUEST),
    INVALID_DISPUTE_OUTCOME(400, "Invalid dispute outcome", HttpStatus.BAD_REQUEST),
    DISPUTE_EXPIRED(400, "Dispute period has expired", HttpStatus.BAD_REQUEST),
    PAYMENT_METHOD_NOT_SUPPORTED(400, "Payment method not supported", HttpStatus.BAD_REQUEST),

    CATEGORY_NOT_FOUND(404,"Category not found", HttpStatus.NOT_FOUND),
    NOTIFICATION_NOT_FOUND(404, "Notification not found", HttpStatus.NOT_FOUND);


    private final String message;
    private final int status;
    private final HttpStatusCode statusCode;

    ErrorCode(int status,String message, HttpStatusCode statusCode) {
        this.status = status;
        this.message = message;
        this.statusCode = statusCode;
    }

}
