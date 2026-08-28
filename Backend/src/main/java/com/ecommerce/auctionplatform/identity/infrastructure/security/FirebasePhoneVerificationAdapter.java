package com.ecommerce.auctionplatform.identity.infrastructure.security;

import com.ecommerce.auctionplatform.shared.application.port.out.PhoneVerificationPort;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class FirebasePhoneVerificationAdapter implements PhoneVerificationPort {
    @Override
    public Optional<String> verifiedPhoneNumber(String identityToken) {
        try {
            FirebaseToken token = FirebaseAuth.getInstance().verifyIdToken(identityToken);
            return Optional.ofNullable((String) token.getClaims().get("phone_number"))
                    .filter(phone -> !phone.isBlank());
        } catch (FirebaseAuthException exception) {
            log.warn("Firebase phone verification failed: {}", exception.getMessage());
            return Optional.empty();
        }
    }
}
