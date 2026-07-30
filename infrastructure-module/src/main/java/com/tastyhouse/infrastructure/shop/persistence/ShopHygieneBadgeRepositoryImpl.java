package com.tastyhouse.infrastructure.shop.persistence;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.shop.domain.model.ShopHygieneBadge;
import com.tastyhouse.core.domain.shop.domain.repository.ShopHygieneBadgeRepository;

@Repository
@RequiredArgsConstructor
public class ShopHygieneBadgeRepositoryImpl implements ShopHygieneBadgeRepository {

    private final ShopHygieneBadgeJpaRepository shopHygieneBadgeJpaRepository;

    @Override
    public Optional<ShopHygieneBadge> findById(Long id) {
        return shopHygieneBadgeJpaRepository.findById(id).map(ShopHygieneBadgeMapper::toDomain);
    }

    @Override
    public ShopHygieneBadge save(ShopHygieneBadge shopHygieneBadge) {
        ShopHygieneBadgeJpaEntity saved = shopHygieneBadgeJpaRepository.save(ShopHygieneBadgeMapper.toEntity(shopHygieneBadge));
        return ShopHygieneBadgeMapper.toDomain(saved);
    }

    @Override
    public void deleteById(Long id) {
        shopHygieneBadgeJpaRepository.deleteById(id);
    }
}
