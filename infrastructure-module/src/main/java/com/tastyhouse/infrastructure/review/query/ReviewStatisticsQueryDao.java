package com.tastyhouse.infrastructure.review.query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.member.domain.vo.MemberId;

import static com.tastyhouse.infrastructure.review.persistence.QReviewJpaEntity.reviewJpaEntity;

/**
 * 리뷰 집계·통계 전용 read 어댑터(CQRS query 측).
 *
 * <p>가게/상품/회원 단위의 리뷰 수·평균 평점·평점 분포·월별 추이를 JPA 엔티티에서 직접 투영한다. 도메인
 * 모델을 거치지 않으므로 write 포트({@code ReviewRepository})와 역할이 겹치지 않는다.
 *
 * <p>도메인당 DAO 1개가 원칙이나 review는 대형 도메인이라 용도별로 분리했다. 목록·상세 조회는
 * {@code ReviewQueryDao}, 관리(admin) 화면 전용 조회는 {@code ReviewManagementQueryDao}가 담당하고,
 * 여기에는 집계·통계만 둔다.
 *
 * <p>소비자: web-api {@code ReviewQueryService}(가게·상품 리뷰 통계 조합, 회원 리뷰 수).
 */
@Repository
@RequiredArgsConstructor
public class ReviewStatisticsQueryDao {

    private final JPAQueryFactory queryFactory;

    /**
     * 가게의 노출 리뷰 수.
     */
    public Long countByShopIdAndHiddenFalse(Long shopId) {
        return queryFactory
            .select(reviewJpaEntity.count())
            .from(reviewJpaEntity)
            .where(shopId().eq(shopId), reviewJpaEntity.hidden.eq(false))
            .fetchOne();
    }

    /**
     * 가게 재방문 의사 리뷰 수.
     */
    public Long countWillRevisit(Long shopId) {
        return queryFactory
            .select(reviewJpaEntity.count())
            .from(reviewJpaEntity)
            .where(
                shopId().eq(shopId),
                reviewJpaEntity.hidden.eq(false),
                reviewJpaEntity.willRevisit.eq(true)
            )
            .fetchOne();
    }

    /**
     * 가게 맛 평점 평균.
     */
    public Double getAverageTasteRating(Long shopId) {
        return queryFactory
            .select(reviewJpaEntity.tasteRating.avg())
            .from(reviewJpaEntity)
            .where(shopId().eq(shopId), reviewJpaEntity.hidden.eq(false))
            .fetchOne();
    }

    /**
     * 가게 양 평점 평균.
     */
    public Double getAverageAmountRating(Long shopId) {
        return queryFactory
            .select(reviewJpaEntity.amountRating.avg())
            .from(reviewJpaEntity)
            .where(shopId().eq(shopId), reviewJpaEntity.hidden.eq(false))
            .fetchOne();
    }

    /**
     * 가게 가격 평점 평균.
     */
    public Double getAveragePriceRating(Long shopId) {
        return queryFactory
            .select(reviewJpaEntity.priceRating.avg())
            .from(reviewJpaEntity)
            .where(shopId().eq(shopId), reviewJpaEntity.hidden.eq(false))
            .fetchOne();
    }

    /**
     * 가게 분위기 평점 평균.
     */
    public Double getAverageAtmosphereRating(Long shopId) {
        return queryFactory
            .select(reviewJpaEntity.atmosphereRating.avg())
            .from(reviewJpaEntity)
            .where(shopId().eq(shopId), reviewJpaEntity.hidden.eq(false))
            .fetchOne();
    }

    /**
     * 가게 친절 평점 평균.
     */
    public Double getAverageKindnessRating(Long shopId) {
        return queryFactory
            .select(reviewJpaEntity.kindnessRating.avg())
            .from(reviewJpaEntity)
            .where(shopId().eq(shopId), reviewJpaEntity.hidden.eq(false))
            .fetchOne();
    }

    /**
     * 가게 위생 평점 평균.
     */
    public Double getAverageHygieneRating(Long shopId) {
        return queryFactory
            .select(reviewJpaEntity.hygieneRating.avg())
            .from(reviewJpaEntity)
            .where(shopId().eq(shopId), reviewJpaEntity.hidden.eq(false))
            .fetchOne();
    }

