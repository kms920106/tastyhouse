package com.tastyhouse.infrastructure.review.query;

import com.tastyhouse.application.review.port.out.ShopReviewDisplaySettingQueryPort;
import com.tastyhouse.application.review.port.out.ShopReviewSortTypeResult;
import java.util.Optional;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.review.model.ReviewSortType;

import static com.tastyhouse.infrastructure.review.persistence.QShopReviewDisplaySettingJpaEntity.shopReviewDisplaySettingJpaEntity;

/**
 * 가게 리뷰 노출 정렬 설정 read 어댑터(CQRS query 측).
 *
 * <p>write 포트({@code ShopReviewDisplaySettingRepository})와 같은 행을 읽지만 중복이 아니다 — 이쪽은
 * <b>조회 전용 소비자</b>를 위한 것이다. web-api의 리뷰 목록은 정렬 기본값을 알아야 하는데, QueryService에
 * write 포트를 주입하면 CQRS 교차 주입 금지 규칙을 어긴다. 그래서 값만 투영하는 이 DAO를 둔다.
 */
@Repository
public class ShopReviewDisplaySettingQueryDao implements ShopReviewDisplaySettingQueryPort {

    private final JPAQueryFactory queryFactory;

    public ShopReviewDisplaySettingQueryDao(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * 가게의 저장된 기본 정렬. 설정 행이 없으면 {@code Optional.empty()}다 — 기본값
     * ({@link ReviewSortType#LATEST})으로 접는 판단은 소비 Service가 한다(행 부재와 명시적 LATEST를
     * 구분해야 {@code updatedAt}을 {@code null}로 응답할 수 있다).
     */
    @Override
    public Optional<ReviewSortType> findSortTypeByShopId(Long shopId) {
        return Optional.ofNullable(queryFactory
            .select(shopReviewDisplaySettingJpaEntity.sortType)
            .from(shopReviewDisplaySettingJpaEntity)
            .where(shopReviewDisplaySettingJpaEntity.shopId.eq(shopId))
            .fetchOne());
    }

    /**
     * 정렬 설정 조회 화면용 투영(정렬값 + 최종 변경 시각). 미설정이면 {@code Optional.empty()}다.
     */
    @Override
    public Optional<ShopReviewSortTypeResult> findSortTypeSettingByShopId(Long shopId) {
        return Optional.ofNullable(queryFactory
            .select(Projections.constructor(ShopReviewSortTypeResult.class,
                shopReviewDisplaySettingJpaEntity.sortType,
                shopReviewDisplaySettingJpaEntity.updatedAt
            ))
            .from(shopReviewDisplaySettingJpaEntity)
            .where(shopReviewDisplaySettingJpaEntity.shopId.eq(shopId))
            .fetchOne());
    }
}
