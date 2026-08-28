package com.ecommerce.auctionplatform.identity.infrastructure.persistence.repository;

import com.ecommerce.auctionplatform.identity.domain.model.User;
import com.ecommerce.auctionplatform.identity.domain.repository.UserRepository;
import com.ecommerce.auctionplatform.identity.infrastructure.persistence.mapper.UserPersistenceMapper;
import com.ecommerce.auctionplatform.identity.domain.valueobject.UserSearchCriteria;
import com.ecommerce.auctionplatform.shared.domain.model.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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
    public java.util.List<User> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public java.util.List<User> findByAccountRoleName(String roleName) {
        return jpaRepository.findByAccount_Role_Name(roleName).stream().map(mapper::toDomain).toList();
    }

    @Override
    public PageResult<User> searchNonAdmin(UserSearchCriteria criteria) {
        Specification<com.ecommerce.auctionplatform.identity.infrastructure.persistence.entity.UserEntity> spec =
                (root, query, cb) -> {
                    var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
                    predicates.add(cb.notEqual(root.get("account").get("role").get("name"), "ADMIN"));
                    if (criteria.keyword() != null && !criteria.keyword().isBlank()) {
                        String value = "%" + criteria.keyword().trim().toLowerCase() + "%";
                        predicates.add(cb.or(
                                cb.like(cb.lower(root.get("name")), value),
                                cb.like(cb.lower(root.get("email")), value),
                                cb.like(cb.lower(root.get("phone")), value),
                                cb.like(cb.lower(root.get("account").get("username")), value)));
                    }
                    return cb.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
                };
        String sortProperty = switch (criteria.sortBy()) {
            case "email", "phone", "dob", "reputationScore" -> criteria.sortBy();
            default -> "name";
        };
        Sort sort = Sort.by(criteria.ascending() ? Sort.Direction.ASC : Sort.Direction.DESC, sortProperty);
        var page = jpaRepository.findAll(spec, PageRequest.of(criteria.pageNumber(), criteria.pageSize(), sort));
        return new PageResult<>(
                page.getContent().stream().map(mapper::toDomain).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements());
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
