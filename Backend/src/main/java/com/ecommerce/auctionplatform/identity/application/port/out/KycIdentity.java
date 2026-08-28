package com.ecommerce.auctionplatform.identity.application.port.out;

import java.time.LocalDate;

public record KycIdentity(String idCard, LocalDate dateOfBirth, Boolean gender) {
}
