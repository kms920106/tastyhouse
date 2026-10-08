package com.tastyhouse.infrastructure.persistence.shop.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.model.ShopConvenienceInfo;
import com.tastyhouse.application.shop.port.out.write.ShopConvenienceInfoPersistencePort;

import static com.tastyhouse.infrastructure.persistence.shop.persistence.QShopConvenienceInfoJpaEntity.shopConvenienceInfoJpaEntity;

@Repository
class ShopConvenienceInfoPersistenceAdapter implements ShopConvenienceInfoPersistencePort {

    private final JPAQueryFactory queryFactory;
    private final ShopConvenienceInfoJpaRepository shopConvenienceInfoJpaRepository;

    public ShopConvenienceInfoPersistenceAdapter(JPAQueryFactory queryFactory, ShopConvenienceInfoJpaRepository shopConvenienceInfoJpaRepository) {
        this.queryFactory = queryFactory;
        this.shopConvenienceInfoJpaRepository = shopConvenienceInfoJpaRepository;
    }

    @Override
    public Optional<ShopConvenienceInfo> findByShopId(Long shopId) {
        ShopConvenienceInfoJpaEntity entity = queryFactory
            .selectFrom(shopConvenienceInfoJpaEntity)
            .where(shopConvenienceInfoJpaEntity.shopId.eq(shopId))
            .fetchOne();
        return Optional.ofNullable(entity).map(ShopConvenienceInfoMapper::toDomain);
    }

    @Override
    public ShopConvenienceInfo save(ShopConvenienceInfo shopConvenienceInfo) {
        if (shopConvenienceInfo.getId() == null) {
            ShopConvenienceInfoJpaEntity saved = shopConvenienceInfoJpaRepository.save(ShopConvenienceInfoMapper.toEntity(shopConvenienceInfo));
            return ShopConvenienceInfoMapper.toDomain(saved);
        }

        ShopConvenienceInfoJpaEntity entity = shopConvenienceInfoJpaRepository.findById(shopConvenienceInfo.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 가게 편의정보입니다: " + shopConvenienceInfo.getId()));
        ShopConvenienceInfoMapper.applyChanges(entity, shopConvenienceInfo);
        return ShopConvenienceInfoMapper.toDomain(entity);
    }
}
