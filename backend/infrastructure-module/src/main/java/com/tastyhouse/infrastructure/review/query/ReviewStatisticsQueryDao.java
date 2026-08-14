package com.tastyhouse.infrastructure.review.query;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;


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
 * <p>소비자: web-api {@code ReviewQueryService}(가게·상품 리뷰 통계 조합, 회원 리뷰 수), ceo-api
 * {@code ShopReviewQueryService}(점주 통계 대시보드 — 기간 오버로드 사용).
 *
 * <p><b>이 DAO의 모든 집계는 {@code hidden = false}로 숨김 리뷰를 제외한다.</b> 반면 점주 리뷰 목록
 * ({@code ShopReviewManagementQueryDao})은 차단 탭을 위해 숨김을 <b>포함</b>하므로, 두 화면의 건수가
 * 의도적으로 다르다 — 목록 {@code totalElements}가 20인데 대시보드 {@code totalReviewCount}가 17일 수 있다.
 *
 * <p>이 비대칭은 실수가 아니라 판단이다: 통계는 "내 가게가 손님에게 어떻게 평가되는가"를 답하는 지표이고,
 * 게시중단된 리뷰는 손님에게 보이지 않으므로 평균·분포에 반영되면 안 된다. 반대로 목록은 "내가 관리해야 할
 * 리뷰"라서 차단된 것도 보여야 한다. <b>다음 세션이 두 화면의 숫자가 다르다는 이유로 한쪽 필터를 맞추지 말
 * 것</b> — 맞추면 차단된 악성 리뷰가 평점을 계속 끌어내리거나(통계에 포함), 점주가 차단 리뷰를 볼 수 없게
 * 된다(목록에서 제외).
 */
@Repository
public class ReviewStatisticsQueryDao {

    private final JPAQueryFactory queryFactory;

