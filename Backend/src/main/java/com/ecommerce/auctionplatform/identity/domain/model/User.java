package com.ecommerce.auctionplatform.identity.domain.model;

import com.ecommerce.auctionplatform.identity.domain.enums.VerificationStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
    UUID id;
    Account account;
    String name;
    String phone;
    String email;
    String identityCard;
    Boolean gender;
    LocalDate dob;

    @Builder.Default
    Integer reputationScore = 100;

    @Builder.Default
    VerificationStatus verificationStatus = VerificationStatus.UNVERIFIED;

    String identityFrontImage;
    String identityBackImage;
    String avatarImage;

    public void verifyKyc(LocalDate dob, String identityCard, String identityFrontImage, String identityBackImage, Boolean gender) {
        this.dob = dob;
        this.identityCard = identityCard;
        this.identityFrontImage = identityFrontImage;
        this.identityBackImage = identityBackImage;
        this.gender = gender;
        this.verificationStatus = VerificationStatus.VERIFIED;
    }

    public void updateProfile(String name, LocalDate dob, Boolean gender, String email, String avatarImage) {
        if (name != null) this.name = name;
        if (dob != null) this.dob = dob;
        if (gender != null) this.gender = gender;
        if (email != null) this.email = email;
        if (avatarImage != null) this.avatarImage = avatarImage;
    }

    public void updateAvatar(String avatarImage) {
        this.avatarImage = avatarImage;
    }

    public void updatePhone(String phone) {
        this.phone = phone;
    }

    public void decreaseReputationScore(Integer penalty) {
        this.reputationScore -= penalty;
        if (this.reputationScore < 0) {
            this.reputationScore = 0;
        }
    }

    public void increaseReputationScore(Integer reward) {
        this.reputationScore += reward;
    }
}
