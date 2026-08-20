package com.ecommerce.auctionplatform.exception;

import com.ecommerce.auctionplatform.dto.respose.APIResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandle {
    @ExceptionHandler(value = AppException.class)
    public ResponseEntity<APIResponse> handleAppException(AppException exception){
        ErrorCode errorCode = exception.getErrorCode();
        return ResponseEntity
                .status(errorCode.getStatusCode())
                .body(APIResponse.builder()
                        .status(errorCode.getStatus())
                        .message(errorCode.getMessage())
                        .build());

    }

}
