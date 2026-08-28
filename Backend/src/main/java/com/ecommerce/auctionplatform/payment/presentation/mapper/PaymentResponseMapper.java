package com.ecommerce.auctionplatform.payment.presentation.mapper;

import com.ecommerce.auctionplatform.payment.presentation.dto.response.OrderPaymentResponse;
import com.ecommerce.auctionplatform.payment.presentation.dto.response.OrderResponse;
import com.ecommerce.auctionplatform.payment.presentation.dto.response.PaymentCallbackResponse;
import com.ecommerce.auctionplatform.payment.presentation.dto.response.PaymentResponse;
import com.ecommerce.auctionplatform.payment.presentation.dto.response.TransactionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PaymentResponseMapper {
    OrderResponse toOrderResponse(
            com.ecommerce.auctionplatform.payment.application.dto.response.OrderResponse source);

    List<OrderResponse> toOrderResponses(
            List<com.ecommerce.auctionplatform.payment.application.dto.response.OrderResponse> source);

    OrderPaymentResponse toOrderPaymentResponse(
            com.ecommerce.auctionplatform.payment.application.dto.response.OrderPaymentResponse source);

    PaymentResponse toPaymentResponse(
            com.ecommerce.auctionplatform.payment.application.dto.response.PaymentResponse source);

    PaymentCallbackResponse toCallbackResponse(
            com.ecommerce.auctionplatform.payment.application.dto.response.PaymentCallbackResponse source);

    TransactionResponse toTransactionResponse(
            com.ecommerce.auctionplatform.payment.application.dto.response.TransactionResponse source);

    List<TransactionResponse> toTransactionResponses(
            List<com.ecommerce.auctionplatform.payment.application.dto.response.TransactionResponse> source);
}
