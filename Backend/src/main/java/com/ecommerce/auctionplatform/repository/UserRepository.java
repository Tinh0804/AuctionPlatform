package com.ecommerce.auctionplatform.repository;

import com.ecommerce.auctionplatform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {
        Optional<User> findByAccountId(UUID accountId);
        Optional<User> findFirstByAccountRoleId(UUID roleId);
        Boolean existsByPhone(String phone);
        Boolean existsByEmail(String email);
        Optional<User> findFirstByAccount_Role_Name(String roleName);
}
