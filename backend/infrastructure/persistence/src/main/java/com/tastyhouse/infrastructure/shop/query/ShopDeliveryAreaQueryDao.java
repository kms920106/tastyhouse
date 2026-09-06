package com.tastyhouse.infrastructure.shop.query;

import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaQueryPort;
import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaItemResult;
import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaPolygonResult;
import com.tastyhouse.application.shop.port.out.ShopLocationResult;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

import static com.tastyhouse.infrastructure.region.persistence.QAdminDongJpaEntity.adminDongJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopDeliveryAreaJpaEntity.shopDeliveryAreaJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopDeliveryAreaPolygonJpaEntity.shopDeliveryAreaPolygonJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopDeliveryTipRegionJpaEntity.shopDeliveryTipRegionJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopJpaEntity.shopJpaEntity;

@Repository
public class ShopDeliveryAreaQueryDao implements ShopDeliveryAreaQueryPort {
    private final JPAQueryFactory queryFactory;

    public ShopDeliveryAreaQueryDao(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public List<ShopDeliveryAreaItemResult> findDeliveryAreas(Long shopId) {
        return queryFactory
            .select(Projections.constructor(ShopDeliveryAreaItemResult.class,
                shopDeliveryAreaJpaEntity.id,
                shopDeliveryAreaJpaEntity.adminDongId,
                regionName(),
                shopDeliveryAreaJpaEntity.source.stringValue()
            ))
            .from(shopDeliveryAreaJpaEntity)
            .join(adminDongJpaEntity).on(shopDeliveryAreaJpaEntity.adminDongId.eq(adminDongJpaEntity.id))
            .where(shopDeliveryAreaJpaEntity.shopId.eq(shopId))
            .orderBy(shopDeliveryAreaJpaEntity.id.asc())
            .fetch();
    }

    @Override
    public Set<Long> findAdminDongIds(Long shopId) {
        return Set.copyOf(queryFactory
            .select(shopDeliveryAreaJpaEntity.adminDongId)
            .from(shopDeliveryAreaJpaEntity)
            .where(shopDeliveryAreaJpaEntity.shopId.eq(shopId))
            .fetch());
    }

    @Override
    public Set<Long> findAdminDongIdsBySource(Long shopId, String source) {
        return Set.copyOf(queryFactory
            .select(shopDeliveryAreaJpaEntity.adminDongId)
            .from(shopDeliveryAreaJpaEntity)
            .where(
                shopDeliveryAreaJpaEntity.shopId.eq(shopId),
                shopDeliveryAreaJpaEntity.source.stringValue().eq(source)
            )
            .fetch());
    }

    @Override
    public ShopLocationResult findShopLocation(Long ceoId, Long shopId) {
        ShopLocationResult result = queryFactory
            .select(Projections.constructor(ShopLocationResult.class,
                shopJpaEntity.id,
                shopJpaEntity.latitude,
                shopJpaEntity.longitude
            ))
            .from(shopJpaEntity)
            .where(shopJpaEntity.id.eq(shopId), shopJpaEntity.ceoId.eq(ceoId))
            .fetchOne();

        if (result == null) {
            throw new BusinessException(ErrorCode.SHOP_ACCESS_DENIED);
        }
        if (result.latitude() == null || result.longitude() == null) {
            throw new BusinessException(
                ErrorCode.SHOP_DELIVERY_AREA_RADIUS_EXCEEDED,
                "가게 좌표가 등록돼 있지 않아 배달지역을 설정할 수 없습니다."
            );
        }
        return result;
    }

    @Override
    public Optional<ShopDeliveryAreaPolygonResult> findPolygon(Long shopId) {
        return Optional.ofNullable(queryFactory
            .select(Projections.constructor(ShopDeliveryAreaPolygonResult.class,
                shopDeliveryAreaPolygonJpaEntity.id,
                shopDeliveryAreaPolygonJpaEntity.rings,
                shopDeliveryAreaPolygonJpaEntity.centerLatitude,
                shopDeliveryAreaPolygonJpaEntity.centerLongitude,
                shopDeliveryAreaPolygonJpaEntity.maxRadiusMeters,
                shopDeliveryAreaPolygonJpaEntity.ringCount,
                shopDeliveryAreaPolygonJpaEntity.vertexCount,
                shopDeliveryAreaPolygonJpaEntity.updatedAt
            ))
            .from(shopDeliveryAreaPolygonJpaEntity)
            .where(shopDeliveryAreaPolygonJpaEntity.shopId.eq(shopId))
            .fetchOne());
    }

    @Override
    public Set<Long> findRegionTipAdminDongIds(Long shopId) {
        return Set.copyOf(queryFactory
            .select(shopDeliveryTipRegionJpaEntity.adminDongId)
            .from(shopDeliveryTipRegionJpaEntity)
            .where(shopDeliveryTipRegionJpaEntity.shopId.eq(shopId))
            .fetch());
    }

    private StringExpression regionName() {
        return adminDongJpaEntity.sidoName
            .concat(Expressions.asString(" "))
            .concat(adminDongJpaEntity.sigunguName)
            .concat(Expressions.asString(" "))
            .concat(adminDongJpaEntity.dongName);
    }
}
