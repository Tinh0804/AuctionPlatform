package com.ecommerce.auctionplatform.identity.application.port.in;

import com.ecommerce.auctionplatform.shared.application.model.FileContent;

public interface EKycUseCase {
    void verifyKyc(FileContent frontImage, FileContent backImage);
}
