package com.tastyhouse.infrastructure.shop.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.model.ShopHygieneBadge;
import com.tastyhouse.domain.shop.repository.ShopHygieneBadgeRepository;

@Repository
public class ShopHygieneBadgeRepositoryImpl implements ShopHygieneBadgeRepository {

    private final ShopHygieneBadgeJpaRepository shopHygieneBadgeJpaRepository;

    public ShopHygieneBadgeRepositoryImpl(ShopHygieneBadgeJpaRepository shopHygieneBadgeJpaRepository) {
        this.shopHygieneBadgeJpaRepository = shopHygieneBadgeJpaRepository;
    }

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
