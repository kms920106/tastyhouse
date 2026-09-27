package com.tastyhouse.infrastructure.shop.persistence;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.shop.port.out.write.ShopCeoAssignmentHistoryState;
import com.tastyhouse.application.shop.port.out.write.ShopCeoAssignmentHistoryStatePort;

@Repository
public class ShopCeoAssignmentHistoryStatePortImpl implements ShopCeoAssignmentHistoryStatePort {
    private final ShopCeoAssignmentHistoryJpaRepository shopCeoAssignmentHistoryJpaRepository;

    public ShopCeoAssignmentHistoryStatePortImpl(
        ShopCeoAssignmentHistoryJpaRepository shopCeoAssignmentHistoryJpaRepository
    ) {
        this.shopCeoAssignmentHistoryJpaRepository = shopCeoAssignmentHistoryJpaRepository;
    }

    @Override
    public ShopCeoAssignmentHistoryState save(ShopCeoAssignmentHistoryState shopCeoAssignmentHistory) {
        ShopCeoAssignmentHistoryJpaEntity saved = shopCeoAssignmentHistoryJpaRepository
            .save(ShopCeoAssignmentHistoryMapper.toEntity(shopCeoAssignmentHistory));
        return ShopCeoAssignmentHistoryMapper.toState(saved);
    }
}
