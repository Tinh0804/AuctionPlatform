package com.ecommerce.auctionplatform.identity.application.port.in;

/**
 * Public application contract for statistics consumers outside the user context.
 */
public interface UserStatisticsUseCase {
    long countUsers();
}
