package com.ecommerce.auctionplatform.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {
    @NotBlank(message = "Name cannot be blank")
    String name;

    @Email(message = "Invalid email format")
    String email;

    Boolean gender;
    
    LocalDate dob;

    String avatarImage;
}
