package com.tastyhouse.infrastructure.shop.persistence;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.model.ShopCeoAssignmentHistory;
import com.tastyhouse.application.shop.port.out.write.ShopCeoAssignmentHistoryPersistencePort;

@Repository
public class ShopCeoAssignmentHistoryPersistenceAdapter implements ShopCeoAssignmentHistoryPersistencePort {
    private final ShopCeoAssignmentHistoryJpaRepository shopCeoAssignmentHistoryJpaRepository;

    public ShopCeoAssignmentHistoryPersistenceAdapter(
        ShopCeoAssignmentHistoryJpaRepository shopCeoAssignmentHistoryJpaRepository
    ) {
        this.shopCeoAssignmentHistoryJpaRepository = shopCeoAssignmentHistoryJpaRepository;
    }

    @Override
    public ShopCeoAssignmentHistory save(ShopCeoAssignmentHistory shopCeoAssignmentHistory) {
        ShopCeoAssignmentHistoryJpaEntity saved = shopCeoAssignmentHistoryJpaRepository
            .save(ShopCeoAssignmentHistoryMapper.toEntity(shopCeoAssignmentHistory));
        return ShopCeoAssignmentHistoryMapper.toDomain(saved);
    }
}
