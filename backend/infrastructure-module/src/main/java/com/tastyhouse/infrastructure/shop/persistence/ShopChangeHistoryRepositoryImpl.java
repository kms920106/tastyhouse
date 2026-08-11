package com.tastyhouse.infrastructure.shop.persistence;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.model.ShopChangeHistory;
import com.tastyhouse.domain.shop.repository.ShopChangeHistoryRepository;

/**
 * 가게 변경이력 write 어댑터.
 *
 * <p>append-only라 insert 경로만 있다 — 다른 어댑터의 {@code save}가 갖는 "id가 있으면 managed 엔티티를
 * 찾아 필드 복사" update 분기가 필요 없다.
 */
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
