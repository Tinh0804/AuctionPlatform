package com.ecommerce.auctionplatform.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminUserUpdateRequest {
    @NotBlank(message = "Tên không được để trống")
    String name;

    @NotBlank(message = "Số điện thoại không được để trống")
    String phone;

    @Email(message = "Email không đúng định dạng")
    String email;

    String identityCard;

    Boolean gender;

    LocalDate dob;

    @Min(value = 0, message = "Điểm uy tín không thể nhỏ hơn 0")
    Integer reputationScore;
}
