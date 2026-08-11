package com.tastyhouse.infrastructure.shop.query;

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

/**
 * 가게 변경이력 read 어댑터(CQRS query 측).
 *
 * <p>날짜 필터는 <b>반열림 구간</b> {@code [changedDate 00:00, 다음날 00:00)}으로 만든다 —
 * {@code DATE(created_at) = ?}처럼 컬럼에 함수를 씌우면 인덱스를 타지 못한다.
 */
@Repository
public class ShopChangeHistoryQueryDao {

    private final JPAQueryFactory queryFactory;

    public ShopChangeHistoryQueryDao(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * 가게 변경이력 목록 페이징 — 최신순({@code created_at DESC}).
     */
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

    /**
     * 조회 대상 하루를 반열림 구간으로 좁히고, 보관 하한을 함께 건다.
     *
     * <p>하한은 서비스가 이미 400으로 거부한 뒤에도 항상 실어 보내는 정책 이중 안전망이다. 조회 대상
     * 하루가 하한보다 뒤이므로 결과는 달라지지 않지만, 정책이 DAO에도 남아 다른 호출자가 생겨도 6개월
     * 밖 데이터가 새지 않는다.
     */
    private BooleanExpression createdAtOnDate(ShopChangeHistorySearchCondition condition) {
        LocalDateTime from = condition.changedDate().atStartOfDay();
        LocalDateTime until = condition.changedDate().plusDays(1).atStartOfDay();
        return shopChangeHistoryJpaEntity.createdAt.goe(from)
            .and(shopChangeHistoryJpaEntity.createdAt.lt(until))
            .and(shopChangeHistoryJpaEntity.createdAt.goe(condition.retentionFrom().atStartOfDay()));
    }
}
