package com.ecommerce.auctionplatform.user.infrastructure.persistence.repository;

import com.ecommerce.auctionplatform.user.domain.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByName(String roleName);
    Boolean existsByName(String roleName);
}
