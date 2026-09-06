package com.tastyhouse.infrastructure.shop.query;

import com.tastyhouse.application.shop.port.out.ShopChangeHistoryQueryPort;
import com.tastyhouse.application.shop.port.out.ShopChangeHistoryResult;
import com.tastyhouse.application.shop.port.out.ShopChangeHistorySearchCondition;
import java.time.LocalDateTime;
import java.util.List;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.domain.shop.model.ShopChangeCategory;
import com.tastyhouse.domain.shop.model.ShopChangeType;

import static com.tastyhouse.infrastructure.shop.persistence.QShopChangeHistoryJpaEntity.shopChangeHistoryJpaEntity;

@Repository
public class ShopChangeHistoryQueryDao implements ShopChangeHistoryQueryPort {
    private final JPAQueryFactory queryFactory;

    public ShopChangeHistoryQueryDao(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public PageResult<ShopChangeHistoryResult> findChangeHistoryPage(
        ShopChangeHistorySearchCondition condition,
        PageQuery pageQuery
    ) {
        BooleanExpression[] predicates = {
            shopChangeHistoryJpaEntity.shopId.eq(condition.shopId()),
            categoryEq(condition.category()),
            changeTypeEq(condition.changeType()),
            createdAtOnDate(condition),
        };

        Long total = queryFactory
            .select(shopChangeHistoryJpaEntity.count())
            .from(shopChangeHistoryJpaEntity)
            .where(predicates)
            .fetchOne();

        if (total == null || total == 0) {
            return PageResult.empty(pageQuery.page(), pageQuery.size());
        }

        List<ShopChangeHistoryResult> content = queryFactory
            .select(Projections.constructor(ShopChangeHistoryResult.class,
                shopChangeHistoryJpaEntity.id,
                shopChangeHistoryJpaEntity.category,
                shopChangeHistoryJpaEntity.changeType,
                shopChangeHistoryJpaEntity.actionType,
                shopChangeHistoryJpaEntity.previousValue,
                shopChangeHistoryJpaEntity.newValue,
                shopChangeHistoryJpaEntity.createdAt
            ))
            .from(shopChangeHistoryJpaEntity)
            .where(predicates)
            .orderBy(shopChangeHistoryJpaEntity.createdAt.desc(), shopChangeHistoryJpaEntity.id.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        return PageResult.of(content, total, pageQuery.page(), pageQuery.size());
    }

    private BooleanExpression categoryEq(ShopChangeCategory category) {
        return category != null ? shopChangeHistoryJpaEntity.category.eq(category) : null;
    }

    private BooleanExpression changeTypeEq(ShopChangeType changeType) {
        return changeType != null ? shopChangeHistoryJpaEntity.changeType.eq(changeType) : null;
    }

    private BooleanExpression createdAtOnDate(ShopChangeHistorySearchCondition condition) {
        LocalDateTime from = condition.changedDate().atStartOfDay();
        LocalDateTime until = condition.changedDate().plusDays(1).atStartOfDay();
        return shopChangeHistoryJpaEntity.createdAt.goe(from)
            .and(shopChangeHistoryJpaEntity.createdAt.lt(until))
            .and(shopChangeHistoryJpaEntity.createdAt.goe(condition.retentionFrom().atStartOfDay()));
    }
}
