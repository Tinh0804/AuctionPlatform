package com.ecommerce.auctionplatform.identity.infrastructure.persistence.repository;

import com.ecommerce.auctionplatform.identity.domain.model.Address;
import com.ecommerce.auctionplatform.identity.domain.repository.AddressRepository;
import com.ecommerce.auctionplatform.identity.infrastructure.persistence.mapper.UserPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AddressRepositoryImpl implements AddressRepository {
    private final AddressJpaRepository jpaRepository;
    private final UserPersistenceMapper mapper;

    @Override
    public Address save(Address address) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(address)));
    }

    @Override
    public Optional<Address> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Address> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
