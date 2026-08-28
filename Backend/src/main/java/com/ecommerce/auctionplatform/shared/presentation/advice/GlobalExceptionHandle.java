package com.ecommerce.auctionplatform.shared.presentation.advice;

import com.ecommerce.auctionplatform.shared.application.exception.AppException;
import com.ecommerce.auctionplatform.shared.application.exception.ErrorCode;
import com.ecommerce.auctionplatform.shared.application.exception.FileStorageException;
import com.ecommerce.auctionplatform.shared.domain.exception.DomainException;
import com.ecommerce.auctionplatform.shared.presentation.response.APIResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.http.HttpStatus;

@RestControllerAdvice
public class GlobalExceptionHandle {

    @ExceptionHandler({MissingServletRequestPartException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<APIResponse> handleMissingRequestPart(Exception exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(APIResponse.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .message("Required request parameter or file is missing")
                        .build());
    }

    @ExceptionHandler(value = FileStorageException.class)
    public ResponseEntity<APIResponse> handleFileStorageException(FileStorageException exception) {
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity
                .status(ErrorHttpStatusMapper.toHttpStatus(errorCode))
                .body(APIResponse.builder()
                        .status(errorCode.getStatus())
                        .message(errorCode.getMessage())
                        .build());
    }

    @ExceptionHandler(value = AppException.class)
    public ResponseEntity<APIResponse> handleAppException(AppException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        return ResponseEntity
                .status(ErrorHttpStatusMapper.toHttpStatus(errorCode))
                .body(APIResponse.builder()
                        .status(errorCode.getStatus())
                        .message(errorCode.getMessage())
                        .build());
    }

    @ExceptionHandler(value = DomainException.class)
    public ResponseEntity<APIResponse> handleDomainException(DomainException exception) {
        ErrorCode errorCode = exception.getErrorCode() != null
                ? ErrorCode.from(exception.getErrorCode())
                : ErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity
                .status(ErrorHttpStatusMapper.toHttpStatus(errorCode))
                .body(APIResponse.builder()
                        .status(errorCode.getStatus())
                        .message(errorCode.getMessage())
                        .build());
    }
}
