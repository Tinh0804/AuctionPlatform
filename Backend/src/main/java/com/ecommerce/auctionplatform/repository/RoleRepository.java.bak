package com.ecommerce.auctionplatform.repository;

import com.ecommerce.auctionplatform.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByName(String roleName);
    Boolean existsByName(String roleName);
}
