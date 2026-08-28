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

    public void updateByAdmin(
            String name,
            String phone,
            String email,
            String identityCard,
            Boolean gender,
            LocalDate dob,
            Integer reputationScore
    ) {
        if (name != null && !name.isBlank()) this.name = name;
        if (phone != null && !phone.isBlank()) this.phone = phone;
        if (email != null && !email.isBlank()) this.email = email;
        if (identityCard != null) {
            this.identityCard = identityCard.isBlank() ? null : identityCard;
        }
        if (gender != null) this.gender = gender;
        if (dob != null) this.dob = dob;
        if (reputationScore != null && reputationScore >= 0) this.reputationScore = reputationScore;
    }

    public void updateVerificationStatus(VerificationStatus status) {
        this.verificationStatus = status;
    }

    public void toggleAccountStatus() {
        if (this.account == null) {
            throw new IllegalStateException("User account is missing");
        }
        this.account.toggleActive();
    }

    public void anonymizeForDeletion() {
        if (this.account == null) {
            throw new IllegalStateException("User account is missing");
        }
        this.account.deactivate();
        String compactId = this.id == null
                ? UUID.randomUUID().toString().replace("-", "")
                : this.id.toString().replace("-", "");
        this.email = "deleted_" + compactId + "@deleted.local";
        this.phone = "deleted-" + compactId.substring(0, 12);
        this.identityCard = null;
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