    public ReviewStatisticsQueryDao(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * 가게의 노출 리뷰 수.
     */
    public Long countByShopIdAndHiddenFalse(Long shopId) {
        return queryFactory
            .select(reviewJpaEntity.count())
            .from(reviewJpaEntity)
            .where(reviewJpaEntity.shopId.eq(shopId), reviewJpaEntity.hidden.eq(false))
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
                reviewJpaEntity.shopId.eq(shopId),
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
            .where(reviewJpaEntity.shopId.eq(shopId), reviewJpaEntity.hidden.eq(false))
            .fetchOne();
    }

    /**
     * 가게 양 평점 평균.
     */
    public Double getAverageAmountRating(Long shopId) {
        return queryFactory
            .select(reviewJpaEntity.amountRating.avg())
            .from(reviewJpaEntity)
            .where(reviewJpaEntity.shopId.eq(shopId), reviewJpaEntity.hidden.eq(false))
            .fetchOne();
    }

    /**
     * 가게 가격 평점 평균.
     */
    public Double getAveragePriceRating(Long shopId) {
        return queryFactory
            .select(reviewJpaEntity.priceRating.avg())
            .from(reviewJpaEntity)
            .where(reviewJpaEntity.shopId.eq(shopId), reviewJpaEntity.hidden.eq(false))
            .fetchOne();
    }

    /**
     * 가게 분위기 평점 평균.
     */
    public Double getAverageAtmosphereRating(Long shopId) {
        return queryFactory
            .select(reviewJpaEntity.atmosphereRating.avg())
            .from(reviewJpaEntity)
            .where(reviewJpaEntity.shopId.eq(shopId), reviewJpaEntity.hidden.eq(false))
            .fetchOne();
    }

    /**
     * 가게 친절 평점 평균.
     */
    public Double getAverageKindnessRating(Long shopId) {
        return queryFactory
            .select(reviewJpaEntity.kindnessRating.avg())
            .from(reviewJpaEntity)
            .where(reviewJpaEntity.shopId.eq(shopId), reviewJpaEntity.hidden.eq(false))
            .fetchOne();
    }

    /**
     * 가게 위생 평점 평균.
     */
    public Double getAverageHygieneRating(Long shopId) {
        return queryFactory
            .select(reviewJpaEntity.hygieneRating.avg())
            .from(reviewJpaEntity)
            .where(reviewJpaEntity.shopId.eq(shopId), reviewJpaEntity.hidden.eq(false))
            .fetchOne();
    }

    /**
     * 가게의 총점 구간(내림 정수)별 리뷰 수.
     */
    public Map<Integer, Long> getRatingCounts(Long shopId) {
        List<Tuple> results = queryFactory
            .select(reviewJpaEntity.totalRating.floor().intValue(), reviewJpaEntity.count())
            .from(reviewJpaEntity)
            .where(reviewJpaEntity.shopId.eq(shopId), reviewJpaEntity.hidden.eq(false))
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
                reviewJpaEntity.shopId.eq(shopId),
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
     * 기간 내 종합 평점 평균 — 점주 통계 대시보드용 <b>신설</b>.
     *
     * <p>기존 DAO에 총점 평균이 없었던 이유는 가게 평점을 {@code Shop.rating} 비정규화 컬럼에서 읽었기
     * 때문이다. 그 컬럼은 전체 기간 누적값이라 "최근 6개월" 평균을 답할 수 없어 여기서 직접 집계한다.
     *
     * <p>리뷰가 0건이면 {@code null}을 돌려준다(0.0이 아니다 — "평점 0점"과 구분해야 한다).
     */
    public Double getAverageTotalRating(Long shopId, LocalDateTime from, LocalDateTime to) {
        return queryFactory
            .select(reviewJpaEntity.totalRating.avg())
            .from(reviewJpaEntity)
            .where(
                reviewJpaEntity.shopId.eq(shopId),
                reviewJpaEntity.hidden.eq(false),
                reviewJpaEntity.createdAt.goe(from),
                reviewJpaEntity.createdAt.lt(to)
            )
            .fetchOne();
    }

    /**
     * 기간 내 리뷰 수 — <b>반열림 구간</b> {@code [from, to)}.
     *
     * <p>상한 없는 {@link #countSince}와 나누는 이유는, 통계 응답의 13개 필드가 <b>모두 같은 행 집합</b>을
     * 설명해야 하기 때문이다. 월별 그래프는 이미 {@code [from, to)}로 집계하므로, 헤더 카운트가 상한 없이
     * 집계되면 미래 시각 행(시계 오차·백필·관리자 보정)이 헤더에만 포함돼 "그래프 합 ≠ 총 건수"가 된다.
     */
    public long countBetween(Long shopId, LocalDateTime from, LocalDateTime to) {
        Long count = queryFactory
            .select(reviewJpaEntity.count())
            .from(reviewJpaEntity)
            .where(
                reviewJpaEntity.shopId.eq(shopId),
                reviewJpaEntity.hidden.eq(false),
                reviewJpaEntity.createdAt.goe(from),
                reviewJpaEntity.createdAt.lt(to)
            )
            .fetchOne();
        return count != null ? count : 0L;
    }

    /**
     * 기간 내 리뷰 수 — 180일 노출 게이트·최근 30일 카운트용.
     */
    public long countSince(Long shopId, LocalDateTime from) {
        Long count = queryFactory
            .select(reviewJpaEntity.count())
            .from(reviewJpaEntity)
            .where(
                reviewJpaEntity.shopId.eq(shopId),
                reviewJpaEntity.hidden.eq(false),
                reviewJpaEntity.createdAt.goe(from)
            )
            .fetchOne();
        return count != null ? count : 0L;
    }

    /**
     * 기간 내 항목별 평점 평균 — 맛·양·가격·분위기·친절·위생을 한 번에 집계한다.
     *
     * <p>항목별로 메서드를 6개 두면 통계 조회가 쿼리 6번이 된다. 같은 {@code WHERE}·같은 기간이라 한
     * 쿼리에서 함께 집계하는 것이 자연스럽다.
     */
    public ShopReviewCategoryAverageResult getCategoryAverages(Long shopId, LocalDateTime from, LocalDateTime to) {
        Tuple row = queryFactory
            .select(
                reviewJpaEntity.tasteRating.avg(),
                reviewJpaEntity.amountRating.avg(),
                reviewJpaEntity.priceRating.avg(),
                reviewJpaEntity.atmosphereRating.avg(),
                reviewJpaEntity.kindnessRating.avg(),
                reviewJpaEntity.hygieneRating.avg()
            )
            .from(reviewJpaEntity)
            .where(
                reviewJpaEntity.shopId.eq(shopId),
                reviewJpaEntity.hidden.eq(false),
                reviewJpaEntity.createdAt.goe(from),
                reviewJpaEntity.createdAt.lt(to)
            )
            .fetchOne();

        if (row == null) {
            return new ShopReviewCategoryAverageResult(null, null, null, null, null, null);
        }
        return new ShopReviewCategoryAverageResult(
            row.get(0, Double.class),
            row.get(1, Double.class),
            row.get(2, Double.class),
            row.get(3, Double.class),
            row.get(4, Double.class),
            row.get(5, Double.class)
        );
    }

    /**
     * 기간 내 재방문 의사 리뷰 수.
     */
    public long countWillRevisitBetween(Long shopId, LocalDateTime from, LocalDateTime to) {
        Long count = queryFactory
            .select(reviewJpaEntity.count())
            .from(reviewJpaEntity)
            .where(
                reviewJpaEntity.shopId.eq(shopId),
                reviewJpaEntity.hidden.eq(false),
                reviewJpaEntity.willRevisit.eq(true),
                reviewJpaEntity.createdAt.goe(from),
                reviewJpaEntity.createdAt.lt(to)
            )
            .fetchOne();
        return count != null ? count : 0L;
    }

    /**
     * 기간 내 총점 구간(내림 정수)별 리뷰 수 — 기존 전체 기간 오버로드의 기간 한정판.
     *
     * <p>키 1~5를 항상 채우는 정규화는 소비 Service가 한다 — DAO는 실제로 조회된 것만 담는다.
     */
    public Map<Integer, Long> getRatingCounts(Long shopId, LocalDateTime from, LocalDateTime to) {
        List<Tuple> results = queryFactory
            .select(reviewJpaEntity.totalRating.floor().intValue(), reviewJpaEntity.count())
            .from(reviewJpaEntity)
            .where(
                reviewJpaEntity.shopId.eq(shopId),
                reviewJpaEntity.hidden.eq(false),
                reviewJpaEntity.createdAt.goe(from),
                reviewJpaEntity.createdAt.lt(to)
            )
            .groupBy(reviewJpaEntity.totalRating.floor().intValue())
            .fetch();

        Map<Integer, Long> ratingMap = new HashMap<>();
        for (Tuple row : results) {
            ratingMap.put(row.get(0, Integer.class), row.get(1, Long.class));
        }
        return ratingMap;
    }

    /**
     * 기간 내 월별 리뷰 수 — 키는 {@code yyyy-MM}.
     *
     * <p>기존 {@code getMonthlyReviewCounts(shopId, year)}는 키가 <b>월(1~12)</b>이라 연도를 걸쳐 있는
     * 구간(최근 6개월)에서 작년 1월과 올해 1월이 같은 키로 뭉개진다. 그래서 기간 오버로드는 키를
     * {@code yyyy-MM}로 바꾼다.
     */
    public Map<String, Long> getMonthlyReviewCounts(Long shopId, LocalDateTime from, LocalDateTime to) {
        List<Tuple> results = queryFactory
            .select(yearMonthKey(), reviewJpaEntity.count())
            .from(reviewJpaEntity)
            .where(
                reviewJpaEntity.shopId.eq(shopId),
                reviewJpaEntity.hidden.eq(false),
                reviewJpaEntity.createdAt.goe(from),
                reviewJpaEntity.createdAt.lt(to)
            )
            .groupBy(yearMonthKey())
            .fetch();

        Map<String, Long> monthlyMap = new HashMap<>();
        for (Tuple row : results) {
            monthlyMap.put(row.get(0, String.class), row.get(1, Long.class));
        }
        return monthlyMap;
    }

    /**
     * 기간 내 월별 평균 종합 평점 — <b>신설</b>. 기존에는 월별 <i>카운트</i>만 있었다.
     */
    public Map<String, Double> getMonthlyAverageRatings(Long shopId, LocalDateTime from, LocalDateTime to) {
        List<Tuple> results = queryFactory
            .select(yearMonthKey(), reviewJpaEntity.totalRating.avg())
            .from(reviewJpaEntity)
            .where(
                reviewJpaEntity.shopId.eq(shopId),
                reviewJpaEntity.hidden.eq(false),
                reviewJpaEntity.createdAt.goe(from),
                reviewJpaEntity.createdAt.lt(to)
            )
            .groupBy(yearMonthKey())
            .fetch();

        Map<String, Double> monthlyMap = new HashMap<>();
        for (Tuple row : results) {
            monthlyMap.put(row.get(0, String.class), row.get(1, Double.class));
        }
        return monthlyMap;
    }

    /**
     * {@code yyyy-MM} 그룹 키. {@code DATE_FORMAT}을 쓰면 인덱스를 타지 못하지만, 기간 조건
     * ({@code created_at} 범위)이 이미 {@code idx_review_shop_id_created_at}로 대상을 좁힌 뒤
     * 그룹핑에만 쓰이므로 문제되지 않는다.
     */
    private StringTemplate yearMonthKey() {
        return Expressions.stringTemplate("DATE_FORMAT({0}, '%Y-%m')", reviewJpaEntity.createdAt);
    }

    /**
     * 상품의 노출 리뷰 수.
     */
    public Long countByProductIdAndHiddenFalse(Long productId) {
        return queryFactory
            .select(reviewJpaEntity.count())
            .from(reviewJpaEntity)
            .where(reviewJpaEntity.productId.eq(productId), reviewJpaEntity.hidden.eq(false))
            .fetchOne();
    }

    /**
     * 상품 맛 평점 평균.
     */
    public Double getAverageTasteRatingByProductId(Long productId) {
        return queryFactory
            .select(reviewJpaEntity.tasteRating.avg())
            .from(reviewJpaEntity)
            .where(reviewJpaEntity.productId.eq(productId), reviewJpaEntity.hidden.eq(false))
            .fetchOne();
    }

    /**
     * 상품 양 평점 평균.
     */
    public Double getAverageAmountRatingByProductId(Long productId) {
        return queryFactory
            .select(reviewJpaEntity.amountRating.avg())
            .from(reviewJpaEntity)
            .where(reviewJpaEntity.productId.eq(productId), reviewJpaEntity.hidden.eq(false))
            .fetchOne();
    }

    /**
     * 상품 가격 평점 평균.
     */
    public Double getAveragePriceRatingByProductId(Long productId) {
        return queryFactory
            .select(reviewJpaEntity.priceRating.avg())
            .from(reviewJpaEntity)
            .where(reviewJpaEntity.productId.eq(productId), reviewJpaEntity.hidden.eq(false))
            .fetchOne();
    }

    /**
     * 회원이 쓴 노출 리뷰 수(회원 등급·랭킹 산정용).
     */
    public long countVisibleReviewsByMemberId(Long memberId) {
        Long count = queryFactory
            .select(reviewJpaEntity.count())
            .from(reviewJpaEntity)
            .where(reviewJpaEntity.memberId.eq(memberId), reviewJpaEntity.hidden.eq(false))
            .fetchOne();
        return count != null ? count : 0L;
    }
}
