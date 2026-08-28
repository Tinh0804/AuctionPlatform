package com.ecommerce.auctionplatform.identity.application.port.in;

public interface UserBootstrapUseCase {
    void ensureAdmin(AdminBootstrapCommand command);

    record AdminBootstrapCommand(String username, String password, String phone, String email) {
    }
}
