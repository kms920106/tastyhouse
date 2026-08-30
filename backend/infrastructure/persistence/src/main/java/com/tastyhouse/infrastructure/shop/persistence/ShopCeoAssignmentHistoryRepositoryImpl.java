package com.tastyhouse.infrastructure.shop.persistence;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.model.ShopCeoAssignmentHistory;
import com.tastyhouse.domain.shop.repository.ShopCeoAssignmentHistoryRepository;

/**
 * 가게-점주 접근권한 이력 write 어댑터.
 *
 * <p>append-only라 insert 경로만 있다 — 다른 어댑터의 {@code save}가 갖는 "id가 있으면 managed 엔티티를
 * 찾아 필드 복사" update 분기가 필요 없다.
 */
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
