package com.tastyhouse.infrastructure.jpa.shop.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.model.ShopOwnerMessageHistory;
import com.tastyhouse.application.shop.port.out.write.ShopOwnerMessageHistoryLoadPort;
import com.tastyhouse.application.shop.port.out.write.ShopOwnerMessageHistorySavePort;

import static com.tastyhouse.infrastructure.jpa.shop.persistence.QShopOwnerMessageHistoryJpaEntity.shopOwnerMessageHistoryJpaEntity;

@Repository
class ShopOwnerMessageHistoryPersistenceAdapter implements ShopOwnerMessageHistoryLoadPort, ShopOwnerMessageHistorySavePort {

    private final JPAQueryFactory queryFactory;
    private final ShopOwnerMessageHistoryJpaRepository shopOwnerMessageHistoryJpaRepository;

    public ShopOwnerMessageHistoryPersistenceAdapter(
        JPAQueryFactory queryFactory,
        ShopOwnerMessageHistoryJpaRepository shopOwnerMessageHistoryJpaRepository
    ) {
        this.queryFactory = queryFactory;
        this.shopOwnerMessageHistoryJpaRepository = shopOwnerMessageHistoryJpaRepository;
    }

    @Override
    public void saveOwnerMessage(ShopOwnerMessageHistory ownerMessageHistory) {
        shopOwnerMessageHistoryJpaRepository.save(
            ShopOwnerMessageHistoryMapper.toEntity(ownerMessageHistory)
        );
    }

    @Override
    public Optional<ShopOwnerMessageHistory> findLatestOwnerMessage(Long shopId) {
        ShopOwnerMessageHistoryJpaEntity entity = queryFactory
            .selectFrom(shopOwnerMessageHistoryJpaEntity)
            .where(shopOwnerMessageHistoryJpaEntity.shopId.eq(shopId))
            .orderBy(shopOwnerMessageHistoryJpaEntity.id.desc())
            .fetchFirst();
        return Optional.ofNullable(entity).map(ShopOwnerMessageHistoryMapper::toDomain);
    }
}
