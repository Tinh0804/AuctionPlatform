package com.ecommerce.auctionplatform.shared.application.port.out;

import java.util.Optional;

public interface PhoneVerificationPort {
    Optional<String> verifiedPhoneNumber(String identityToken);
}
