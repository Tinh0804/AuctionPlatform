package com.ecommerce.auctionplatform.auction.application.dto.command;

import com.ecommerce.auctionplatform.shared.application.model.FileContent;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateAuctionCommand {
    String name;
    String description;
    String origin;
    String categoryId;
    String condition;
    BigDecimal startPrice;
    BigDecimal stepPrice;
    BigDecimal depositAmount;
    LocalDateTime startTime;
    LocalDateTime endTime;
    BigDecimal reservePrice;
    BigDecimal buyNowPrice;
    Boolean autoExtend;
    Integer extendMinutes;
    String relistId;
    List<FileContent> files;
}
