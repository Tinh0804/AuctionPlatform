package com.ecommerce.auctionplatform.identity.application.dto.command;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateUserCommand {
    String name;

    String email;

    Boolean gender;
    
    LocalDate dob;

    String avatarImage;
}
