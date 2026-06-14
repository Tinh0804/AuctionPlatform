package com.ecommerce.auctionplatform.repository;

import com.ecommerce.auctionplatform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
        Optional<User> findByAccountId(UUID accountId);
        Boolean existsByPhone(String phone);
        Boolean existsByEmail(String email);
}
