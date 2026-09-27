package com.tastyhouse.infrastructure.shop.persistence;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.shop.port.out.write.ShopChangeHistoryState;
import com.tastyhouse.application.shop.port.out.write.ShopChangeHistoryStatePort;

@Repository
public class ShopChangeHistoryStatePortImpl implements ShopChangeHistoryStatePort {
    private final ShopChangeHistoryJpaRepository shopChangeHistoryJpaRepository;

    public ShopChangeHistoryStatePortImpl(ShopChangeHistoryJpaRepository shopChangeHistoryJpaRepository) {
        this.shopChangeHistoryJpaRepository = shopChangeHistoryJpaRepository;
    }

    @Override
    public ShopChangeHistoryState save(ShopChangeHistoryState shopChangeHistory) {
        ShopChangeHistoryJpaEntity saved = shopChangeHistoryJpaRepository
            .save(ShopChangeHistoryMapper.toEntity(shopChangeHistory));
        return ShopChangeHistoryMapper.toState(saved);
    }
}
