package com.tastyhouse.infrastructure.jpa.shop.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.model.ShopDeliveryAreaPolygon;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryAreaPolygonLoadPort;
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryAreaPolygonSavePort;

import static com.tastyhouse.infrastructure.jpa.shop.persistence.QShopDeliveryAreaPolygonJpaEntity.shopDeliveryAreaPolygonJpaEntity;

@Repository
class ShopDeliveryAreaPolygonPersistenceAdapter implements ShopDeliveryAreaPolygonLoadPort, ShopDeliveryAreaPolygonSavePort {

    private final JPAQueryFactory queryFactory;
    private final ShopDeliveryAreaPolygonJpaRepository shopDeliveryAreaPolygonJpaRepository;

    public ShopDeliveryAreaPolygonPersistenceAdapter(JPAQueryFactory queryFactory, ShopDeliveryAreaPolygonJpaRepository shopDeliveryAreaPolygonJpaRepository) {
        this.queryFactory = queryFactory;
        this.shopDeliveryAreaPolygonJpaRepository = shopDeliveryAreaPolygonJpaRepository;
    }

    @Override
    public Optional<ShopDeliveryAreaPolygon> findByShopId(ShopId shopId) {
        ShopDeliveryAreaPolygonJpaEntity entity = queryFactory
            .selectFrom(shopDeliveryAreaPolygonJpaEntity)
            .where(shopDeliveryAreaPolygonJpaEntity.shopId.eq(shopId.value()))
            .fetchOne();
        return Optional.ofNullable(entity).map(ShopDeliveryAreaPolygonMapper::toDomain);
    }

    @Override
    public ShopDeliveryAreaPolygon save(ShopDeliveryAreaPolygon shopDeliveryAreaPolygon) {
        if (shopDeliveryAreaPolygon.getId() == null) {
            ShopDeliveryAreaPolygonJpaEntity saved = shopDeliveryAreaPolygonJpaRepository
                .save(ShopDeliveryAreaPolygonMapper.toEntity(shopDeliveryAreaPolygon));
            return ShopDeliveryAreaPolygonMapper.toDomain(saved);
        }

        ShopDeliveryAreaPolygonJpaEntity managed = shopDeliveryAreaPolygonJpaRepository
            .findById(shopDeliveryAreaPolygon.getId())
            .orElseThrow(() -> new IllegalStateException(
                "저장 대상 배달지역 도형을 찾을 수 없습니다: " + shopDeliveryAreaPolygon.getId()
            ));
        ShopDeliveryAreaPolygonMapper.applyChanges(managed, shopDeliveryAreaPolygon);
        return ShopDeliveryAreaPolygonMapper.toDomain(managed);
    }

    @Override
    public void deleteByShopId(ShopId shopId) {
        List<ShopDeliveryAreaPolygonJpaEntity> rows = queryFactory
            .selectFrom(shopDeliveryAreaPolygonJpaEntity)
            .where(shopDeliveryAreaPolygonJpaEntity.shopId.eq(shopId.value()))
            .fetch();
        shopDeliveryAreaPolygonJpaRepository.deleteAll(rows);
    }
}
