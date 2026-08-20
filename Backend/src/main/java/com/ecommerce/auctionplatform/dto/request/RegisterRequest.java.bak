package com.ecommerce.auctionplatform.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegisterRequest {

    @NotBlank(message = "Username must not be blank")
    @Size(min = 3, message = "Username must be at least 3 characters")
    String userName;

    @NotBlank(message = "Password must not be blank")
    @Size(min = 5, message = "Password must be at least 5 characters")
    String passWord;

    @NotBlank(message = "Full name must not be blank")
    String fullName;

    @NotBlank(message = "Phone number must not be blank")
    String phone;

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email should be valid")
    String email;

}
