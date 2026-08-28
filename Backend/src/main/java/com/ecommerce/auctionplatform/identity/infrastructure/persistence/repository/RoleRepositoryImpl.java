package com.ecommerce.auctionplatform.identity.infrastructure.persistence.repository;

import com.ecommerce.auctionplatform.identity.domain.model.Role;
import com.ecommerce.auctionplatform.identity.domain.repository.RoleRepository;
import com.ecommerce.auctionplatform.identity.infrastructure.persistence.mapper.UserPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RoleRepositoryImpl implements RoleRepository {
    private final RoleJpaRepository jpaRepository;
    private final UserPersistenceMapper mapper;

    @Override
    public Role save(Role role) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(role)));
    }

    @Override
    public Optional<Role> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Role> findByName(String name) {
        return jpaRepository.findByName(name).map(mapper::toDomain);
    }

    @Override
    public boolean existsByName(String name) {
        return Boolean.TRUE.equals(jpaRepository.existsByName(name));
    }
}
