package com.ecommerce.auctionplatform.controller;

import com.ecommerce.auctionplatform.dto.respose.APIResponse;
import com.ecommerce.auctionplatform.dto.respose.AuctionCreationResponse;
import com.ecommerce.auctionplatform.dto.respose.CategoryResponse;
import com.ecommerce.auctionplatform.service.AuctionService;
import com.ecommerce.auctionplatform.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/auctions")
@RequiredArgsConstructor
public class AuctionController {

    private final AuctionService auctionService;
    private final CategoryService categoryService;

    @GetMapping("/categories")
    public APIResponse<List<CategoryResponse>> getCategories() {
        return APIResponse.<List<CategoryResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Categories fetched successfully")
                .result(categoryService.getAllCategories())
                .build();
    }

    @PostMapping("/create-auction")
    public APIResponse<AuctionCreationResponse> createAuction(@ModelAttribute com.ecommerce.auctionplatform.dto.request.AuctionCreationRequest request) throws IOException {

        AuctionCreationResponse response = auctionService.createAuction(request);

        return APIResponse.<AuctionCreationResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Auction created successfully")
                .result(response)
                .build();
    }
}
