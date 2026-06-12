package com.ecommerce.auctionplatform.repository;

import com.ecommerce.auctionplatform.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findById(String roleId);
    Optional<Role> findByName(String roleName);
    Boolean existsByName(String roleName);
}
