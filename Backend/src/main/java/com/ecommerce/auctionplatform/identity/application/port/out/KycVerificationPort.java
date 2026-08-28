package com.ecommerce.auctionplatform.identity.application.port.out;

import com.ecommerce.auctionplatform.shared.application.model.FileContent;

public interface KycVerificationPort {
    KycIdentity verify(FileContent frontImage);
}
