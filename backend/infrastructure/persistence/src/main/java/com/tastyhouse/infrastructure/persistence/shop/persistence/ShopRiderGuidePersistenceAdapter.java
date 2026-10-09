package com.tastyhouse.infrastructure.persistence.shop.persistence;

import java.util.Optional;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.model.ShopRiderGuide;
import com.tastyhouse.domain.shop.model.ShopRiderGuideHistory;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.out.write.ShopRiderGuideLoadPort;
import com.tastyhouse.application.shop.port.out.write.ShopRiderGuideSavePort;

import static com.tastyhouse.infrastructure.persistence.shop.persistence.QShopRiderGuideJpaEntity.shopRiderGuideJpaEntity;

@Repository
class ShopRiderGuidePersistenceAdapter implements ShopRiderGuideLoadPort, ShopRiderGuideSavePort {

    private final JPAQueryFactory queryFactory;
    private final ShopRiderGuideJpaRepository shopRiderGuideJpaRepository;
    private final ShopRiderGuideHistoryJpaRepository shopRiderGuideHistoryJpaRepository;

    public ShopRiderGuidePersistenceAdapter(
        JPAQueryFactory queryFactory,
        ShopRiderGuideJpaRepository shopRiderGuideJpaRepository,
        ShopRiderGuideHistoryJpaRepository shopRiderGuideHistoryJpaRepository
    ) {
        this.queryFactory = queryFactory;
        this.shopRiderGuideJpaRepository = shopRiderGuideJpaRepository;
        this.shopRiderGuideHistoryJpaRepository = shopRiderGuideHistoryJpaRepository;
    }

    @Override
    public Optional<ShopRiderGuide> findByShopId(ShopId shopId) {
        ShopRiderGuideJpaEntity entity = queryFactory
            .selectFrom(shopRiderGuideJpaEntity)
            .where(shopIdEq(shopId == null ? null : shopId.value()))
            .fetchOne();
        return Optional.ofNullable(entity).map(ShopRiderGuideMapper::toDomain);
    }

    @Override
    public ShopRiderGuide save(ShopRiderGuide riderGuide) {
        if (riderGuide.getId() == null) {
            ShopRiderGuideJpaEntity saved = shopRiderGuideJpaRepository.save(ShopRiderGuideMapper.toEntity(riderGuide));
            return ShopRiderGuideMapper.toDomain(saved);
        }

        ShopRiderGuideJpaEntity entity = shopRiderGuideJpaRepository.findById(riderGuide.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 라이더 안내입니다: " + riderGuide.getId()));
        ShopRiderGuideMapper.applyChanges(entity, riderGuide);
        return ShopRiderGuideMapper.toDomain(entity);
    }

    @Override
    public ShopRiderGuideHistory saveHistory(ShopRiderGuideHistory history) {
        ShopRiderGuideHistoryJpaEntity saved = shopRiderGuideHistoryJpaRepository
            .save(ShopRiderGuideHistoryMapper.toEntity(history));
        return ShopRiderGuideHistoryMapper.toDomain(saved);
    }

    private BooleanExpression shopIdEq(Long shopId) {
        return shopId == null ? shopRiderGuideJpaEntity.shopId.isNull() : shopRiderGuideJpaEntity.shopId.eq(shopId);
    }
}
