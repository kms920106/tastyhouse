package com.tastyhouse.infrastructure.shop.query;

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

/**
 * 가게-점주 접근권한 이력 read 어댑터(CQRS query 측).
 *
 * <p>{@code SHOP}을 join해 {@code shopName}까지 투영한다 — 소비 Service가 가게를 재조회하지 않게 하기
 * 위함이다. 이력 행은 append-only라 가게가 나중에 폐업해도 남으므로 {@code leftJoin}으로 잇는다.
 *
 * <p>날짜 필터는 <b>반열림 구간</b> {@code [startDate 00:00, endDate+1d 00:00)}으로 만든다 —
 * {@code DATE(created_at) BETWEEN ...}처럼 컬럼에 함수를 씌우면 인덱스를 타지 못한다.
 */
@Repository
public class ShopCeoAssignmentHistoryQueryDao {

    private final JPAQueryFactory queryFactory;

    public ShopCeoAssignmentHistoryQueryDao(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * 내 시스템 접근권한 이력 목록 페이징 — 최신순({@code created_at DESC, id DESC}).
     */
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

    /**
     * 조회 기간을 반열림 구간으로 좁힌다. 종료일은 그날 하루를 포함해야 하므로 다음날 00:00 미만으로 건다.
     */
    private BooleanExpression createdAtBetween(ShopCeoAssignmentHistorySearchCondition condition) {
        LocalDateTime from = condition.startDate().atStartOfDay();
        LocalDateTime until = condition.endDate().plusDays(1).atStartOfDay();
        return shopCeoAssignmentHistoryJpaEntity.createdAt.goe(from)
            .and(shopCeoAssignmentHistoryJpaEntity.createdAt.lt(until));
    }
}
