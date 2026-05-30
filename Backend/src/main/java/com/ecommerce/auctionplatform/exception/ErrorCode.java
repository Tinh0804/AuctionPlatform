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
    UNCATEGORIZED_EXCEPTION(1000,"Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR);


    private final String message;
    private final int status;
    private final HttpStatusCode statusCode;

    ErrorCode(int status,String message, HttpStatusCode statusCode) {
        this.status = status;
        this.message = message;
        this.statusCode = statusCode;
    }

}
