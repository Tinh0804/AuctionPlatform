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
    INVALID_TOKEN(401,"Invalid token", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(401,"Unauthorized", HttpStatus.UNAUTHORIZED),
    FORBIDDEN(403,"Forbidden", HttpStatus.FORBIDDEN),
    BAD_REQUEST(400,"Bad request", HttpStatus.BAD_REQUEST),
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


    CATEGORY_NOT_FOUND(404,"Category not found", HttpStatus.NOT_FOUND);


    private final String message;
    private final int status;
    private final HttpStatusCode statusCode;

    ErrorCode(int status,String message, HttpStatusCode statusCode) {
        this.status = status;
        this.message = message;
        this.statusCode = statusCode;
    }

}
