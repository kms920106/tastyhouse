package com.tastyhouse.infrastructure.shop.persistence;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.shop.port.out.write.ShopChangeHistoryRepository;
import com.tastyhouse.domain.shop.model.ShopChangeHistory;

@Repository
public class ShopChangeHistoryRepositoryImpl implements ShopChangeHistoryRepository {
    private final ShopChangeHistoryJpaRepository shopChangeHistoryJpaRepository;

    public ShopChangeHistoryRepositoryImpl(ShopChangeHistoryJpaRepository shopChangeHistoryJpaRepository) {
        this.shopChangeHistoryJpaRepository = shopChangeHistoryJpaRepository;
    }

    @Override
    public ShopChangeHistory save(ShopChangeHistory shopChangeHistory) {
        ShopChangeHistoryJpaEntity saved = shopChangeHistoryJpaRepository
            .save(ShopChangeHistoryMapper.toEntity(shopChangeHistory));
        return ShopChangeHistoryMapper.toDomain(saved);
    }
}
