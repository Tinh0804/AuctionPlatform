package com.ecommerce.auctionplatform.identity.infrastructure.persistence.repository;

import com.ecommerce.auctionplatform.identity.domain.model.User;
import com.ecommerce.auctionplatform.identity.domain.repository.UserRepository;
import com.ecommerce.auctionplatform.identity.infrastructure.persistence.mapper.UserPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    private final UserJpaRepository jpaRepository;
    private final UserPersistenceMapper mapper;

    @Override
    public User save(User user) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(user)));
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public java.util.List<User> findAllById(Iterable<UUID> ids) {
        return jpaRepository.findAllById(ids).stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<User> findByAccountId(UUID accountId) {
        return jpaRepository.findByAccountId(accountId).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findFirstByAccountRoleId(UUID roleId) {
        return jpaRepository.findFirstByAccountRoleId(roleId).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findFirstByAccountRoleName(String roleName) {
        return jpaRepository.findFirstByAccount_Role_Name(roleName).map(mapper::toDomain);
    }

    @Override
    public boolean existsByPhone(String phone) {
        return Boolean.TRUE.equals(jpaRepository.existsByPhone(phone));
    }

    @Override
    public boolean existsByEmail(String email) {
        return Boolean.TRUE.equals(jpaRepository.existsByEmail(email));
    }

}
