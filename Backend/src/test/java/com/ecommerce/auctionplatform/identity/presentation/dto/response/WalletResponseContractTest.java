package com.ecommerce.auctionplatform.identity.presentation.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WalletResponseContractTest {

    @Test
    void walletFieldsKeepTheirExistingSnakeCaseHttpContract() {
        WalletResponse response = new WalletResponse(
                UUID.randomUUID(),
                BigDecimal.TEN,
                BigDecimal.ONE,
                true,
                "ACTIVE"
        );

        JsonNode json = new ObjectMapper().valueToTree(response);

        assertTrue(json.has("available_balance"));
        assertTrue(json.has("frozen_balance"));
        assertTrue(json.has("has_pin"));
        assertFalse(json.has("availableBalance"));
    }
}
