package com.ecommerce.auctionplatform.payment.infrastructure.persistence.mapper;

import com.ecommerce.auctionplatform.payment.domain.model.Order;
import com.ecommerce.auctionplatform.payment.domain.model.Transaction;
import com.ecommerce.auctionplatform.payment.domain.model.Wallet;
import com.ecommerce.auctionplatform.payment.infrastructure.persistence.entity.OrderEntity;
import com.ecommerce.auctionplatform.payment.infrastructure.persistence.entity.TransactionEntity;
import com.ecommerce.auctionplatform.payment.infrastructure.persistence.entity.WalletEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PaymentPersistenceMapper {
    Wallet toDomain(WalletEntity entity);
    WalletEntity toEntity(Wallet domain);

    Order toDomain(OrderEntity entity);
    OrderEntity toEntity(Order domain);

    Transaction toDomain(TransactionEntity entity);
    TransactionEntity toEntity(Transaction domain);
}
