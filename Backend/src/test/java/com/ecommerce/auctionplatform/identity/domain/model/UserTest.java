package com.ecommerce.auctionplatform.identity.domain.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void adminUpdatePreservesIdentityCardWhenFieldIsOmitted() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .identityCard("012345678901")
                .build();

        user.updateByAdmin(null, null, null, null, null, null, null);

        assertThat(user.getIdentityCard()).isEqualTo("012345678901");
    }

    @Test
    void adminCanExplicitlyClearIdentityCard() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .identityCard("012345678901")
                .build();

        user.updateByAdmin(null, null, null, "  ", null, null, null);

        assertThat(user.getIdentityCard()).isNull();
    }

    @Test
    void deletionIsSoftAndAnonymizesUniqueContactFields() {
        UUID id = UUID.randomUUID();
        Account account = Account.builder().isActive(true).build();
        User user = User.builder()
                .id(id)
                .account(account)
                .email("person@example.com")
                .phone("0900000000")
                .identityCard("012345678901")
                .build();

        user.anonymizeForDeletion();

        assertThat(account.getIsActive()).isFalse();
        assertThat(user.getEmail()).startsWith("deleted_").endsWith("@deleted.local");
        assertThat(user.getPhone()).startsWith("deleted-").hasSize(20);
        assertThat(user.getIdentityCard()).isNull();
    }
}
