package com.tastyhouse.infrastructure.shop.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.shop.port.out.write.ShopHygieneBadgeState;
import com.tastyhouse.application.shop.port.out.write.ShopHygieneBadgeStatePort;

@Repository
public class ShopHygieneBadgeStatePortImpl implements ShopHygieneBadgeStatePort {
    private final ShopHygieneBadgeJpaRepository shopHygieneBadgeJpaRepository;

    public ShopHygieneBadgeStatePortImpl(ShopHygieneBadgeJpaRepository shopHygieneBadgeJpaRepository) {
        this.shopHygieneBadgeJpaRepository = shopHygieneBadgeJpaRepository;
    }

    @Override
    public Optional<ShopHygieneBadgeState> findById(Long id) {
        return shopHygieneBadgeJpaRepository.findById(id).map(ShopHygieneBadgeMapper::toState);
    }

    @Override
    public ShopHygieneBadgeState save(ShopHygieneBadgeState shopHygieneBadge) {
        ShopHygieneBadgeJpaEntity saved = shopHygieneBadgeJpaRepository.save(ShopHygieneBadgeMapper.toEntity(shopHygieneBadge));
        return ShopHygieneBadgeMapper.toState(saved);
    }

    @Override
    public void deleteById(Long id) {
        shopHygieneBadgeJpaRepository.deleteById(id);
    }
}
