package com.tastyhouse.infrastructure.shop.query;

import com.tastyhouse.application.shop.port.out.ShopCeoAssignmentHistoryQueryPort;
import com.tastyhouse.application.shop.port.out.ShopCeoAssignmentHistoryResult;
import com.tastyhouse.application.shop.port.out.ShopCeoAssignmentHistorySearchCondition;
import java.time.LocalDateTime;
import java.util.List;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.domain.shop.model.ShopCeoAssignmentActionType;

import static com.tastyhouse.infrastructure.shop.persistence.QShopCeoAssignmentHistoryJpaEntity.shopCeoAssignmentHistoryJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopJpaEntity.shopJpaEntity;

@Repository
public class ShopCeoAssignmentHistoryQueryDao implements ShopCeoAssignmentHistoryQueryPort {
    private final JPAQueryFactory queryFactory;

    public ShopCeoAssignmentHistoryQueryDao(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public PageResult<ShopCeoAssignmentHistoryResult> findShopAccessHistoryPage(
        ShopCeoAssignmentHistorySearchCondition condition,
        PageQuery pageQuery
    ) {
        BooleanExpression[] predicates = {
            shopCeoAssignmentHistoryJpaEntity.ceoId.eq(condition.ceoId()),
            shopIdEq(condition.shopId()),
            actionTypeEq(condition.actionType()),
            createdAtBetween(condition),
        };

        Long total = queryFactory
            .select(shopCeoAssignmentHistoryJpaEntity.count())
            .from(shopCeoAssignmentHistoryJpaEntity)
            .where(predicates)
            .fetchOne();

        if (total == null || total == 0) {
            return PageResult.empty(pageQuery.page(), pageQuery.size());
        }

        List<ShopCeoAssignmentHistoryResult> content = queryFactory
            .select(Projections.constructor(ShopCeoAssignmentHistoryResult.class,
                shopCeoAssignmentHistoryJpaEntity.id,
                shopCeoAssignmentHistoryJpaEntity.shopId,
                shopJpaEntity.name,
                shopCeoAssignmentHistoryJpaEntity.actionType,
                shopCeoAssignmentHistoryJpaEntity.createdAt
            ))
            .from(shopCeoAssignmentHistoryJpaEntity)
            .leftJoin(shopJpaEntity)
            .on(shopJpaEntity.id.eq(shopCeoAssignmentHistoryJpaEntity.shopId))
            .where(predicates)
            .orderBy(
                shopCeoAssignmentHistoryJpaEntity.createdAt.desc(),
                shopCeoAssignmentHistoryJpaEntity.id.desc()
            )
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        return PageResult.of(content, total, pageQuery.page(), pageQuery.size());
    }

    private BooleanExpression shopIdEq(Long shopId) {
        return shopId != null ? shopCeoAssignmentHistoryJpaEntity.shopId.eq(shopId) : null;
    }

    private BooleanExpression actionTypeEq(ShopCeoAssignmentActionType actionType) {
        return actionType != null ? shopCeoAssignmentHistoryJpaEntity.actionType.eq(actionType) : null;
    }

    private BooleanExpression createdAtBetween(ShopCeoAssignmentHistorySearchCondition condition) {
        LocalDateTime from = condition.startDate().atStartOfDay();
        LocalDateTime until = condition.endDate().plusDays(1).atStartOfDay();
        return shopCeoAssignmentHistoryJpaEntity.createdAt.goe(from)
            .and(shopCeoAssignmentHistoryJpaEntity.createdAt.lt(until));
    }
}
