package com.ecommerce.auctionplatform.shared.presentation.mapper;

import com.ecommerce.auctionplatform.shared.application.model.FileContent;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;

public final class FileUploadMapper {
    private FileUploadMapper() {
    }

    public static FileContent toContent(MultipartFile file) {
        try {
            return new FileContent(file.getOriginalFilename(), file.getContentType(), file.getBytes());
        } catch (IOException exception) {
            throw new UncheckedIOException("Unable to read uploaded file", exception);
        }
    }

    public static List<FileContent> toContents(MultipartFile[] files) {
        if (files == null) {
            return List.of();
        }
        return Arrays.stream(files)
                .filter(file -> file != null && !file.isEmpty())
                .map(FileUploadMapper::toContent)
                .toList();
    }
}
