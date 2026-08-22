package com.tastyhouse.infrastructure.shop.query;

import java.util.Optional;

import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import static com.tastyhouse.infrastructure.shop.persistence.QShopOrderNoticeJpaEntity.shopOrderNoticeJpaEntity;

/**
 * 주문안내 read 어댑터(CQRS query 측).
 *
 * <p>{@code shop} 도메인은 대형이라 용도별 DAO 분리가 허용된다. 주문안내는 가게당 1건 단독 행이고
 * 조인 대상이 없어 {@code ShopQueryDao}의 대형 조립 메서드들과 성격이 다르므로 별도 DAO로 둔다.
 *
 * <p>메서드가 둘로 나뉜 이유는 <b>노출 조건이 소비자에 따라 다르기 때문</b>이다. 점주는 자기 문구가
 * 내려갔다는 사실과 그 사유를 봐야 하므로 게시중단 건도 받고, 손님은 게시중단 건을 아예 받지 않는다.
 * 이 분기를 Service의 if 문으로 옮기면 손님 경로에서 필터를 빠뜨렸을 때 게시중단된 문구가 그대로
 * 노출되는 결함이 되고, 쿼리 자체가 걸러내면 그 실수가 물리적으로 불가능해진다.
 */
@Repository
public class ShopOrderNoticeQueryDao {

    private final JPAQueryFactory queryFactory;

    public ShopOrderNoticeQueryDao(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * 가게의 주문안내(점주 화면) — 게시중단 여부와 무관하게 내려간다. 미설정이면 빈 값이다.
     */
    public Optional<ShopOrderNoticeResult> findOrderNotice(Long shopId) {
        return Optional.ofNullable(queryFactory
            .select(projection())
            .from(shopOrderNoticeJpaEntity)
            .where(shopOrderNoticeJpaEntity.shopId.eq(shopId))
            .fetchFirst());
    }

    /**
     * 가게의 노출 가능한 주문안내(손님 화면) — 게시중단({@code is_hidden = true})된 문구는 걸러진다.
     * 미설정이거나 게시중단이면 빈 값이다.
     */
    public Optional<ShopOrderNoticeResult> findVisibleOrderNotice(Long shopId) {
        return Optional.ofNullable(queryFactory
            .select(projection())
            .from(shopOrderNoticeJpaEntity)
            .where(
                shopOrderNoticeJpaEntity.shopId.eq(shopId),
                shopOrderNoticeJpaEntity.hidden.isFalse()
            )
            .fetchFirst());
    }

    /**
     * 두 조회가 같은 컬럼 묶음을 읽으므로 투영을 한 곳에 둔다 — 복제하면 필드 추가 시 한쪽만 고쳐진다.
     */
    private ConstructorExpression<ShopOrderNoticeResult> projection() {
        return Projections.constructor(ShopOrderNoticeResult.class,
            shopOrderNoticeJpaEntity.id,
            shopOrderNoticeJpaEntity.shopId,
            shopOrderNoticeJpaEntity.content,
            shopOrderNoticeJpaEntity.hidden,
            shopOrderNoticeJpaEntity.hiddenReason
        );
    }
}
