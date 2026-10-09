package com.tastyhouse.infrastructure.persistence.shop.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.model.ShopOriginInfo;
import com.tastyhouse.application.shop.port.out.write.ShopOriginInfoLoadPort;
import com.tastyhouse.application.shop.port.out.write.ShopOriginInfoSavePort;

import static com.tastyhouse.infrastructure.persistence.shop.persistence.QShopOriginInfoJpaEntity.shopOriginInfoJpaEntity;

@Repository
class ShopOriginInfoPersistenceAdapter implements ShopOriginInfoLoadPort, ShopOriginInfoSavePort {

    private final JPAQueryFactory queryFactory;
    private final ShopOriginInfoJpaRepository shopOriginInfoJpaRepository;

    public ShopOriginInfoPersistenceAdapter(JPAQueryFactory queryFactory, ShopOriginInfoJpaRepository shopOriginInfoJpaRepository) {
        this.queryFactory = queryFactory;
        this.shopOriginInfoJpaRepository = shopOriginInfoJpaRepository;
    }

    @Override
    public Optional<ShopOriginInfo> findByShopId(Long shopId) {
        ShopOriginInfoJpaEntity entity = queryFactory
            .selectFrom(shopOriginInfoJpaEntity)
            .where(shopOriginInfoJpaEntity.shopId.eq(shopId))
            .fetchOne();
        return Optional.ofNullable(entity).map(ShopOriginInfoMapper::toDomain);
    }

    @Override
    public ShopOriginInfo save(ShopOriginInfo shopOriginInfo) {
        if (shopOriginInfo.getId() == null) {
            ShopOriginInfoJpaEntity saved = shopOriginInfoJpaRepository.save(ShopOriginInfoMapper.toEntity(shopOriginInfo));
            return ShopOriginInfoMapper.toDomain(saved);
        }

        ShopOriginInfoJpaEntity entity = shopOriginInfoJpaRepository.findById(shopOriginInfo.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 가게 원산지 정보입니다: " + shopOriginInfo.getId()));
        ShopOriginInfoMapper.applyChanges(entity, shopOriginInfo);
        return ShopOriginInfoMapper.toDomain(entity);
    }
}
