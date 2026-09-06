package com.tastyhouse.infrastructure.review.query;

import com.tastyhouse.application.review.port.out.ShopReviewStatisticsQueryPort;
import com.tastyhouse.application.review.port.out.ReviewStatisticsQueryPort;
import com.tastyhouse.application.review.port.out.ShopReviewCategoryAverageResult;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import static com.tastyhouse.infrastructure.review.persistence.QReviewJpaEntity.reviewJpaEntity;

@Repository
public class ReviewStatisticsQueryDao implements ReviewStatisticsQueryPort, ShopReviewStatisticsQueryPort {
    private final JPAQueryFactory queryFactory;

    public ReviewStatisticsQueryDao(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    private BooleanExpression visibleToCustomer() {
        return reviewJpaEntity.hidden.isFalse().and(reviewJpaEntity.ownerOnly.isFalse());
    }

    @Override
    public Long countVisibleByShopId(Long shopId) {
        return queryFactory
            .select(reviewJpaEntity.count())
            .from(reviewJpaEntity)
            .where(reviewJpaEntity.shopId.eq(shopId), visibleToCustomer())
            .fetchOne();
    }

    @Override
    public Long countWillRevisit(Long shopId) {
        return queryFactory
            .select(reviewJpaEntity.count())
            .from(reviewJpaEntity)
            .where(
                reviewJpaEntity.shopId.eq(shopId),
                visibleToCustomer(),
                reviewJpaEntity.willRevisit.eq(true)
            )
            .fetchOne();
    }

    @Override
    public Double getAverageTasteRating(Long shopId) {
        return queryFactory
            .select(reviewJpaEntity.tasteRating.avg())
            .from(reviewJpaEntity)
            .where(reviewJpaEntity.shopId.eq(shopId), visibleToCustomer())
            .fetchOne();
    }

    @Override
    public Double getAverageAmountRating(Long shopId) {
        return queryFactory
            .select(reviewJpaEntity.amountRating.avg())
            .from(reviewJpaEntity)
            .where(reviewJpaEntity.shopId.eq(shopId), visibleToCustomer())
            .fetchOne();
    }

    @Override
    public Double getAveragePriceRating(Long shopId) {
        return queryFactory
            .select(reviewJpaEntity.priceRating.avg())
            .from(reviewJpaEntity)
            .where(reviewJpaEntity.shopId.eq(shopId), visibleToCustomer())
            .fetchOne();
    }

    @Override
    public Double getAverageAtmosphereRating(Long shopId) {
        return queryFactory
            .select(reviewJpaEntity.atmosphereRating.avg())
            .from(reviewJpaEntity)
            .where(reviewJpaEntity.shopId.eq(shopId), visibleToCustomer())
            .fetchOne();
    }

    @Override
    public Double getAverageKindnessRating(Long shopId) {
        return queryFactory
            .select(reviewJpaEntity.kindnessRating.avg())
            .from(reviewJpaEntity)
            .where(reviewJpaEntity.shopId.eq(shopId), visibleToCustomer())
            .fetchOne();
    }

    @Override
    public Double getAverageHygieneRating(Long shopId) {
        return queryFactory
            .select(reviewJpaEntity.hygieneRating.avg())
            .from(reviewJpaEntity)
            .where(reviewJpaEntity.shopId.eq(shopId), visibleToCustomer())
            .fetchOne();
    }

    @Override
    public Map<Integer, Long> getRatingCounts(Long shopId) {
        List<Tuple> results = queryFactory
            .select(reviewJpaEntity.totalRating.floor().intValue(), reviewJpaEntity.count())
            .from(reviewJpaEntity)
            .where(reviewJpaEntity.shopId.eq(shopId), visibleToCustomer())
            .groupBy(reviewJpaEntity.totalRating.floor().intValue())
            .fetch();

        Map<Integer, Long> ratingMap = new HashMap<>();
        for (Tuple row : results) {
            ratingMap.put(row.get(0, Integer.class), row.get(1, Long.class));
        }
        return ratingMap;
    }

    @Override
    public Map<Integer, Long> getMonthlyReviewCounts(Long shopId, int year) {
        List<Tuple> results = queryFactory
            .select(reviewJpaEntity.createdAt.month(), reviewJpaEntity.count())
            .from(reviewJpaEntity)
            .where(
                reviewJpaEntity.shopId.eq(shopId),
                visibleToCustomer(),
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    private StringTemplate yearMonthKey() {
        return Expressions.stringTemplate("DATE_FORMAT({0}, '%Y-%m')", reviewJpaEntity.createdAt);
    }

    @Override
    public Long countVisibleByProductId(Long productId) {
        return queryFactory
            .select(reviewJpaEntity.count())
            .from(reviewJpaEntity)
            .where(reviewJpaEntity.productId.eq(productId), visibleToCustomer())
            .fetchOne();
    }

    @Override
    public Double getAverageTasteRatingByProductId(Long productId) {
        return queryFactory
            .select(reviewJpaEntity.tasteRating.avg())
            .from(reviewJpaEntity)
            .where(reviewJpaEntity.productId.eq(productId), visibleToCustomer())
            .fetchOne();
    }

    @Override
    public Double getAverageAmountRatingByProductId(Long productId) {
        return queryFactory
            .select(reviewJpaEntity.amountRating.avg())
            .from(reviewJpaEntity)
            .where(reviewJpaEntity.productId.eq(productId), visibleToCustomer())
            .fetchOne();
    }

    @Override
    public Double getAveragePriceRatingByProductId(Long productId) {
        return queryFactory
            .select(reviewJpaEntity.priceRating.avg())
            .from(reviewJpaEntity)
            .where(reviewJpaEntity.productId.eq(productId), visibleToCustomer())
            .fetchOne();
    }

    @Override
    public long countVisibleReviewsByMemberId(Long memberId) {
        Long count = queryFactory
            .select(reviewJpaEntity.count())
            .from(reviewJpaEntity)
            .where(reviewJpaEntity.memberId.eq(memberId), visibleToCustomer())
            .fetchOne();
        return count != null ? count : 0L;
    }
}
