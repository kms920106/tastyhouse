package com.tastyhouse.infrastructure.shop.persistence;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.model.ShopChangeHistory;
import com.tastyhouse.application.shop.port.out.write.ShopChangeHistoryPersistencePort;

@Repository
public class ShopChangeHistoryPersistenceAdapter implements ShopChangeHistoryPersistencePort {

    private final ShopChangeHistoryJpaRepository shopChangeHistoryJpaRepository;

    public ShopChangeHistoryPersistenceAdapter(ShopChangeHistoryJpaRepository shopChangeHistoryJpaRepository) {
        this.shopChangeHistoryJpaRepository = shopChangeHistoryJpaRepository;
    }

    @Override
    public ShopChangeHistory save(ShopChangeHistory shopChangeHistory) {
        ShopChangeHistoryJpaEntity saved = shopChangeHistoryJpaRepository
            .save(ShopChangeHistoryMapper.toEntity(shopChangeHistory));
        return ShopChangeHistoryMapper.toDomain(saved);
    }
}
