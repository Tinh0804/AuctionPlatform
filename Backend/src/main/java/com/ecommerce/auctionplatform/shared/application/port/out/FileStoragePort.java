package com.ecommerce.auctionplatform.shared.application.port.out;

import com.ecommerce.auctionplatform.shared.application.model.FileContent;

import java.util.Map;

public interface FileStoragePort {
    String uploadFile(FileContent file, String folderName);

    String uploadFile(FileContent file, String folderName, Map<String, Object> options);
}
