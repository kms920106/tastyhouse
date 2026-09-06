package com.tastyhouse.infrastructure.shop.persistence;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.model.ShopCeoAssignmentHistory;
import com.tastyhouse.domain.shop.repository.ShopCeoAssignmentHistoryRepository;

@Repository
public class ShopCeoAssignmentHistoryRepositoryImpl implements ShopCeoAssignmentHistoryRepository {
    private final ShopCeoAssignmentHistoryJpaRepository shopCeoAssignmentHistoryJpaRepository;

    public ShopCeoAssignmentHistoryRepositoryImpl(
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
