package com.ecommerce.auctionplatform.payment.presentation.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DepositResponseContractTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void successfulDepositKeepsTheExistingPaymentUrlShape() {
        JsonNode json = objectMapper.valueToTree(new DepositResponse("https://payment.example", null));

        assertTrue(json.has("payment_url"));
        assertFalse(json.has("paymentUrl"));
        assertFalse(json.has("message"));
    }

    @Test
    void rejectedProviderStillIncludesANullPaymentUrlAndMessage() {
        JsonNode json = objectMapper.valueToTree(new DepositResponse(null, "Unavailable"));

        assertTrue(json.has("payment_url"));
        assertTrue(json.get("payment_url").isNull());
        assertTrue(json.has("message"));
    }
}
