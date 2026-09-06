package com.tastyhouse.infrastructure.shop.query;

import com.tastyhouse.application.shop.port.out.ShopRiderGuideManagementQueryPort;
import com.tastyhouse.application.shop.port.out.ShopRiderGuideQueryPort;
import com.tastyhouse.application.shop.port.out.ShopRiderGuideHistoryResult;
import com.tastyhouse.application.shop.port.out.ShopRiderGuideListItemResult;
import com.tastyhouse.application.shop.port.out.ShopRiderGuideResult;
import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

import static com.tastyhouse.infrastructure.shop.persistence.QShopJpaEntity.shopJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopRiderGuideHistoryJpaEntity.shopRiderGuideHistoryJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopRiderGuideJpaEntity.shopRiderGuideJpaEntity;

@Repository
public class ShopRiderGuideQueryDao implements ShopRiderGuideQueryPort, ShopRiderGuideManagementQueryPort {
    private static final int HISTORY_LIMIT = 20;

    private final JPAQueryFactory queryFactory;

    public ShopRiderGuideQueryDao(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public Optional<ShopRiderGuideResult> findRiderGuide(Long shopId) {
        return Optional.ofNullable(
            queryFactory
                .select(Projections.constructor(ShopRiderGuideResult.class,
                    shopJpaEntity.id,
                    shopJpaEntity.name,
                    shopRiderGuideJpaEntity.visitGuide,
                    shopRiderGuideJpaEntity.pickupRoadAddress,
                    shopRiderGuideJpaEntity.pickupLotAddress,
                    shopRiderGuideJpaEntity.pickupDetailAddress,
                    shopRiderGuideJpaEntity.pickupLatitude,
                    shopRiderGuideJpaEntity.pickupLongitude,
                    shopJpaEntity.roadAddress,
                    shopJpaEntity.lotAddress,
                    shopJpaEntity.latitude,
                    shopJpaEntity.longitude,
                    shopRiderGuideJpaEntity.updatedAt
                ))
                .from(shopJpaEntity)
                .leftJoin(shopRiderGuideJpaEntity).on(shopRiderGuideJpaEntity.shopId.eq(shopJpaEntity.id))
                .where(shopJpaEntity.id.eq(shopId))
                .fetchFirst()
        );
    }

    @Override
    public PageResult<ShopRiderGuideListItemResult> findRiderGuidePage(
        String shopName,
        Boolean hasVisitGuide,
        PageQuery pageQuery
    ) {
        Long total = queryFactory
            .select(shopRiderGuideJpaEntity.count())
            .from(shopRiderGuideJpaEntity)
            .join(shopJpaEntity).on(shopJpaEntity.id.eq(shopRiderGuideJpaEntity.shopId))
            .where(
                shopNameContains(shopName),
                visitGuidePresenceEq(hasVisitGuide)
            )
            .fetchOne();

        if (total == null || total == 0) {
            return PageResult.empty(pageQuery.page(), pageQuery.size());
        }

        List<ShopRiderGuideListItemResult> content = queryFactory
            .select(Projections.constructor(ShopRiderGuidePickupPresenceResult.class,
                shopRiderGuideJpaEntity.shopId,
                shopJpaEntity.name,
                shopRiderGuideJpaEntity.visitGuide,
                shopRiderGuideJpaEntity.pickupRoadAddress,
                shopRiderGuideJpaEntity.pickupLatitude,
                shopRiderGuideJpaEntity.pickupLongitude,
                shopRiderGuideJpaEntity.updatedAt
            ))
            .from(shopRiderGuideJpaEntity)
            .join(shopJpaEntity).on(shopJpaEntity.id.eq(shopRiderGuideJpaEntity.shopId))
            .where(
                shopNameContains(shopName),
                visitGuidePresenceEq(hasVisitGuide)
            )
            .orderBy(shopRiderGuideJpaEntity.updatedAt.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch()
            .stream()
            .map(ShopRiderGuidePickupPresenceResult::toListItem)
            .toList();

        return PageResult.of(content, total, pageQuery.page(), pageQuery.size());
    }

    @Override
    public List<ShopRiderGuideHistoryResult> findHistories(Long shopId) {
        return queryFactory
            .select(Projections.constructor(ShopRiderGuideHistoryResult.class,
                shopRiderGuideHistoryJpaEntity.id,
                shopRiderGuideHistoryJpaEntity.actorType,
                shopRiderGuideHistoryJpaEntity.actorId,
                shopRiderGuideHistoryJpaEntity.actionType,
                shopRiderGuideHistoryJpaEntity.previousVisitGuide,
                shopRiderGuideHistoryJpaEntity.newVisitGuide,
                shopRiderGuideHistoryJpaEntity.reason,
                shopRiderGuideHistoryJpaEntity.createdAt
            ))
            .from(shopRiderGuideHistoryJpaEntity)
            .where(shopRiderGuideHistoryJpaEntity.shopId.eq(shopId))
            .orderBy(shopRiderGuideHistoryJpaEntity.createdAt.desc(), shopRiderGuideHistoryJpaEntity.id.desc())
            .limit(HISTORY_LIMIT)
            .fetch();
    }

    private BooleanExpression shopNameContains(String shopName) {
        return shopName == null || shopName.isBlank() ? null : shopJpaEntity.name.contains(shopName);
    }

    private BooleanExpression visitGuidePresenceEq(Boolean hasVisitGuide) {
        if (hasVisitGuide == null) {
            return null;
        }
        return hasVisitGuide
            ? shopRiderGuideJpaEntity.visitGuide.isNotNull()
            : shopRiderGuideJpaEntity.visitGuide.isNull();
    }
}
