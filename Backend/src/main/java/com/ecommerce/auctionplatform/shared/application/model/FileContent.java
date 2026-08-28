package com.ecommerce.auctionplatform.shared.application.model;

public record FileContent(String filename, String contentType, byte[] bytes) {
    public FileContent {
        bytes = bytes == null ? new byte[0] : bytes.clone();
    }

    @Override
    public byte[] bytes() {
        return bytes.clone();
    }
}