    /**
     * 가게의 총점 구간(내림 정수)별 리뷰 수.
     */
    public Map<Integer, Long> getRatingCounts(Long shopId) {
        List<Tuple> results = queryFactory
            .select(reviewJpaEntity.totalRating.floor().intValue(), reviewJpaEntity.count())
            .from(reviewJpaEntity)
            .where(shopId().eq(shopId), reviewJpaEntity.hidden.eq(false))
            .groupBy(reviewJpaEntity.totalRating.floor().intValue())
            .fetch();

        Map<Integer, Long> ratingMap = new HashMap<>();
        for (Tuple row : results) {
            ratingMap.put(row.get(0, Integer.class), row.get(1, Long.class));
        }
        return ratingMap;
    }

    /**
     * 가게의 해당 연도 월별 리뷰 수.
     */
    public Map<Integer, Long> getMonthlyReviewCounts(Long shopId, int year) {
        List<Tuple> results = queryFactory
            .select(reviewJpaEntity.createdAt.month(), reviewJpaEntity.count())
            .from(reviewJpaEntity)
            .where(
                shopId().eq(shopId),
                reviewJpaEntity.hidden.eq(false),
                reviewJpaEntity.createdAt.year().eq(year)
            )
            .groupBy(reviewJpaEntity.createdAt.month())
            .fetch();

        Map<Integer, Long> monthlyMap = new HashMap<>();
        for (Tuple row : results) {
            monthlyMap.put(row.get(0, Integer.class), row.get(1, Long.class));
        }
        return monthlyMap;
    }

    /**
     * 상품의 노출 리뷰 수.
     */
    public Long countByProductIdAndHiddenFalse(Long productId) {
        return queryFactory
            .select(reviewJpaEntity.count())
            .from(reviewJpaEntity)
            .where(productId().eq(productId), reviewJpaEntity.hidden.eq(false))
            .fetchOne();
    }

    /**
     * 상품 맛 평점 평균.
     */
    public Double getAverageTasteRatingByProductId(Long productId) {
        return queryFactory
            .select(reviewJpaEntity.tasteRating.avg())
            .from(reviewJpaEntity)
            .where(productId().eq(productId), reviewJpaEntity.hidden.eq(false))
            .fetchOne();
    }

    /**
     * 상품 양 평점 평균.
     */
    public Double getAverageAmountRatingByProductId(Long productId) {
        return queryFactory
            .select(reviewJpaEntity.amountRating.avg())
            .from(reviewJpaEntity)
            .where(productId().eq(productId), reviewJpaEntity.hidden.eq(false))
            .fetchOne();
    }

    /**
     * 상품 가격 평점 평균.
     */
    public Double getAveragePriceRatingByProductId(Long productId) {
        return queryFactory
            .select(reviewJpaEntity.priceRating.avg())
            .from(reviewJpaEntity)
            .where(productId().eq(productId), reviewJpaEntity.hidden.eq(false))
            .fetchOne();
    }

    /**
     * 회원이 쓴 노출 리뷰 수(회원 등급·랭킹 산정용).
     */
    public long countVisibleReviewsByMemberId(MemberId memberId) {
        Long count = queryFactory
            .select(reviewJpaEntity.count())
            .from(reviewJpaEntity)
            .where(reviewJpaEntity.memberId.eq(memberId), reviewJpaEntity.hidden.eq(false))
            .fetchOne();
        return count != null ? count : 0L;
    }

    /**
     * {@code @Convert} VO 컬럼인 {@code REVIEW.shop_id}를 raw {@code Long}으로 비교하기 위한 path.
     */
    private NumberPath<Long> shopId() {
        return Expressions.numberPath(Long.class, reviewJpaEntity, "shopId");
    }

    /**
     * {@code @Convert} VO 컬럼인 {@code REVIEW.product_id}를 raw {@code Long}으로 비교하기 위한 path.
     */
    private NumberPath<Long> productId() {
        return Expressions.numberPath(Long.class, reviewJpaEntity, "productId");
    }
}
