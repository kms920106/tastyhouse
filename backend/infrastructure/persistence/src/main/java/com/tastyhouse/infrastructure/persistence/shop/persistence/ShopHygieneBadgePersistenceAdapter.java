package com.tastyhouse.infrastructure.persistence.shop.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.model.ShopHygieneBadge;
import com.tastyhouse.application.shop.port.out.write.ShopHygieneBadgePersistencePort;

@Repository
class ShopHygieneBadgePersistenceAdapter implements ShopHygieneBadgePersistencePort {

    private final ShopHygieneBadgeJpaRepository shopHygieneBadgeJpaRepository;

    public ShopHygieneBadgePersistenceAdapter(ShopHygieneBadgeJpaRepository shopHygieneBadgeJpaRepository) {
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
